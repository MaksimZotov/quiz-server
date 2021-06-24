package clientside.network

import data.Data
import java.io.ObjectOutputStream

class Sender(private val outputStream: ObjectOutputStream) : Thread() {
    private var runWasLaunchedFromStart = true
    private lateinit var data: Data

    init {
        start()
    }

    override fun run() {
        if (runWasLaunchedFromStart) {
            runWasLaunchedFromStart = false
            return
        }
        outputStream.writeObject(data)
        outputStream.flush()
    }

    fun sendDataToServer(data: Data) {
        this.data = data
        run()
    }
}