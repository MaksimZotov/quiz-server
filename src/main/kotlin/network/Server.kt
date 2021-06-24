package network

import Logging
import storage.Cleaner
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import sessions.WaitingForNameSession
import java.net.ServerSocket
import java.net.Socket

object Server {
    private val logging = Logging("Server")
    private val log: (text: String) -> Unit = { text -> logging.log(text) }

    private val serverSocket: ServerSocket = ServerSocket(80)

    fun start() {
        log("The server is running")
        Cleaner.start()
        GlobalScope.launch {
            while (true) {
                log("Waiting for a new socket")
                val socket: Socket = serverSocket.accept()
                log("The server has received the socket")
                WaitingForNameSession.addClient(Client(socket, WaitingForNameSession))
            }
        }
    }
}