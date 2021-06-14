package sessions

import data.Data
import data.Name
import network.Client

class WaitingForNameSession: Session {
    private val onlineSession = OnlineSession()
    private val clients = mutableSetOf<Client>()

    override fun handleDataFromClient(data: Data, client: Client) {
        if (data is Name) {
            val name = data.name
            if (!NamesStorage.whoIsOnline.contains(name) && !NamesStorage.whoIsInTheGame.contains(name)) {
                NamesStorage.whoIsOnline.add(name)
                clients.remove(client)
                client.name = name
                onlineSession.addClientHandler(client)
            }
        }
    }

    fun addClient(client: Client) {
        clients.add(client)
    }
}