package clientside

import data.Data

class ReceiverFromServer(private val client: Observer) {
    fun getData(data: Data) {
        client.getData(data)
    }
}