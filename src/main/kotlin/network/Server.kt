package network

import sessions.WaitingForNameSession
import java.net.ServerSocket
import java.net.Socket

class Server {
    private val serverSocket: ServerSocket = ServerSocket(80)
    private val waitingForNameSession = WaitingForNameSession()

    fun start() {
        while (true) {
            val socket: Socket = serverSocket.accept()
            waitingForNameSession.addClient(Client(socket, waitingForNameSession))
        }
    }
}