package sessions

import common.NamesStorage
import data.*
import log
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
            else -> log("SERVER: Unexpected data for the session OnlineSession")
        }
    }

    fun addClient(client: Client) {
        client.session = this
        clientsWhoIsOnline.add(client)
        whoIsOnline.add(client.playerName)
        nameToClient[client.playerName] = client
        log("SERVER: The client with name \"${client.playerName}\" has been added to OnlineSession")
    }

    private fun createGameSession(whoInvited: Client, whoIsInvited: Client) {
        log("SERVER: Creating GameSession for \"${whoInvited.playerName}\" and \"${whoIsInvited.playerName}\"")
        GameSession(this, whoInvited, whoIsInvited)
        whoInvitedToWhoIsInvited.remove(whoInvited)
        whoIsInvitedToWhoInvited.remove(whoIsInvited)
        whoIsOnline.remove(whoInvited.playerName)
        whoIsOnline.remove(whoIsInvited.playerName)
    }

    private fun waitForAcceptingTheInvitation(whoInvited: Client, whoIsInvited: Client) {
        whoInvitedToWhoIsInvited[whoInvited] = whoIsInvited
        whoIsInvitedToWhoInvited[whoIsInvited] = whoInvited
        log("SERVER: Sending to the client with name \"${whoIsInvited.playerName}\" Invitation(\"${whoInvited.playerName}\")")
        whoIsInvited.sendDataToClient(Invitation(whoInvited.playerName))
        log("SERVER: The client with name \"${whoInvited.playerName}\" is waiting for " +
                "AcceptingTheInvitation(\"${whoInvited.playerName}\") from the client with name \"${whoIsInvited.playerName}\"")
    }

    private fun handleInvitation(invitation: Invitation, client: Client) {
        log("SERVER: The client with name \"${client.playerName}\" has sent Invitation(\"${invitation.name}\")")
        
        if (!nameToClient.contains(invitation.name)) {
            log("SERVER: The client with name \"${invitation.name}\" does not exist")
            log("SERVER: Sending to the client with name \"${client.playerName}\" IncorrectInvitation(\"${invitation.name}\")")
            client.sendDataToClient(IncorrectInvitation(invitation.name))
            return
        }
        
        val whoIsInvited = nameToClient[invitation.name]!!

        val clientInvitedSomeone = whoInvitedToWhoIsInvited.contains(client)
        val clientIsInvitedBySomeone = whoIsInvitedToWhoInvited.contains(whoIsInvited)

        when {
            !clientInvitedSomeone && !clientIsInvitedBySomeone -> {
                waitForAcceptingTheInvitation(client, whoIsInvited)
            }
            clientInvitedSomeone && clientIsInvitedBySomeone -> {
                throw Exception("A situation when the client invited someone and the client is invited by someone is unacceptable")
            }
            clientInvitedSomeone && !clientIsInvitedBySomeone -> {

                val whoWasInvitedByTheClientEarlier = whoInvitedToWhoIsInvited[client]!!

                log("SERVER: But the client with name \"${client.playerName}\" sent " +
                        "Invitation(\"${client.playerName}\") to the client with name \"${whoWasInvitedByTheClientEarlier.playerName}\" earlier")

                if (whoWasInvitedByTheClientEarlier != whoIsInvited) {

                    log("SERVER: Sending to the client with name \"${whoWasInvitedByTheClientEarlier.playerName}\" " +
                            "ThePlayerWhoInvitedYouIsWaitingForAcceptingTheInvitationFromAnotherPlayer(\"${client.playerName}\")")

                    whoWasInvitedByTheClientEarlier.sendDataToClient(ThePlayerWhoInvitedYouIsWaitingForAcceptingTheInvitationFromAnotherPlayer(client.playerName))

                    whoInvitedToWhoIsInvited.remove(client)
                    whoIsInvitedToWhoInvited.remove(whoWasInvitedByTheClientEarlier)

                    log("SERVER: From now the client with name \"${client.playerName}\" does not wait for " +
                            "AcceptingTheInvitation(\"${client.playerName}\") from the client with name \"${whoWasInvitedByTheClientEarlier.playerName}\"")

                    waitForAcceptingTheInvitation(client, whoIsInvited)

                } else {

                    log("SERVER: Invitation(\"${client.playerName}\") has been sent to the client with name \"${whoIsInvited.playerName}\" earlier")

                    log("SERVER: The client with name \"${client.playerName}\" is waiting for " +
                            "AcceptingTheInvitation(\"${client.playerName}\") from the client with name \"${whoIsInvited.playerName}\"")
                }
            }
            !clientInvitedSomeone && clientIsInvitedBySomeone -> {

                val whoInvitedTheClientEarlier = whoIsInvitedToWhoInvited[client]!!

                if (whoInvitedTheClientEarlier == whoIsInvited) {

                    log("SERVER: But the client with name \"${whoIsInvited.playerName}\" sent " +
                            "Invitation(\"${whoIsInvited.playerName}\") to the client with name \"${client.playerName}\" earlier")

                    createGameSession(client, whoIsInvited)

                } else {

                    log("SERVER: But the client with name \"${whoInvitedTheClientEarlier.playerName}\" sent " +
                            "Invitation(\"${whoInvitedTheClientEarlier.playerName}\") to the client with name \"${client.playerName}\" earlier")

                    log("SERVER: Sending to the client with name \"${whoInvitedTheClientEarlier.playerName}\" " +
                            "InvitedPlayerIsWaitingForAcceptingTheInvitationFromAnotherPlayer(\"${client.playerName}\")")

                    whoInvitedTheClientEarlier.sendDataToClient(InvitedPlayerIsWaitingForAcceptingTheInvitationFromAnotherPlayer(client.playerName))

                    whoInvitedToWhoIsInvited.remove(whoInvitedTheClientEarlier)
                    whoIsInvitedToWhoInvited.remove(client)

                    log("SERVER: From now the client with name \"${whoInvitedTheClientEarlier.playerName}\" does not wait for " +
                            "AcceptingTheInvitation(\"${client.playerName}\") from the client with name \"${client.playerName}\"")

                    waitForAcceptingTheInvitation(client, whoIsInvited)
                }
            }
        }
    }

    private fun handleAcceptingTheInvitation(acceptingTheInvitation: AcceptingTheInvitation, client: Client) {
        val whoIsInvited = client
        val whoInvited = nameToClient[acceptingTheInvitation.name] ?:
            throw Exception("The map nameToClient must contains who invited")

        log("SERVER: The client with name \"${whoInvited.playerName}\" has sent AcceptingTheInvitation(\"${whoIsInvited.playerName}\")")

        if (whoInvitedToWhoIsInvited.contains(whoInvited) && whoInvitedToWhoIsInvited[whoInvited] == whoIsInvited) {
            createGameSession(whoInvited, whoIsInvited)
        }
    }

    private fun handleRefusalTheInvitation(refusalTheInvitation: RefusalTheInvitation, client: Client) {
        TODO()
    }

    private fun handleExit(exit: Exit, client: Client) {
        client.socket.close()
        whoIsOnline.remove(client.playerName)
        nameToClient.remove(client.playerName)
        clientsWhoIsOnline.remove(client)
        whoInvitedToWhoIsInvited.filter { entry -> entry.key != client && entry.value != client }
        whoIsInvitedToWhoInvited.filter { entry -> entry.key != client && entry.value != client }
    }
}