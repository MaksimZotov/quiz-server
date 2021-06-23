package network

import common.Cleaner
import log
import sessions.WaitingForNameSession
import java.net.ServerSocket
import java.net.Socket

object Server {
    private val serverSocket: ServerSocket = ServerSocket(80)

    fun start() {
        log("The server is running")
        Cleaner.start()
        while (true) {
            log("Waiting for a new socket")
            val socket: Socket = serverSocket.accept()
            log("The server has received the socket")
            WaitingForNameSession.addClient(Client(socket, WaitingForNameSession))
        }
    }
}