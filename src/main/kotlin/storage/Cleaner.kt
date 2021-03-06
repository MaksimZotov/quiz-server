package storage

import Logging
import data.HardRemovalOfThePlayer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object Cleaner {
    private val logging = Logging("Cleaner")
    private val log: (text: String) -> Unit = { text -> logging.log(text) }

    val timeDelay = 10000.toLong()
    val hardRemovalOfThePlayer = HardRemovalOfThePlayer()

    fun start() {
        log("The cleaner is running")
        GlobalScope.launch {
            while (true) {
                val clientsBefore = ClientsStorage.nameToClient.values + ClientsStorage.clientsWithoutName
                clientsBefore.forEach { it.sendPing() }
                delay(timeDelay)
                val clientsAfter = ClientsStorage.nameToClient.values + ClientsStorage.clientsWithoutName
                clientsAfter.forEach {
                    if (!it.receivedPong) {
                        log("Removing the client $it")
                        it.session.handleDataFromClient(hardRemovalOfThePlayer, it)
                    }
                }
            }
        }
    }
}