package sessions

import common.NamesStorage
import data.*
import network.Client
import java.lang.Exception

class OnlineSession(): Session {
    private val whoIsOnline = NamesStorage.whoIsOnline
    private val nameToClient = NamesStorage.nameToClient

    private val clientsWhoIsOnline = mutableSetOf<Client>()

    private val whoInvitedToWhoIsInvited = mutableMapOf<Client, Client>()
    private val whoIsInvitedToWhoInvited = mutableMapOf<Client, Client>()


    override fun handleDataFromClient(data: Data, client: Client) {
        when (data) {
            is Invitation -> handleInvitation(data, client)
            is AcceptingTheInvitation -> handleAcceptingTheInvitation(data, client)
            is RefusalTheInvitation -> handleRefusalTheInvitation(data, client)
            is Exit -> handleExit(data, client)
            else -> println("SERVER: Unexpected data for the session OnlineSession")
        }
    }

    fun addClient(client: Client) {
        client.session = this
        clientsWhoIsOnline.add(client)
        whoIsOnline.add(client.name)
        nameToClient[client.name] = client
        println("SERVER: The client with name \"${client.name}\" has been added to OnlineSession")
    }

    private fun handleInvitation(invitation: Invitation, client: Client) {
        val whoInvited = client
        val whoIsInvited = nameToClient[invitation.name] ?:
            throw Exception("The map nameToClient must contains who is invited")

        println("SERVER: The client with name \"${whoInvited.name}\" has sent Invitation(\"${whoIsInvited.name}\")")

        val clientInvitedSomeone = whoInvitedToWhoIsInvited.contains(whoInvited)
        val clientIsInvitedBySomeone = whoIsInvitedToWhoInvited.contains(whoIsInvited)

        when {
            !clientInvitedSomeone && !clientIsInvitedBySomeone -> {
                whoInvitedToWhoIsInvited[whoInvited] = whoIsInvited
                whoIsInvitedToWhoInvited[whoIsInvited] = whoInvited
                println("SERVER: Sending to the client with name \"${whoIsInvited.name}\" Invitation(\"${whoInvited.name}\")")
                whoIsInvited.sendDataToClient(Invitation(whoInvited.name))
                println("SERVER: The client with name \"${whoInvited.name}\" is waiting for " +
                        "AcceptingTheInvitation(\"${whoInvited.name}\") from the client with name \"${whoIsInvited.name}\"")
            }
            clientInvitedSomeone && clientIsInvitedBySomeone -> {
                if (whoInvitedToWhoIsInvited[whoInvited] == whoIsInvited &&
                        whoInvitedToWhoIsInvited[whoIsInvited] == client) {
                    GameSession(this, whoInvited, whoIsInvited)
                    whoIsOnline.remove(whoInvited.name)
                    whoIsOnline.remove(whoIsInvited.name)
                } else {
                    whoInvitedToWhoIsInvited[whoInvited] = whoIsInvited
                    whoIsInvitedToWhoInvited.remove(whoIsInvited)
                    whoIsInvitedToWhoInvited[whoIsInvited] = whoInvited
                }
            }
            clientInvitedSomeone && !clientIsInvitedBySomeone -> {
                whoInvitedToWhoIsInvited[whoInvited] = whoIsInvited
                whoIsInvitedToWhoInvited.remove(whoIsInvited)
                whoIsInvitedToWhoInvited[whoIsInvited] = whoInvited
            }
            !clientInvitedSomeone && clientIsInvitedBySomeone -> {
                if (whoInvitedToWhoIsInvited[whoInvited] == whoIsInvited &&
                        whoInvitedToWhoIsInvited[whoIsInvited] == client) {
                    GameSession(this, whoInvited, whoIsInvited)
                    whoIsOnline.remove(whoInvited.name)
                    whoIsOnline.remove(whoIsInvited.name)
                } else {
                    whoInvitedToWhoIsInvited[whoInvited] = whoIsInvited
                    whoIsInvitedToWhoInvited.remove(whoIsInvited)
                    whoIsInvitedToWhoInvited[whoIsInvited] = whoInvited
                }
            }
        }
    }

    private fun handleAcceptingTheInvitation(acceptingTheInvitation: AcceptingTheInvitation, client: Client) {
        val whoIsInvited = client
        val whoInvited = nameToClient[acceptingTheInvitation.name] ?:
            throw Exception("The map nameToClient must contains who invited")

        println("SERVER: The client with name \"${whoInvited.name}\" has sent AcceptingTheInvitation(\"${whoIsInvited.name}\")")

        if (whoInvitedToWhoIsInvited.contains(whoInvited) &&
                whoInvitedToWhoIsInvited[whoInvited] == whoIsInvited) {
            println("SERVER: Creating GameSession for \"${whoInvited.name}\" and \"${whoIsInvited.name}\"")
            GameSession(this, whoInvited, whoIsInvited)
            whoIsOnline.remove(whoInvited.name)
            whoIsOnline.remove(whoIsInvited.name)
        }
    }

    private fun handleRefusalTheInvitation(refusalTheInvitation: RefusalTheInvitation, client: Client) {
        TODO()
    }

    private fun handleExit(exit: Exit, client: Client) {
        client.socket.close()
        whoIsOnline.remove(client.name)
        nameToClient.remove(client.name)
        clientsWhoIsOnline.remove(client)
        whoInvitedToWhoIsInvited.filter { entry -> entry.key != client && entry.value != client }
        whoIsInvitedToWhoInvited.filter { entry -> entry.key != client && entry.value != client }
    }
}