package sessions

import common.ClientsStorage
import data.*
import log
import network.Client
import java.lang.Exception

object OnlineSession : Session {
    private val whoIsOnline = ClientsStorage.whoIsOnline
    private val nameToClient = ClientsStorage.nameToClient

    private val clientsWhoIsOnline = mutableSetOf<Client>()

    private val whoInvitedToWhoIsInvited = mutableMapOf<Client, Client>()
    private val whoIsInvitedToWhoInvited = mutableMapOf<Client, Client>()


    override fun handleDataFromClient(data: Data, client: Client) {
        when (data) {
            is Invitation -> handleInvitation(data, client)
            is AcceptingTheInvitation -> handleAcceptingTheInvitation(data, client)
            is RefusalTheInvitation -> handleRefusalTheInvitation(data, client)
            is NameChange -> handleNameChange(client)
            is HardRemovalOfThePlayer -> handleHardRemovalOfThePlayer(client)
            else -> {
                log("Unexpected data for the session OnlineSession")
                log("Hard removing the client \"${client.playerName}\"")
                handleHardRemovalOfThePlayer(client)
            }
        }
    }

    fun addClient(client: Client) {
        client.session = this
        clientsWhoIsOnline.add(client)
        whoIsOnline.add(client.playerName)
        nameToClient[client.playerName] = client
        log("The client \"${client.playerName}\" has been added to OnlineSession")
    }

    private fun createGameSession(whoInvited: Client, whoIsInvited: Client) {
        log("Creating GameSession for \"${whoInvited.playerName}\" and \"${whoIsInvited.playerName}\"")
        GameSession(this, whoInvited, whoIsInvited)
        whoInvitedToWhoIsInvited.remove(whoInvited)
        whoIsInvitedToWhoInvited.remove(whoIsInvited)
        whoIsOnline.remove(whoInvited.playerName)
        whoIsOnline.remove(whoIsInvited.playerName)
    }

    private fun waitForAcceptingTheInvitation(whoInvited: Client, whoIsInvited: Client) {
        whoInvitedToWhoIsInvited[whoInvited] = whoIsInvited
        whoIsInvitedToWhoInvited[whoIsInvited] = whoInvited
        log("Sending to the client \"${whoIsInvited.playerName}\" Invitation(\"${whoInvited.playerName}\")")
        whoIsInvited.sendDataToClient(Invitation(whoInvited.playerName))
        log("The client \"${whoInvited.playerName}\" is waiting for " +
                "AcceptingTheInvitation(\"${whoInvited.playerName}\") from the client \"${whoIsInvited.playerName}\"")
    }

    private fun removeClientFromOnlineSession(client: Client) {
        whoIsOnline.remove(client.playerName)
        nameToClient.remove(client.playerName)
        clientsWhoIsOnline.remove(client)
        whoInvitedToWhoIsInvited.remove(client)
        whoIsInvitedToWhoInvited.remove(client)
    }

