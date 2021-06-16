package tests

import main
import data.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Thread.sleep


class Tests {
    @Test
    fun testAcceptingAndRefusalTheName_1() {
        // Server
        GlobalScope.launch {
            main()
        }

        sleep(1000)

        // Client_1
        GlobalScope.launch {
            val client_1: ClientStub = object : ClientStub() {
                override fun getData(data: Data) {
                    count++
                    when (count) {
                        1 -> {
                            println("CLIENT_1: The client got AcceptingTheName: \"${(data as AcceptingTheName).name}\"")
                            assertEquals(data.name, AcceptingTheName("Test_1").name)
                        }
                    }
                }
            }
            println("CLIENT_1: The client was created")

            println("CLIENT_1: Creating connection")
            client_1.createConnection()

            println("CLIENT_1: Sending the name \"Test_1\"")
            client_1.sendData(Name("Test_1"))
        }

        // Client_2
        GlobalScope.launch {
            val client_2: ClientStub = object : ClientStub() {
                override fun getData(data: Data) {
                    count++
                    when (count) {
                        1 -> {
                            println("CLIENT_2: The client got RefusalTheName: \"${(data as RefusalTheName).name}\"")
                            assertEquals(data.name, AcceptingTheName("Test_1").name)
                        }
                        2 -> {
                            println("CLIENT_2: The client got AcceptingTheName: \"${(data as AcceptingTheName).name}\"")
                            assertEquals(data.name, AcceptingTheName("Test_2").name)
                        }
                    }
                }
            }
            println("CLIENT_2: The client was created")

            println("CLIENT_2: Delay 1 second...")
            delay(1000)

            println("CLIENT_2: Creating connection")
            client_2.createConnection()

            println("CLIENT_2: Sending the name \"Test_1\"")
            client_2.sendData(Name("Test_1"))

            println("CLIENT_2: Delay 1 second...")
            delay(1000)

            println("CLIENT_2: Sending the name \"Test_2\"")
            client_2.sendData(Name("Test_2"))
        }
        sleep(3000)
    }
}

