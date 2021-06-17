package network

import sessions.WaitingForNameSession
import java.net.ServerSocket
import java.net.Socket

class Server {
    private val serverSocket: ServerSocket = ServerSocket(80)
    private val waitingForNameSession = WaitingForNameSession()

    fun start() {
        println("\nSERVER: The server is running")
        while (true) {
            println("SERVER: Waiting for a new socket")
            val socket: Socket = serverSocket.accept()
            println("SERVER: The server has received the socket")
            waitingForNameSession.addClient(Client(socket, waitingForNameSession))
        }
    }
}