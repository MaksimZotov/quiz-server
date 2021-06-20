package network

import data.Data
import log
import sessions.Session
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

class Client(val socket: Socket, var session: Session) : Thread() {
    private val input: ObjectInputStream = ObjectInputStream(socket.getInputStream())
    private val output: ObjectOutputStream = ObjectOutputStream(socket.getOutputStream())

    lateinit var playerName: String

    init {
        start()
    }

    override fun run() {
        while (true) {
            val data = input.readObject() as Data
            log("SERVER: The server has received the data")
            session.handleDataFromClient(data, this)
        }
    }

    fun sendDataToClient(data: Data) {
        output.writeObject(data)
        output.flush()
        output.reset()
    }
}