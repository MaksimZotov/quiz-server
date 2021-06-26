package sessions

import Logging
import storage.ClientsStorage
import data.*
import network.Client
import java.lang.Exception

object OnlineSession : Session {
    private val logging = Logging("OnlineSession")
    private val log: (text: String) -> Unit = { text -> logging.log(text) }

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
                log("Hard removing the client $client")
                handleHardRemovalOfThePlayer(client)
            }
        }
    }

    fun addClient(client: Client) {
        client.session = this
        clientsWhoIsOnline.add(client)
        whoIsOnline.add(client.name)
        nameToClient[client.name] = client
        log("The client $client has been added to OnlineSession")
    }

    private fun createGameSession(whoInvited: Client, whoIsInvited: Client) {
        log("Creating GameSession for $whoInvited and $whoIsInvited")
        GameSession(this, listOf(whoInvited, whoIsInvited))
        whoInvitedToWhoIsInvited.remove(whoInvited)
        whoIsInvitedToWhoInvited.remove(whoIsInvited)
        whoIsOnline.remove(whoInvited.name)
        whoIsOnline.remove(whoIsInvited.name)
    }

    private fun waitForAcceptingTheInvitation(whoInvited: Client, whoIsInvited: Client) {
        whoInvitedToWhoIsInvited[whoInvited] = whoIsInvited
        whoIsInvitedToWhoInvited[whoIsInvited] = whoInvited
        log("Sending to the client $whoIsInvited Invitation($whoInvited)")
        whoIsInvited.sendDataToClient(Invitation(whoInvited.name))
        log("The client $whoInvited is waiting for " +
                "AcceptingTheInvitation($whoInvited) from the client $whoIsInvited")
    }

    private fun removeClientFromOnlineSession(client: Client) {
        whoIsOnline.remove(client.name)
        nameToClient.remove(client.name)
        clientsWhoIsOnline.remove(client)
        whoInvitedToWhoIsInvited.remove(client)
        whoIsInvitedToWhoInvited.remove(client)
    }

    private fun handleInvitation(invitation: Invitation, client: Client) {
        log("The client $client has sent Invitation(\"${invitation.name}\")")

        if (client.name == invitation.name) {
            log("Sending to the client $client InvitationMyself()")
            client.sendDataToClient(InvitationMyself())
            return
        }
        
        if (!nameToClient.contains(invitation.name)) {
            log("The client \"${invitation.name}\" does not exist")
            log("Sending to the client $client IncorrectInvitation(\"${invitation.name}\")")
            client.sendDataToClient(IncorrectInvitation(invitation.name))
            return
        }
        
        val whoIsInvited = nameToClient[invitation.name]!!

        val clientInvitedSomeone = whoInvitedToWhoIsInvited.contains(client)
        val clientIsInvitedBySomeone = whoIsInvitedToWhoInvited.contains(client)

        when {
            !clientInvitedSomeone && !clientIsInvitedBySomeone -> {
                waitForAcceptingTheInvitation(client, whoIsInvited)
            }
            clientInvitedSomeone && clientIsInvitedBySomeone -> {
                throw Exception(
                        "A situation when the client invited someone and " +
                                "the client is invited by someone is unacceptable"
                )
            }
            clientInvitedSomeone && !clientIsInvitedBySomeone -> {

                val whoWasInvitedByTheClientEarlier = whoInvitedToWhoIsInvited[client]!!

                log("But the client $client sent " +
                        "Invitation($client) to the client $whoWasInvitedByTheClientEarlier earlier")

                if (whoWasInvitedByTheClientEarlier != whoIsInvited) {

                    log("Sending to the client $whoWasInvitedByTheClientEarlier " +
                            "ThePlayerWhoInvitedYouIsWaitingForAcceptingTheInvitationFromAnotherPlayer($client)")

                    whoWasInvitedByTheClientEarlier
                            .sendDataToClient(
                                    ThePlayerWhoInvitedYouIsWaitingForAcceptingTheInvitationFromAnotherPlayer(
                                            client.name
                                    )
                            )

                    whoInvitedToWhoIsInvited.remove(client)
                    whoIsInvitedToWhoInvited.remove(whoWasInvitedByTheClientEarlier)

                    log("From now the client $client does not wait for " +
                            "AcceptingTheInvitation($client) from the client $whoWasInvitedByTheClientEarlier")

                    waitForAcceptingTheInvitation(client, whoIsInvited)

                } else {

                    log("Invitation($client) was sent to the client $whoIsInvited earlier")

                    log("Sending the invitation again...")

                    waitForAcceptingTheInvitation(client, whoIsInvited)
                }
            }
            !clientInvitedSomeone && clientIsInvitedBySomeone -> {

                val whoInvitedTheClientEarlier = whoIsInvitedToWhoInvited[client]!!

                if (whoInvitedTheClientEarlier == whoIsInvited) {

                    log("But the client $whoIsInvited sent " +
                            "Invitation($whoIsInvited) to the client $client earlier")

                    createGameSession(client, whoIsInvited)

                } else {

                    log("But the client \"${whoInvitedTheClientEarlier.name}\" sent " +
                            "Invitation(\"${whoInvitedTheClientEarlier.name}\") to the client $client earlier")

                    log("Sending to the client $whoIsInvited " +
                            "InvitedPlayerIsDecidingWhetherToPlayWithAnotherPlayer($client)")

                    whoInvitedTheClientEarlier.sendDataToClient(
                            InvitedPlayerIsDecidingWhetherToPlayWithAnotherPlayer(client.name)
                    )

                    whoInvitedToWhoIsInvited.remove(whoInvitedTheClientEarlier)
                    whoIsInvitedToWhoInvited.remove(client)

                    log("From now the client \"${whoInvitedTheClientEarlier.name}\" does not wait for " +
                            "AcceptingTheInvitation($client) from the client $client")

                    waitForAcceptingTheInvitation(client, whoIsInvited)
                }
            }
        }
    }

    private fun handleAcceptingTheInvitation(acceptingTheInvitation: AcceptingTheInvitation, client: Client) {
        log("The client $client has sent AcceptingTheInvitation(\"${acceptingTheInvitation.name}\")")

        if (!nameToClient.contains(acceptingTheInvitation.name) || !whoIsInvitedToWhoInvited.contains(client) ||
                whoIsInvitedToWhoInvited[client]!!.name != acceptingTheInvitation.name) {

            log("The client \"${acceptingTheInvitation.name}\" is not invited by the client $client")

            log("Sending to the client $client " +
                    "IncorrectAcceptingTheInvitation(\"${acceptingTheInvitation.name}\")")
            client.sendDataToClient(IncorrectAcceptingTheInvitation(acceptingTheInvitation.name))

        } else {

            val whoInvited = nameToClient[acceptingTheInvitation.name]!!
            createGameSession(whoInvited, client)
        }
    }

    private fun handleRefusalTheInvitation(refusalTheInvitation: RefusalTheInvitation, client: Client) {
        log("The client $client has sent RefusalTheInvitation(\"${refusalTheInvitation.name}\")")

        if (!nameToClient.contains(refusalTheInvitation.name) || !whoIsInvitedToWhoInvited.contains(client) ||
                whoIsInvitedToWhoInvited[client]!!.name != refusalTheInvitation.name) {

            log("The client \"${refusalTheInvitation.name}\" is not invited by the client $client")

            log("Sending to the client $client IncorrectRefusalTheInvitation(\"${refusalTheInvitation.name}\")")
            client.sendDataToClient(IncorrectRefusalTheInvitation(refusalTheInvitation.name))

        } else {

            val whoInvited = nameToClient[refusalTheInvitation.name]!!
            whoInvitedToWhoIsInvited.remove(whoInvited)
            whoIsInvitedToWhoInvited.remove(client)

            log("From now the client $whoInvited does not wait for " +
                    "AcceptingTheInvitation($client) from the client $client")

            log("Sending to the client $whoInvited RefusalTheInvitation($client)")
            whoInvited.sendDataToClient(RefusalTheInvitation(client.name))
        }
    }

    private fun handleNameChange(client: Client) {
        WaitingForNameSession.addClient(client)
        removeClientFromOnlineSession(client)
    }

    private fun handleHardRemovalOfThePlayer(client: Client) {
        client.stop()
        removeClientFromOnlineSession(client)
    }
}