package tests

import clientside.Observer
import clientside.ReceiverFromServer
import clientside.SenderToServer
import data.Data

abstract class ClientStub : Observer {
    private val receiverFromServer = ReceiverFromServer(this)
    private val senderToServer = SenderToServer(receiverFromServer)

    var count = 0

    abstract override fun getData(data: Data)

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