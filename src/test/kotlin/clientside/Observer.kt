package clientside

import data.Data

interface Observer {
    fun getData(data: Data)
}