package sessions

import common.NamesStorage
import data.Data
import data.Exit
import data.RefusalToPlayAgain
import game.GameState
import network.Client

class GameSession(val onlineSession: OnlineSession, val firstPlayer: Client, val secondPlayer: Client) : Session {
    val gameState: GameState = GameState(this, firstPlayer.name, secondPlayer.name)

    init {
        NamesStorage.whoIsInTheGame.addAll(listOf(firstPlayer.name, secondPlayer.name))
    }

    override fun handleDataFromClient(data: Data, client: Client) {
        when (data) {
            is RefusalToPlayAgain -> {
                val clientWhoMustBeNotified = if (client == firstPlayer) firstPlayer else secondPlayer
                clientWhoMustBeNotified.sendDataToClient(data)
                NamesStorage.whoIsInTheGame.remove(firstPlayer.name)
                NamesStorage.whoIsInTheGame.remove(secondPlayer.name)
                onlineSession.addClient(firstPlayer)
                onlineSession.addClient(secondPlayer)
            }
            is Exit -> {

            }
        }
    }
}