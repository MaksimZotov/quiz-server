package storage

import network.Client

object ClientsStorage {
    val clientsWithoutName = mutableSetOf<Client>()
    val nameToClient = mutableMapOf<String, Client>()
    val whoIsOnline = mutableSetOf<String>()
    val whoIsInTheGame = mutableSetOf<String>()
}