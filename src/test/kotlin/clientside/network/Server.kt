package clientside.network

import clientside.ReceiverFromServer
import clientside.network.Reader
import clientside.network.Sender
import data.Data
import data.Exit
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

class Server {
    private lateinit var clientSocket: Socket
    private lateinit var output: ObjectOutputStream
    private lateinit var input: ObjectInputStream
    private lateinit var sender: Sender
    private lateinit var reader: Reader

    fun createConnection() {
        clientSocket = Socket("localhost", 80)
        output = ObjectOutputStream(clientSocket.getOutputStream())
        input = ObjectInputStream(clientSocket.getInputStream())
        sender = Sender(output)
        reader = Reader(this, input)
    }

    fun closeConnection() {
        sender.sendDataToServer(Exit())
        clientSocket.close()
        input.close()
        output.close()
    }

    fun sendDataToServer(data: Data) {
        sender.sendDataToServer(data)
    }

    fun handleDataFromServer(data: Data) {
        ReceiverFromServer.getData(data)
    }
}