package clientside.network

import data.Data
import java.io.ObjectInputStream

class Reader(private val server: Server, private val inputStream: ObjectInputStream): Thread() {

    init {
        start()
    }

    override fun run() {
        while (true) {
            val data = inputStream.readObject() as Data
            server.handleDataFromServer(data)
        }
    }
}