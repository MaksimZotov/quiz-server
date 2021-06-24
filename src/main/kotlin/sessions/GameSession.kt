package sessions

import Logging
import storage.ClientsStorage
import data.*
import game.GameState
import game.GameStateSender
import network.Client

class GameSession(
        private val onlineSession: OnlineSession,
        private val players: List<Client>) : Session, GameStateSender {

    private val logging = Logging("GameSession")
    private val log: (text: String) -> Unit = { text -> logging.log(text) }

    private val whoIsInTheGame = ClientsStorage.whoIsInTheGame
    private val nameToClient = ClientsStorage.nameToClient

    private val whoWantsToPlay = mutableMapOf<Client, Boolean>()
    private val names = players.map { it.name }

    private var gameState = GameState(this, names)

    init {
        ClientsStorage.whoIsInTheGame.addAll(names)

        players.forEach {
            whoWantsToPlay[it] = false
            it.session = this
        }

        log("Players $names have been added to GameSession")

        val playTheGame = PlayTheGame()
        log("Sending to clients $names PlayTheGame()")
        players.forEach { it.sendDataToClient(playTheGame)}
    }

    override fun handleDataFromClient(data: Data, client: Client) {
        when (data) {
            is Answer -> handleAnswer(data, client)
            is LeavingTheGame -> handleLeavingTheGame(data, client)
            is RequestToPlayAgain -> handleRequestToPlayAgain(client)
            is RefusalToPlayAgain -> handleRefusalToPlayAgain(data, client)
            is HardRemovalOfThePlayer -> handleHardRemovalOfThePlayer(data, client)
            else -> {
                log("Unexpected data for the session GameSession")
                log("Hard removing the client $client")
                handleHardRemovalOfThePlayer(HardRemovalOfThePlayer(), client)
            }
        }
    }

    private fun handleAnswer(answer: Answer, client: Client) {
        log("The client \"${client.name}\" has sent Answer(${answer.indexOfAnswer})")
        gameState.getAnswer(client.name, answer.indexOfAnswer)
    }

    private fun handleLeavingTheGame(leavingTheGame: LeavingTheGame, client: Client) {
        gameState.stopGame()
        log("The client $client has sent LeavingTheGame()")
        val clientsWhoMustBeNotified = players.filter { it != client }
        log("Sending to the clients $clientsWhoMustBeNotified LeavingTheGame()")
        clientsWhoMustBeNotified.forEach { it.sendDataToClient(leavingTheGame) }
        players.forEach {
            whoIsInTheGame.remove(it.name)
            onlineSession.addClient(it)
        }
    }

    private fun handleRequestToPlayAgain(client: Client) {
        whoWantsToPlay[client] = true
        if (whoWantsToPlay.all { it.value }) {
            gameState = GameState(this, names)
            whoWantsToPlay.forEach { key, _ -> whoWantsToPlay[key] = false }
            val playTheGame = PlayTheGame()
            log("Sending to clients $names PlayTheGame()")
            players.forEach { it.sendDataToClient(playTheGame)}
        }
    }

    private fun handleRefusalToPlayAgain(refusalToPlayAgain: RefusalToPlayAgain, client: Client) {
        log("The client \"${client.name}\" has sent RefusalToPlayAgain()")
        val clientsWhoMustBeNotified = players.filter { it != client }
        log("Sending to the clients $clientsWhoMustBeNotified RefusalToPlayAgain()")
        clientsWhoMustBeNotified.forEach { it.sendDataToClient(refusalToPlayAgain) }
        players.forEach {
            whoIsInTheGame.remove(it.name)
            onlineSession.addClient(it)
        }
    }

    private fun handleHardRemovalOfThePlayer(hardRemovalOfThePlayer: HardRemovalOfThePlayer, client: Client) {
        gameState.stopGame()
        client.stop()
        whoIsInTheGame.remove(client.name)
        nameToClient.remove(client.name)
        val clientsWhoMustBeNotified = players.filter { it != client }
        log("Sending to the clients $clientsWhoMustBeNotified HardRemovalOfThePlayer()")
        clientsWhoMustBeNotified.forEach {
            it.sendDataToClient(hardRemovalOfThePlayer)
            whoIsInTheGame.remove(it.name)
            onlineSession.addClient(it)
        }
    }


    override fun sendScore(playerNameToScore: Map<String, Int>) {
        val scoreData = Score(playerNameToScore)
        players.forEach { it.sendDataToClient(scoreData) }
    }

    override fun sendQuestion(question: Triple<String, List<String>, Int>) {
        val questionData = Question(question.first, question.second, question.third)
        players.forEach { it.sendDataToClient(questionData) }
    }

    override fun sendRemainingTime(remainingTime: Int) {
        val remainingTimeData = RemainingTime(remainingTime)
        players.forEach { it.sendDataToClient(remainingTimeData) }
    }

    override fun sendFinish() {
        val finishData = FinishTheGame()
        players.forEach { it.sendDataToClient(finishData) }
    }
}