package clientside

import data.Data

interface Observer {
    fun getData(data: Data)
}

interface Observable {
    fun setObserver(observer: Observer)
}