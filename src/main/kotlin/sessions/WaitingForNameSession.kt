package sessions

import common.NamesStorage
import data.*
import network.Client

class WaitingForNameSession: Session {
    private val onlineSession = OnlineSession()
    private val clientsWithoutName = mutableSetOf<Client>()

    override fun handleDataFromClient(data: Data, client: Client) {
        when (data){
            is Name -> handleName(data, client)
            is Exit -> handleExit(data, client)
            else -> println("SERVER: Unexpected data for the session WaitingForNameSession")
        }
    }

    fun addClient(client: Client) {
        println("SERVER: A new client was registered")
        clientsWithoutName.add(client)
    }

    private fun handleName(name: Name, client: Client) {
        println("SERVER: The client sent Name(\"${name.name}\")")
        val nameStr = name.name
        if (!NamesStorage.whoIsOnline.contains(nameStr) && !NamesStorage.whoIsInTheGame.contains(nameStr)) {
            println("SERVER: The client with name \"${name.name}\" is moving to OnlineSession")
            client.name = nameStr
            clientsWithoutName.remove(client)
            onlineSession.addClient(client)

            println("SERVER: Sending to the client AcceptingTheName(\"${name.name}\")")
            client.sendDataToClient(AcceptingTheName(nameStr))
        } else {
            println("SERVER: The client with name \"${name.name}\" already exists")
            println("SERVER: Sending to the client RefusalTheName(\"${name.name}\")")
            client.sendDataToClient(RefusalTheName(nameStr))
        }
    }

    private fun handleExit(exit: Exit, client: Client) {
        client.socket.close()
    }
}