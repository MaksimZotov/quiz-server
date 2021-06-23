package network

import data.Data
import data.HardRemovalOfThePlayer
import data.Ping
import data.Pong
import log
import sessions.Session
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

class Client(val socket: Socket, var session: Session) : Thread() {
    private val input: ObjectInputStream = ObjectInputStream(socket.getInputStream())
    private val output: ObjectOutputStream = ObjectOutputStream(socket.getOutputStream())

    var receivedPong = true

    lateinit var playerName: String

    init {
        start()
    }

    override fun run() {
        while (true) {
            try {
                val data = input.readObject() as Data
                log("The server has received the data")
                if (data is Pong) {
                    receivedPong = true
                    continue
                }
                session.handleDataFromClient(data, this)
            } catch (ex: Exception) {
                if (this::playerName.isInitialized) {
                    log("An error occurred while reading the data from the client \"$playerName\"")
                    log("Hard removing the client \"$playerName\"")
                } else {
                    log("An error occurred while reading the data from an unnamed client")
                    log("Hard removing an unnamed client")
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
                log("An error occurred while sending the data to the client \"$playerName\"")
                log("Hard removing the client \"$playerName\"")
            } else {
                log("An error occurred while sending the data to an unnamed client")
                log("Hard removing an unnamed client")
            }
            session.handleDataFromClient(HardRemovalOfThePlayer(), this)
        }
    }

    fun sendPing() {
        receivedPong = false
        try {
            output.writeObject(Ping())
            output.flush()
            output.reset()
        } catch (ex: Exception) {
            if (this::playerName.isInitialized) {
                log("An error occurred while sending the data to the client \"$playerName\"")
                log("Hard removing the client \"$playerName\"")
            } else {
                log("An error occurred while sending the data to an unnamed client")
                log("Hard removing an unnamed client")
            }
            session.handleDataFromClient(HardRemovalOfThePlayer(), this)
        }
    }
}