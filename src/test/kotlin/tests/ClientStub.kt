package tests

import clientside.Observer
import clientside.ReceiverFromServer
import clientside.SenderToServer
import data.Data
import data.Ping
import data.Pong

abstract class ClientStub : Observer {
    private val receiverFromServer = ReceiverFromServer(this)
    private val senderToServer = SenderToServer(receiverFromServer)

    var count = 0

    override fun getData(data: Data) {
        if (data is Ping) {
            sendData(Pong())
            return
        }
    }

    fun createConnection() {
        senderToServer.createConnection()
    }

    fun sendData(data: Data) {
        senderToServer.sendData(data)
    }

    fun closeConnection() {
        senderToServer.closeConnection()
    }
}