package sessions

import Logging
import storage.ClientsStorage
import data.*
import network.Client

object WaitingForNameSession: Session {
    private val logging = Logging("WaitingForNameSession")
    private val log: (text: String) -> Unit = { text -> logging.log(text) }

    private val onlineSession = OnlineSession
    private val clientsWithoutName = ClientsStorage.clientsWithoutName

    override fun handleDataFromClient(data: Data, client: Client) {
        when (data) {
            is Name -> handleName(data, client)
            is HardRemovalOfThePlayer -> handleHardRemovalOfThePlayer(client)
            else -> {
                log("Unexpected data for the session WaitingForNameSession")
                log("Hard removing an unnamed client")
                handleHardRemovalOfThePlayer(client)
            }
        }
    }

    fun addClient(client: Client) {
        if (client.session == OnlineSession) {
            client.session = this
            log("The client $client has been moved from OnlineSession to WaitingForNameSession")
        } else {
            log("A new client $client has been registered")
        }
        clientsWithoutName.add(client)
    }

    private fun handleName(name: Name, client: Client) {
        log("The client $client has sent Name(\"${name.name}\")")
        val nameStr = name.name
        if (!ClientsStorage.whoIsOnline.contains(nameStr) && !ClientsStorage.whoIsInTheGame.contains(nameStr)) {
            log("The client \"${name.name}\" is moving to OnlineSession")
            client.name = nameStr
            clientsWithoutName.remove(client)
            onlineSession.addClient(client)

            log("Sending to the client AcceptingTheName(\"${name.name}\")")
            client.sendDataToClient(AcceptingTheName(nameStr))
        } else {
            log("The client \"${name.name}\" already exists")
            log("Sending to the client RefusalTheName(\"${name.name}\")")
            client.sendDataToClient(RefusalTheName(nameStr))
        }
    }

    private fun handleHardRemovalOfThePlayer(client: Client) {
        client.socket.close()
        clientsWithoutName.remove(client)
    }
}