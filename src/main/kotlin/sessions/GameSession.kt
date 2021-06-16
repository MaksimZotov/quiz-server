package sessions

import common.NamesStorage
import data.*
import game.GameState
import network.Client

class GameSession(val onlineSession: OnlineSession, val firstPlayer: Client, val secondPlayer: Client) : Session {
    val gameState: GameState = GameState(this, firstPlayer.name, secondPlayer.name)

    init {
        NamesStorage.whoIsInTheGame.addAll(listOf(firstPlayer.name, secondPlayer.name))

        val playTheGame = PlayTheGame()
        println("SERVER: Sending to the client with name \"${firstPlayer.name}\" PlayTheGame()")
        firstPlayer.sendDataToClient(playTheGame)
        println("SERVER: Sending to the client with name \"${secondPlayer.name}\" PlayTheGame()")
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
        TODO()
    }

    private fun handleRequestToPlayAgain(requestToPlayAgain: RequestToPlayAgain, client: Client) {
        TODO()
    }

    private fun handleRefusalToPlayAgain(refusalToPlayAgain: RefusalToPlayAgain, client: Client) {
        val clientWhoMustBeNotified = if (client == firstPlayer) firstPlayer else secondPlayer
        clientWhoMustBeNotified.sendDataToClient(refusalToPlayAgain)
        NamesStorage.whoIsInTheGame.remove(firstPlayer.name)
        NamesStorage.whoIsInTheGame.remove(secondPlayer.name)
        onlineSession.addClient(firstPlayer)
        onlineSession.addClient(secondPlayer)
    }

    private fun handleExit(exit: Exit, client: Client) {
        TODO()
    }
}