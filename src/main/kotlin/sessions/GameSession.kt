package sessions

import common.NamesStorage
import data.*
import game.GameState
import game.GameStateSender
import log
import network.Client

class GameSession(
        private val onlineSession: OnlineSession,
        private val firstPlayer: Client,
        private val secondPlayer: Client) : Session, GameStateSender {

    var gameState = GameState(this, firstPlayer.playerName, secondPlayer.playerName)
    var firstPlayerWantsToPlayAgain = false
    var secondPlayerWantsToPlayAgain = false

    init {
        NamesStorage.whoIsInTheGame.addAll(listOf(firstPlayer.playerName, secondPlayer.playerName))

        firstPlayer.session = this
        secondPlayer.session = this
        log("SERVER: \"${firstPlayer.playerName}\" and \"${secondPlayer.playerName}\" have been added to GameSession")

        val playTheGame = PlayTheGame()
        log("SERVER: Sending to the client \"${firstPlayer.playerName}\" PlayTheGame()")
        firstPlayer.sendDataToClient(playTheGame)
        log("SERVER: Sending to the client \"${secondPlayer.playerName}\" PlayTheGame()")
        secondPlayer.sendDataToClient(playTheGame)
    }

    override fun handleDataFromClient(data: Data, client: Client) {
        when (data) {
            is Answer -> handleAnswer(data, client)
            is LeavingTheGame -> handleLeavingTheGame(data, client)
            is RequestToPlayAgain -> handleRequestToPlayAgain(client)
            is RefusalToPlayAgain -> handleRefusalToPlayAgain(data, client)
            is HardRemovalOfThePlayer -> handleHardRemovalOfThePlayer(client)
            else -> {
                log("SERVER: Unexpected data for the session GameSession")
                log("SERVER: Hard removing the client \"${client.playerName}\"")
                handleHardRemovalOfThePlayer(client)
            }
        }
    }

    private fun handleAnswer(answer: Answer, client: Client) {
        log("SERVER: The client \"${client.playerName}\" has sent Answer(${answer.indexOfAnswer})")
        gameState.getAnswer(client.playerName, answer.indexOfAnswer)
    }

    private fun handleLeavingTheGame(leavingTheGame: LeavingTheGame, client: Client) {
        gameState.stopGame()
        log("SERVER: The client \"${client.playerName}\" has sent LeavingTheGame()")
        val clientWhoMustBeNotified = if (client == firstPlayer) secondPlayer else firstPlayer
        log("SERVER: Sending to the client \"${clientWhoMustBeNotified.playerName}\" LeavingTheGame()")
        clientWhoMustBeNotified.sendDataToClient(leavingTheGame)
        NamesStorage.whoIsInTheGame.remove(firstPlayer.playerName)
        NamesStorage.whoIsInTheGame.remove(secondPlayer.playerName)
        onlineSession.addClient(firstPlayer)
        onlineSession.addClient(secondPlayer)
    }

    private fun handleRequestToPlayAgain(client: Client) {
        when (client) {
            firstPlayer -> firstPlayerWantsToPlayAgain = true
            secondPlayer -> secondPlayerWantsToPlayAgain = true
        }
        if (firstPlayerWantsToPlayAgain && secondPlayerWantsToPlayAgain) {
            gameState = GameState(this, firstPlayer.playerName, secondPlayer.playerName)
            firstPlayerWantsToPlayAgain = false
            secondPlayerWantsToPlayAgain = false

            val playTheGame = PlayTheGame()
            log("SERVER: Sending to the client \"${firstPlayer.playerName}\" PlayTheGame()")
            firstPlayer.sendDataToClient(playTheGame)
            log("SERVER: Sending to the client \"${secondPlayer.playerName}\" PlayTheGame()")
            secondPlayer.sendDataToClient(playTheGame)
        }
    }

    private fun handleRefusalToPlayAgain(refusalToPlayAgain: RefusalToPlayAgain, client: Client) {
        log("SERVER: The client \"${client.playerName}\" has sent RefusalToPlayAgain()")
        val clientWhoMustBeNotified = if (client == firstPlayer) secondPlayer else firstPlayer
        log("SERVER: Sending to the client \"${clientWhoMustBeNotified.playerName}\" RefusalToPlayAgain()")
        clientWhoMustBeNotified.sendDataToClient(refusalToPlayAgain)
        NamesStorage.whoIsInTheGame.remove(firstPlayer.playerName)
        NamesStorage.whoIsInTheGame.remove(secondPlayer.playerName)
        onlineSession.addClient(firstPlayer)
        onlineSession.addClient(secondPlayer)
    }

    private fun handleHardRemovalOfThePlayer(client: Client) {
        client.socket.close()
        val clientWhoMustBeNotified = if (client == firstPlayer) secondPlayer else firstPlayer
        log("SERVER: Sending to the client \"${clientWhoMustBeNotified.playerName}\" HardRemovalOfThePlayer()")
        clientWhoMustBeNotified.sendDataToClient(HardRemovalOfThePlayer())
        NamesStorage.nameToClient.remove(client.playerName)
        NamesStorage.whoIsInTheGame.remove(client.playerName)
    }


    override fun sendScore(playerNameToScore: Map<String, Int>) {
        val playerScore = playerNameToScore[firstPlayer.playerName]!!
        val scoreOfAnotherPlayer = playerNameToScore[secondPlayer.playerName]!!
        firstPlayer.sendDataToClient(Score(playerScore, scoreOfAnotherPlayer))
        secondPlayer.sendDataToClient(Score(scoreOfAnotherPlayer, playerScore))
    }

    override fun sendQuestion(question: Triple<String, List<String>, Int>) {
        val questionData = Question(question.first, question.second, question.third)
        firstPlayer.sendDataToClient(questionData)
        secondPlayer.sendDataToClient(questionData)
    }

    override fun sendRemainingTime(remainingTime: Int) {
        val remainingTimeData = RemainingTime(remainingTime)
        firstPlayer.sendDataToClient(remainingTimeData)
        secondPlayer.sendDataToClient(remainingTimeData)
    }

    override fun sendFinish() {
        val finishData = FinishTheGame()
        firstPlayer.sendDataToClient(finishData)
        secondPlayer.sendDataToClient(finishData)
    }
}