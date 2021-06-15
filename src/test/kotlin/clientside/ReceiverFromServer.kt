package clientside

import data.Data

object ReceiverFromServer : Observable {
    private lateinit var currentObserver: Observer

    override fun setObserver(observer: Observer) {
        currentObserver = observer
    }

    fun getData(data: Data) {
        currentObserver.getData(data)
    }
}