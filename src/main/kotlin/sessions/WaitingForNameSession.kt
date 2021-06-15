package sessions

import common.NamesStorage
import data.Data
import data.Exit
import data.Name
import network.Client

class WaitingForNameSession: Session {
    private val onlineSession = OnlineSession()
    private val clientsWithoutName = mutableSetOf<Client>()

    override fun handleDataFromClient(data: Data, client: Client) {
        when (data){
            is Name -> handleName(data, client)
            is Exit -> handleExit(data, client)
        }
    }

    fun addClient(client: Client) {
        clientsWithoutName.add(client)
    }

    private fun handleName(name: Name, client: Client) {
        val nameStr = name.name
        if (!NamesStorage.whoIsOnline.contains(nameStr) && !NamesStorage.whoIsInTheGame.contains(nameStr)) {
            client.name = nameStr
            clientsWithoutName.remove(client)
            onlineSession.addClient(client)
        }
    }

    private fun handleExit(exit: Exit, client: Client) {
        client.socket.close()
    }
}