package sessions

import common.NamesStorage
import data.*
import log
import network.Client

class WaitingForNameSession: Session {
    private val onlineSession = OnlineSession()
    private val clientsWithoutName = mutableSetOf<Client>()

    override fun handleDataFromClient(data: Data, client: Client) {
        when (data){
            is Name -> handleName(data, client)
            is Exit -> handleExit(data, client)
            else -> log("SERVER: Unexpected data for the session WaitingForNameSession")
        }
    }

    fun addClient(client: Client) {
        log("SERVER: A new client has been registered")
        clientsWithoutName.add(client)
    }

    private fun handleName(name: Name, client: Client) {
        log("SERVER: The client has sent Name(\"${name.name}\")")
        val nameStr = name.name
        if (!NamesStorage.whoIsOnline.contains(nameStr) && !NamesStorage.whoIsInTheGame.contains(nameStr)) {
            log("SERVER: The client with name \"${name.name}\" is moving to OnlineSession")
            client.playerName = nameStr
            clientsWithoutName.remove(client)
            onlineSession.addClient(client)

            log("SERVER: Sending to the client AcceptingTheName(\"${name.name}\")")
            client.sendDataToClient(AcceptingTheName(nameStr))
        } else {
            log("SERVER: The client with name \"${name.name}\" already exists")
            log("SERVER: Sending to the client RefusalTheName(\"${name.name}\")")
            client.sendDataToClient(RefusalTheName(nameStr))
        }
    }

    private fun handleExit(exit: Exit, client: Client) {
        client.socket.close()
    }
}