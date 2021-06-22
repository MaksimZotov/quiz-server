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
                if (this::playerName.isInitialized) {
                    log("SERVER: An error occurred while reading the data from the client \"$playerName\"")
                    log("SERVER: Hard removing the client \"$playerName\"")
                } else {
                    log("SERVER: An error occurred while reading the data from an unnamed client")
                    log("SERVER: Hard removing an unnamed client")
                }
                session.handleDataFromClient(HardRemovalOfThePlayer(), this)
                break
            }
        }
    }

    fun sendDataToClient(data: Data) {
        try {
            output.writeObject(data)
            output.flush()
            output.reset()
        } catch (ex: Exception) {
            if (this::playerName.isInitialized) {
                log("SERVER: An error occurred while sending the data to the client \"$playerName\"")
                log("SERVER: Hard removing the client \"$playerName\"")
            } else {
                log("SERVER: An error occurred while sending the data to an unnamed client")
                log("SERVER: Hard removing an unnamed client")
            }
            session.handleDataFromClient(HardRemovalOfThePlayer(), this)
        }
    }
}