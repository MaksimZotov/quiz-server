package sessions

import data.Data
import network.Client

interface Session {
    fun handleDataFromClient(data: Data, client: Client)
}