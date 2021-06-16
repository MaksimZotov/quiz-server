package clientside

import clientside.network.Server
import data.Data

class SenderToServer(receiverFromServer: ReceiverFromServer) {
    private val server = Server(receiverFromServer)

    fun createConnection() {
        server.createConnection()
    }

    fun sendData(data: Data) {
        server.sendDataToServer(data)
    }

    fun closeConnection() {
        server.closeConnection()
    }
}