    private fun handleInvitation(invitation: Invitation, client: Client) {
        log("The client \"${client.playerName}\" has sent Invitation(\"${invitation.name}\")")

        if (client.playerName == invitation.name) {
            log("Sending to the client \"${client.playerName}\" InvitationMyself()")
            client.sendDataToClient(InvitationMyself())
            return
        }
        
        if (!nameToClient.contains(invitation.name)) {
            log("The client \"${invitation.name}\" does not exist")
            log("Sending to the client \"${client.playerName}\" IncorrectInvitation(\"${invitation.name}\")")
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

                log("But the client \"${client.playerName}\" sent " +
                        "Invitation(\"${client.playerName}\") to the client \"${whoWasInvitedByTheClientEarlier.playerName}\" earlier")

                if (whoWasInvitedByTheClientEarlier != whoIsInvited) {

                    log("Sending to the client \"${whoWasInvitedByTheClientEarlier.playerName}\" " +
                            "ThePlayerWhoInvitedYouIsWaitingForAcceptingTheInvitationFromAnotherPlayer(\"${client.playerName}\")")

                    whoWasInvitedByTheClientEarlier.sendDataToClient(ThePlayerWhoInvitedYouIsWaitingForAcceptingTheInvitationFromAnotherPlayer(client.playerName))

                    whoInvitedToWhoIsInvited.remove(client)
                    whoIsInvitedToWhoInvited.remove(whoWasInvitedByTheClientEarlier)

                    log("From now the client \"${client.playerName}\" does not wait for " +
                            "AcceptingTheInvitation(\"${client.playerName}\") from the client \"${whoWasInvitedByTheClientEarlier.playerName}\"")

                    waitForAcceptingTheInvitation(client, whoIsInvited)

                } else {

                    log("Invitation(\"${client.playerName}\") has been sent to the client \"${whoIsInvited.playerName}\" earlier")

                    log("The client \"${client.playerName}\" is waiting for " +
                            "AcceptingTheInvitation(\"${client.playerName}\") from the client \"${whoIsInvited.playerName}\"")
                }
            }
            !clientInvitedSomeone && clientIsInvitedBySomeone -> {

                val whoInvitedTheClientEarlier = whoIsInvitedToWhoInvited[client]!!

                if (whoInvitedTheClientEarlier == whoIsInvited) {

                    log("But the client \"${whoIsInvited.playerName}\" sent " +
                            "Invitation(\"${whoIsInvited.playerName}\") to the client \"${client.playerName}\" earlier")

                    createGameSession(client, whoIsInvited)

                } else {

                    log("But the client \"${whoInvitedTheClientEarlier.playerName}\" sent " +
                            "Invitation(\"${whoInvitedTheClientEarlier.playerName}\") to the client \"${client.playerName}\" earlier")

                    log("Sending to the client \"${whoIsInvited.playerName}\" " +
                            "InvitedPlayerIsDecidingWhetherToPlayWithAnotherPlayer(\"${client.playerName}\")")

                    whoIsInvited.sendDataToClient(InvitedPlayerIsDecidingWhetherToPlayWithAnotherPlayer(client.playerName))

                    log("The client \"${whoInvitedTheClientEarlier.playerName}\" is waiting for " +
                            "AcceptingTheInvitation(\"${client.playerName}\") from the client \"${client.playerName}\"")
                }
            }
        }
    }

    private fun handleAcceptingTheInvitation(acceptingTheInvitation: AcceptingTheInvitation, client: Client) {
        log("The client \"${client.playerName}\" has sent AcceptingTheInvitation(\"${acceptingTheInvitation.name}\")")

        if (!nameToClient.contains(acceptingTheInvitation.name) || !whoIsInvitedToWhoInvited.contains(client) ||
                whoIsInvitedToWhoInvited[client]!!.playerName != acceptingTheInvitation.name) {

            log("The client \"${acceptingTheInvitation.name}\" is not invited by the client \"${client.playerName}\"")

            log("Sending to the client \"${client.playerName}\" IncorrectAcceptingTheInvitation(\"${acceptingTheInvitation.name}\")")
            client.sendDataToClient(IncorrectAcceptingTheInvitation(acceptingTheInvitation.name))

        } else {

            val whoInvited = nameToClient[acceptingTheInvitation.name]!!
            createGameSession(whoInvited, client)
        }
    }

    private fun handleRefusalTheInvitation(refusalTheInvitation: RefusalTheInvitation, client: Client) {
        log("The client \"${client.playerName}\" has sent RefusalTheInvitation(\"${refusalTheInvitation.name}\")")

        if (!nameToClient.contains(refusalTheInvitation.name) || !whoIsInvitedToWhoInvited.contains(client) ||
                whoIsInvitedToWhoInvited[client]!!.playerName != refusalTheInvitation.name) {

            log("The client \"${refusalTheInvitation.name}\" is not invited by the client \"${client.playerName}\"")

            log("Sending to the client \"${client.playerName}\" IncorrectRefusalTheInvitation(\"${refusalTheInvitation.name}\")")
            client.sendDataToClient(IncorrectRefusalTheInvitation(refusalTheInvitation.name))

        } else {

            val whoInvited = nameToClient[refusalTheInvitation.name]!!
            whoInvitedToWhoIsInvited.remove(whoInvited)
            whoIsInvitedToWhoInvited.remove(client)

            log("From now the client \"${whoInvited.playerName}\" does not wait for " +
                    "AcceptingTheInvitation(\"${client.playerName}\") from the client \"${client.playerName}\"")

            log("Sending to the client \"${whoInvited.playerName}\" RefusalTheInvitation(\"${client.name}\")")
            whoInvited.sendDataToClient(RefusalTheInvitation(client.playerName))
        }
    }

    private fun handleNameChange(client: Client) {
        WaitingForNameSession.addClient(client)
        removeClientFromOnlineSession(client)
    }

    private fun handleHardRemovalOfThePlayer(client: Client) {
        client.socket.close()
        removeClientFromOnlineSession(client)
    }
}