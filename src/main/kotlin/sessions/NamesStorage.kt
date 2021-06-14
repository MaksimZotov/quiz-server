package sessions

import network.Client

object NamesStorage {
    val whoIsOnline = mutableSetOf<String>()
    val whoIsInTheGame = mutableSetOf<String>()
    val nameToClient = mutableMapOf<String, Client>()
}