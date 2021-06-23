package sessions

import common.ClientsStorage
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
        ClientsStorage.whoIsInTheGame.addAll(listOf(firstPlayer.playerName, secondPlayer.playerName))

        firstPlayer.session = this
        secondPlayer.session = this
        log("\"${firstPlayer.playerName}\" and \"${secondPlayer.playerName}\" have been added to GameSession")

        val playTheGame = PlayTheGame()
        log("Sending to the client \"${firstPlayer.playerName}\" PlayTheGame()")
        firstPlayer.sendDataToClient(playTheGame)
        log("Sending to the client \"${secondPlayer.playerName}\" PlayTheGame()")
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
                log("Unexpected data for the session GameSession")
                log("Hard removing the client \"${client.playerName}\"")
                handleHardRemovalOfThePlayer(client)
            }
        }
    }

    private fun handleAnswer(answer: Answer, client: Client) {
        log("The client \"${client.playerName}\" has sent Answer(${answer.indexOfAnswer})")
        gameState.getAnswer(client.playerName, answer.indexOfAnswer)
    }

    private fun handleLeavingTheGame(leavingTheGame: LeavingTheGame, client: Client) {
        gameState.stopGame()
        log("The client \"${client.playerName}\" has sent LeavingTheGame()")
        val clientWhoMustBeNotified = if (client == firstPlayer) secondPlayer else firstPlayer
        log("Sending to the client \"${clientWhoMustBeNotified.playerName}\" LeavingTheGame()")
        clientWhoMustBeNotified.sendDataToClient(leavingTheGame)
        ClientsStorage.whoIsInTheGame.remove(firstPlayer.playerName)
        ClientsStorage.whoIsInTheGame.remove(secondPlayer.playerName)
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
            log("Sending to the client \"${firstPlayer.playerName}\" PlayTheGame()")
            firstPlayer.sendDataToClient(playTheGame)
            log("Sending to the client \"${secondPlayer.playerName}\" PlayTheGame()")
            secondPlayer.sendDataToClient(playTheGame)
        }
    }

    private fun handleRefusalToPlayAgain(refusalToPlayAgain: RefusalToPlayAgain, client: Client) {
        log("The client \"${client.playerName}\" has sent RefusalToPlayAgain()")
        val clientWhoMustBeNotified = if (client == firstPlayer) secondPlayer else firstPlayer
        log("Sending to the client \"${clientWhoMustBeNotified.playerName}\" RefusalToPlayAgain()")
        clientWhoMustBeNotified.sendDataToClient(refusalToPlayAgain)
        ClientsStorage.whoIsInTheGame.remove(firstPlayer.playerName)
        ClientsStorage.whoIsInTheGame.remove(secondPlayer.playerName)
        onlineSession.addClient(firstPlayer)
        onlineSession.addClient(secondPlayer)
    }

    private fun handleHardRemovalOfThePlayer(client: Client) {
        client.socket.close()
        val clientWhoMustBeNotified = if (client == firstPlayer) secondPlayer else firstPlayer
        log("Sending to the client \"${clientWhoMustBeNotified.playerName}\" HardRemovalOfThePlayer()")
        clientWhoMustBeNotified.sendDataToClient(HardRemovalOfThePlayer())
        ClientsStorage.nameToClient.remove(client.playerName)
        ClientsStorage.whoIsInTheGame.remove(client.playerName)
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