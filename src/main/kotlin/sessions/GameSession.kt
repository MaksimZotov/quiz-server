package sessions

import common.NamesStorage
import data.*
import game.GameState
import game.GameStateSender
import log
import network.Client

class GameSession(val onlineSession: OnlineSession, val firstPlayer: Client, val secondPlayer: Client) : Session, GameStateSender {
    val gameState: GameState = GameState(this, firstPlayer.playerName, secondPlayer.playerName)

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
            is RequestToPlayAgain -> handleRequestToPlayAgain(data, client)
            is RefusalToPlayAgain -> handleRefusalToPlayAgain(data, client)
            is Exit -> handleExit(data, client)
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

    private fun handleRequestToPlayAgain(requestToPlayAgain: RequestToPlayAgain, client: Client) {
        TODO()
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

    private fun handleExit(exit: Exit, client: Client) {
        TODO()
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