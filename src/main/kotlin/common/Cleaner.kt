package common

import data.HardRemovalOfThePlayer
import data.Ping
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import log

object Cleaner {
    val timeDelay = 10000.toLong()
    val hardRemovalOfThePlayer = HardRemovalOfThePlayer()

    fun start() {
        log("The cleaner is running")
        GlobalScope.launch {
            while (true) {
                val clients = ClientsStorage.nameToClient.values + ClientsStorage.clientsWithoutName
                clients.forEach { it.sendPing() }
                delay(timeDelay)
                clients.forEach {
                    if (!it.receivedPong) {
                        it.session.handleDataFromClient(hardRemovalOfThePlayer, it)
                    }
                }
            }
        }
    }
}