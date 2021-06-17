package sessions

import common.NamesStorage
import data.*
import game.GameState
import log
import network.Client

class GameSession(val onlineSession: OnlineSession, val firstPlayer: Client, val secondPlayer: Client) : Session {
    val gameState: GameState = GameState(this, firstPlayer.playerName, secondPlayer.playerName)

    init {
        NamesStorage.whoIsInTheGame.addAll(listOf(firstPlayer.playerName, secondPlayer.playerName))

        firstPlayer.session = this
        secondPlayer.session = this
        log("SERVER: \"${firstPlayer.playerName}\" and \"${secondPlayer.playerName}\" have been added to GameSession")

        val playTheGame = PlayTheGame()
        log("SERVER: Sending to the client with name \"${firstPlayer.playerName}\" PlayTheGame()")
        firstPlayer.sendDataToClient(playTheGame)
        log("SERVER: Sending to the client with name \"${secondPlayer.playerName}\" PlayTheGame()")
        secondPlayer.sendDataToClient(playTheGame)
    }

    override fun handleDataFromClient(data: Data, client: Client) {
        when (data) {
            is LeavingTheGame -> handleLeavingTheGame(data, client)
            is RequestToPlayAgain -> handleRequestToPlayAgain(data, client)
            is RefusalToPlayAgain -> handleRefusalToPlayAgain(data, client)
            is Exit -> handleExit(data, client)
        }
    }

    private fun handleLeavingTheGame(leavingTheGame: LeavingTheGame, client: Client) {
        val clientWhoMustBeNotified = if (client == firstPlayer) firstPlayer else secondPlayer
        clientWhoMustBeNotified.sendDataToClient(leavingTheGame)
        NamesStorage.whoIsInTheGame.remove(firstPlayer.name)
        NamesStorage.whoIsInTheGame.remove(secondPlayer.name)
        onlineSession.addClient(firstPlayer)
        onlineSession.addClient(secondPlayer)
    }

    private fun handleRequestToPlayAgain(requestToPlayAgain: RequestToPlayAgain, client: Client) {
        TODO()
    }

    private fun handleRefusalToPlayAgain(refusalToPlayAgain: RefusalToPlayAgain, client: Client) {
        val clientWhoMustBeNotified = if (client == firstPlayer) firstPlayer else secondPlayer
        clientWhoMustBeNotified.sendDataToClient(refusalToPlayAgain)
        NamesStorage.whoIsInTheGame.remove(firstPlayer.playerName)
        NamesStorage.whoIsInTheGame.remove(secondPlayer.playerName)
        onlineSession.addClient(firstPlayer)
        onlineSession.addClient(secondPlayer)
    }

    private fun handleExit(exit: Exit, client: Client) {
        TODO()
    }
}