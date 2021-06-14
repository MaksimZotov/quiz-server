package sessions

import data.Data
import network.Client

class OnlineSession: Session {
    private val clients = mutableSetOf<Client>()

    override fun handleDataFromClient(data: Data, client: Client) {
        TODO("Not yet implemented")
    }

    fun addClientHandler(client: Client) {
        client.session = this
        clients.add(client)
    }
}