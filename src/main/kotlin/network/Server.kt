package network

import log
import sessions.WaitingForNameSession
import java.net.ServerSocket
import java.net.Socket

class Server {
    private val serverSocket: ServerSocket = ServerSocket(80)
    private val waitingForNameSession = WaitingForNameSession()

    fun start() {
        log("\nSERVER: The server is running")
        while (true) {
            log("SERVER: Waiting for a new socket")
            val socket: Socket = serverSocket.accept()
            log("SERVER: The server has received the socket")
            waitingForNameSession.addClient(Client(socket, waitingForNameSession))
        }
    }
}