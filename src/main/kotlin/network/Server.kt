package network

import log
import sessions.WaitingForNameSession
import java.net.ServerSocket
import java.net.Socket

object Server {
    private val serverSocket: ServerSocket = ServerSocket(80)

    fun start() {
        log("SERVER: The server is running")
        while (true) {
            log("SERVER: Waiting for a new socket")
            val socket: Socket = serverSocket.accept()
            log("SERVER: The server has received the socket")
            WaitingForNameSession.addClient(Client(socket, WaitingForNameSession))
        }
    }
}