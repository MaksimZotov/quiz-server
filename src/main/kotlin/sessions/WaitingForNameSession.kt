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
            is Name -> {
                val name = data.name
                if (!NamesStorage.whoIsOnline.contains(name) && !NamesStorage.whoIsInTheGame.contains(name)) {
                    client.name = name
                    clientsWithoutName.remove(client)
                    onlineSession.addClient(client)
                }
            }
            is Exit -> {
                client.socket.close()
            }
        }
    }

    fun addClient(client: Client) {
        clientsWithoutName.add(client)
    }
}