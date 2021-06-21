package network

import data.Data
import data.HardRemovalOfThePlayer
import log
import sessions.Session
import java.io.EOFException
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
            try {
                val data = input.readObject() as Data
                log("SERVER: The server has received the data")
                session.handleDataFromClient(data, this)
            } catch (ex: Exception) {
                log("SERVER: The server has received the incorrect data")
                if (this::playerName.isInitialized) {
                    log("SERVER: Hard removing the client \"$playerName\"")
                } else {
                    log("SERVER: Hard removing an unnamed client")
                }
                session.handleDataFromClient(HardRemovalOfThePlayer(), this)
            }
        }
    }

    fun sendDataToClient(data: Data) {
        output.writeObject(data)
        output.flush()
        output.reset()
    }
}