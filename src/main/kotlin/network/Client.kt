package network

import Logging
import data.Data
import data.HardRemovalOfThePlayer
import data.Ping
import data.Pong
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import sessions.Session
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

class Client(val socket: Socket, var session: Session) {
    private val logging = Logging("Client")
    private val log: (text: String) -> Unit = { text -> logging.log(text) }

    private val thisClient = this

    lateinit var name: String

    private lateinit var input: ObjectInputStream
    private lateinit var output: ObjectOutputStream

    var receivedPong = true

    init {
        try {
            input = ObjectInputStream(socket.getInputStream())
            output = ObjectOutputStream(socket.getOutputStream())
        } catch(ex: IOException) {
            log("An error occurred during initialization of the client $thisClient")
            log("Hard removing the client $thisClient")
            session.handleDataFromClient(HardRemovalOfThePlayer(), this)
        }

        GlobalScope.launch {
            while (true) {
                try {
                    val data = input.readObject() as Data
                    log("The server has received the data from the client $thisClient")
                    if (data is Pong) {
                        log("The client $thisClient has sent Pong()")
                        receivedPong = true
                        continue
                    }
                    session.handleDataFromClient(data, thisClient)
                } catch (ex: Exception) {
                    log("An error occurred while reading the data from the client $thisClient")
                    log("Hard removing the client $thisClient")
                    session.handleDataFromClient(HardRemovalOfThePlayer(), thisClient)
                    break
                }
            }
        }
    }

    fun sendDataToClient(data: Data) {
        try {
            output.writeObject(data)
            output.flush()
            output.reset()
        } catch (ex: Exception) {
            log("An error occurred while sending the data to the client $thisClient")
            log("Hard removing the client $thisClient")
            session.handleDataFromClient(HardRemovalOfThePlayer(), this)
        }
    }

    fun sendPing() {
        receivedPong = false
        sendDataToClient(Ping())
    }

    override fun toString(): String {
        return if (this::name.isInitialized) "\"$name\"" else "\"${super.toString()}\""
    }
}