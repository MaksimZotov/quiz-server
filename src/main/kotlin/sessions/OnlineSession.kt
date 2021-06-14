package sessions

import common.NamesStorage
import data.AcceptingTheInvitation
import data.Data
import data.Exit
import data.Invitation
import network.Client
import java.lang.Exception

class OnlineSession(): Session {
    private val whoIsOnline = NamesStorage.whoIsOnline
    private val nameToClient = NamesStorage.nameToClient

    private val clientsWhoIsOnline = mutableSetOf<Client>()

    private val whoInvitedToWhoIsInvited = mutableMapOf<Client, Client>()

    override fun handleDataFromClient(data: Data, client: Client) {
        when (data) {
            is Invitation -> {
                val invitation = data
                val whoInvited = client
                val whoIsInvited = nameToClient[invitation.whoIsInvited] ?:
                    throw Exception("The map nameToClient must contains who is invited")

                if (!whoInvitedToWhoIsInvited.contains(whoInvited) &&
                        !whoInvitedToWhoIsInvited.values.contains(whoIsInvited)) {
                    whoInvitedToWhoIsInvited[whoInvited] = whoIsInvited
                }
            }
            is AcceptingTheInvitation -> {
                val acceptingTheInvitation = data
                val whoIsInvited = client
                val whoInvited = nameToClient[acceptingTheInvitation.whoInvited] ?:
                    throw Exception("The map nameToClient must contains who invited")

                if (whoInvitedToWhoIsInvited.contains(whoInvited) &&
                        whoInvitedToWhoIsInvited[whoInvited] == whoIsInvited) {
                    GameSession(this, whoInvited, whoIsInvited)
                    whoIsOnline.remove(whoInvited.name)
                    whoIsOnline.remove(whoIsInvited.name)
                }
            }
            is Exit -> {
                TODO()
            }
        }
    }

    fun addClient(client: Client) {
        client.session = this
        clientsWhoIsOnline.add(client)
        whoIsOnline.add(client.name)
        nameToClient[client.name] = client
    }
}