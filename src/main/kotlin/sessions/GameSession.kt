package sessions

import Logging
import storage.ClientsStorage
import data.*
import game.GameState
import game.GameStateSender
import network.Client

class GameSession(
        private val onlineSession: OnlineSession,
        private val firstPlayer: Client,
        private val secondPlayer: Client) : Session, GameStateSender {

    private val logging = Logging("GameSession")
    private val log: (text: String) -> Unit = { text -> logging.log(text) }

    var gameState = GameState(this, firstPlayer.name, secondPlayer.name)
    var firstPlayerWantsToPlayAgain = false
    var secondPlayerWantsToPlayAgain = false

    init {
        ClientsStorage.whoIsInTheGame.addAll(listOf(firstPlayer.name, secondPlayer.name))

        firstPlayer.session = this
        secondPlayer.session = this
        log("$firstPlayer and $secondPlayer have been added to GameSession")

        val playTheGame = PlayTheGame()
        log("Sending to the client $firstPlayer PlayTheGame()")
        firstPlayer.sendDataToClient(playTheGame)
        log("Sending to the client $secondPlayer PlayTheGame()")
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
                log("Hard removing the client \"${client.name}\"")
                handleHardRemovalOfThePlayer(client)
            }
        }
    }

    private fun handleAnswer(answer: Answer, client: Client) {
        log("The client \"${client.name}\" has sent Answer(${answer.indexOfAnswer})")
        gameState.getAnswer(client.name, answer.indexOfAnswer)
    }

    private fun handleLeavingTheGame(leavingTheGame: LeavingTheGame, client: Client) {
        gameState.stopGame()
        log("The client \"${client.name}\" has sent LeavingTheGame()")
        val clientWhoMustBeNotified = if (client == firstPlayer) secondPlayer else firstPlayer
        log("Sending to the client \"${clientWhoMustBeNotified.name}\" LeavingTheGame()")
        clientWhoMustBeNotified.sendDataToClient(leavingTheGame)
        ClientsStorage.whoIsInTheGame.remove(firstPlayer.name)
        ClientsStorage.whoIsInTheGame.remove(secondPlayer.name)
        onlineSession.addClient(firstPlayer)
        onlineSession.addClient(secondPlayer)
    }

    private fun handleRequestToPlayAgain(client: Client) {
        when (client) {
            firstPlayer -> firstPlayerWantsToPlayAgain = true
            secondPlayer -> secondPlayerWantsToPlayAgain = true
        }
        if (firstPlayerWantsToPlayAgain && secondPlayerWantsToPlayAgain) {
            gameState = GameState(this, firstPlayer.name, secondPlayer.name)
            firstPlayerWantsToPlayAgain = false
            secondPlayerWantsToPlayAgain = false

            val playTheGame = PlayTheGame()
            log("Sending to the client $firstPlayer PlayTheGame()")
            firstPlayer.sendDataToClient(playTheGame)
            log("Sending to the client $secondPlayer PlayTheGame()")
            secondPlayer.sendDataToClient(playTheGame)
        }
    }

    private fun handleRefusalToPlayAgain(refusalToPlayAgain: RefusalToPlayAgain, client: Client) {
        log("The client \"${client.name}\" has sent RefusalToPlayAgain()")
        val clientWhoMustBeNotified = if (client == firstPlayer) secondPlayer else firstPlayer
        log("Sending to the client \"${clientWhoMustBeNotified.name}\" RefusalToPlayAgain()")
        clientWhoMustBeNotified.sendDataToClient(refusalToPlayAgain)
        ClientsStorage.whoIsInTheGame.remove(firstPlayer.name)
        ClientsStorage.whoIsInTheGame.remove(secondPlayer.name)
        onlineSession.addClient(firstPlayer)
        onlineSession.addClient(secondPlayer)
    }

    private fun handleHardRemovalOfThePlayer(client: Client) {
        client.socket.close()
        val clientWhoMustBeNotified = if (client == firstPlayer) secondPlayer else firstPlayer
        log("Sending to the client \"${clientWhoMustBeNotified.name}\" HardRemovalOfThePlayer()")
        clientWhoMustBeNotified.sendDataToClient(HardRemovalOfThePlayer())
        ClientsStorage.nameToClient.remove(client.name)
        ClientsStorage.whoIsInTheGame.remove(client.name)
    }


    override fun sendScore(playerNameToScore: Map<String, Int>) {
        val playerScore = playerNameToScore[firstPlayer.name]!!
        val scoreOfAnotherPlayer = playerNameToScore[secondPlayer.name]!!
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