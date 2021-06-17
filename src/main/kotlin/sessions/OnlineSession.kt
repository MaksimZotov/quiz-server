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
            is Exit -> handleExit(client)
            else -> log("SERVER: Unexpected data for the session OnlineSession")
        }
    }

    fun addClient(client: Client) {
        client.session = this
        clientsWhoIsOnline.add(client)
        whoIsOnline.add(client.playerName)
        nameToClient[client.playerName] = client
        log("SERVER: The client \"${client.playerName}\" has been added to OnlineSession")
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
        log("SERVER: Sending to the client \"${whoIsInvited.playerName}\" Invitation(\"${whoInvited.playerName}\")")
        whoIsInvited.sendDataToClient(Invitation(whoInvited.playerName))
        log("SERVER: The client \"${whoInvited.playerName}\" is waiting for " +
                "AcceptingTheInvitation(\"${whoInvited.playerName}\") from the client \"${whoIsInvited.playerName}\"")
    }

    private fun handleInvitation(invitation: Invitation, client: Client) {
        log("SERVER: The client \"${client.playerName}\" has sent Invitation(\"${invitation.name}\")")
        
        if (!nameToClient.contains(invitation.name)) {
            log("SERVER: The client \"${invitation.name}\" does not exist")
            log("SERVER: Sending to the client \"${client.playerName}\" IncorrectInvitation(\"${invitation.name}\")")
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

                log("SERVER: But the client \"${client.playerName}\" sent " +
                        "Invitation(\"${client.playerName}\") to the client \"${whoWasInvitedByTheClientEarlier.playerName}\" earlier")

                if (whoWasInvitedByTheClientEarlier != whoIsInvited) {

                    log("SERVER: Sending to the client \"${whoWasInvitedByTheClientEarlier.playerName}\" " +
                            "ThePlayerWhoInvitedYouIsWaitingForAcceptingTheInvitationFromAnotherPlayer(\"${client.playerName}\")")

                    whoWasInvitedByTheClientEarlier.sendDataToClient(ThePlayerWhoInvitedYouIsWaitingForAcceptingTheInvitationFromAnotherPlayer(client.playerName))

                    whoInvitedToWhoIsInvited.remove(client)
                    whoIsInvitedToWhoInvited.remove(whoWasInvitedByTheClientEarlier)

                    log("SERVER: From now the client \"${client.playerName}\" does not wait for " +
                            "AcceptingTheInvitation(\"${client.playerName}\") from the client \"${whoWasInvitedByTheClientEarlier.playerName}\"")

                    waitForAcceptingTheInvitation(client, whoIsInvited)

                } else {

                    log("SERVER: Invitation(\"${client.playerName}\") has been sent to the client \"${whoIsInvited.playerName}\" earlier")

                    log("SERVER: The client \"${client.playerName}\" is waiting for " +
                            "AcceptingTheInvitation(\"${client.playerName}\") from the client \"${whoIsInvited.playerName}\"")
                }
            }
            !clientInvitedSomeone && clientIsInvitedBySomeone -> {

                val whoInvitedTheClientEarlier = whoIsInvitedToWhoInvited[client]!!

                if (whoInvitedTheClientEarlier == whoIsInvited) {

                    log("SERVER: But the client \"${whoIsInvited.playerName}\" sent " +
                            "Invitation(\"${whoIsInvited.playerName}\") to the client \"${client.playerName}\" earlier")

                    createGameSession(client, whoIsInvited)

                } else {

                    log("SERVER: But the client \"${whoInvitedTheClientEarlier.playerName}\" sent " +
                            "Invitation(\"${whoInvitedTheClientEarlier.playerName}\") to the client \"${client.playerName}\" earlier")

                    log("SERVER: Sending to the client \"${whoIsInvited.playerName}\" " +
                            "InvitedPlayerIsDecidingWhetherToPlayWithAnotherPlayer(\"${client.playerName}\")")

                    whoIsInvited.sendDataToClient(InvitedPlayerIsDecidingWhetherToPlayWithAnotherPlayer(client.playerName))

                    log("SERVER: The client \"${whoInvitedTheClientEarlier.playerName}\" is waiting for " +
                            "AcceptingTheInvitation(\"${client.playerName}\") from the client \"${client.playerName}\"")
                }
            }
        }
    }

    private fun handleAcceptingTheInvitation(acceptingTheInvitation: AcceptingTheInvitation, client: Client) {
        log("SERVER: The client \"${client.playerName}\" has sent AcceptingTheInvitation(\"${acceptingTheInvitation.name}\")")

        if (!nameToClient.contains(acceptingTheInvitation.name) || !whoIsInvitedToWhoInvited.contains(client) ||
                whoIsInvitedToWhoInvited[client]!!.name != acceptingTheInvitation.name) {

            log("SERVER: The client \"${acceptingTheInvitation.name}\" is not invited by the client \"${client.playerName}\"")

            log("SERVER: Sending to the client \"${client.playerName}\" IncorrectAcceptingTheInvitation(\"${acceptingTheInvitation.name}\")")
            client.sendDataToClient(IncorrectAcceptingTheInvitation(acceptingTheInvitation.name))

        } else {

            val whoInvited = nameToClient[acceptingTheInvitation.name]!!
            createGameSession(whoInvited, client)
        }
    }

    private fun handleRefusalTheInvitation(refusalTheInvitation: RefusalTheInvitation, client: Client) {
        log("SERVER: The client \"${client.playerName}\" has sent RefusalTheInvitation(\"${refusalTheInvitation.name}\")")

        if (!nameToClient.contains(refusalTheInvitation.name) || !whoIsInvitedToWhoInvited.contains(client) ||
                whoIsInvitedToWhoInvited[client]!!.name != refusalTheInvitation.name) {

            log("SERVER: The client \"${refusalTheInvitation.name}\" is not invited by the client \"${client.playerName}\"")

            log("SERVER: Sending to the client \"${client.playerName}\" IncorrectRefusalTheInvitation(\"${refusalTheInvitation.name}\")")
            client.sendDataToClient(IncorrectRefusalTheInvitation(refusalTheInvitation.name))

        } else {

            val whoInvited = nameToClient[refusalTheInvitation.name]!!
            whoInvitedToWhoIsInvited.remove(whoInvited)
            whoIsInvitedToWhoInvited.remove(client)

            log("SERVER: From now the client \"${whoInvited.playerName}\" does not wait for " +
                    "AcceptingTheInvitation(\"${client.playerName}\") from the client \"${client.playerName}\"")
        }
    }

    private fun handleExit(client: Client) {
        client.socket.close()
        whoIsOnline.remove(client.playerName)
        nameToClient.remove(client.playerName)
        clientsWhoIsOnline.remove(client)
        whoInvitedToWhoIsInvited.remove(client)
        whoIsInvitedToWhoInvited.remove(client)
    }
}