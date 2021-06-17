package tests

import main
import data.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.jupiter.api.BeforeEach
import java.lang.Thread.sleep


class Tests {
    @BeforeEach
    fun runTheServer() {
        GlobalScope.launch {
            main()
        }
        sleep(1000)
    }

    @Test
    fun testAcceptingAndRefusalTheName_1() {
        // Client_1
        GlobalScope.launch {
            val client_1: ClientStub = object : ClientStub() {
                override fun getData(data: Data) {
                    count++
                    when (count) {
                        1 -> {
                            assert(data is AcceptingTheName)
                            println("CLIENT_1: The client has received AcceptingTheName: \"${(data as AcceptingTheName).name}\"")
                            assertEquals(data.name, AcceptingTheName("Test_1").name)
                        }
                    }
                }
            }
            println("CLIENT_1: The client has been created")

            println("CLIENT_1: Creating connection")
            client_1.createConnection()

            println("CLIENT_1: Sending Name(\"Test_1\")")
            client_1.sendData(Name("Test_1"))
        }

        // Client_2
        GlobalScope.launch {
            val client_2: ClientStub = object : ClientStub() {
                override fun getData(data: Data) {
                    count++
                    when (count) {
                        1 -> {
                            assert(data is RefusalTheName)
                            println("CLIENT_2: The client has received RefusalTheName(\"${(data as RefusalTheName).name}\")")
                            assertEquals(data.name, AcceptingTheName("Test_1").name)
                        }
                        2 -> {
                            assert(data is AcceptingTheName)
                            println("CLIENT_2: The client has received AcceptingTheName(\"${(data as AcceptingTheName).name}\")")
                            assertEquals(data.name, AcceptingTheName("Test_2").name)
                        }
                    }
                }
            }
            println("CLIENT_2: The client has been created")

            println("CLIENT_2: Delay 1 second...")
            delay(1000)

            println("CLIENT_2: Creating connection")
            client_2.createConnection()

            println("CLIENT_2: Sending Name(\"Test_1\")")
            client_2.sendData(Name("Test_1"))

            println("CLIENT_2: Delay 1 second...")
            delay(1000)

            println("CLIENT_2: Sending Name(\"Test_2\")")
            client_2.sendData(Name("Test_2"))
        }
        sleep(3000)
    }

    @Test
    fun testPlayTheGame_1() {
        // Client_1
        GlobalScope.launch {
            val client_1: ClientStub = object : ClientStub() {
                override fun getData(data: Data) {
                    count++
                    when (count) {
                        1 -> {
                            assert(data is AcceptingTheName)
                            println("CLIENT_1: The client has received AcceptingTheName(\"${(data as AcceptingTheName).name}\")")
                            assertEquals(data.name, AcceptingTheName("Test_1").name)
                        }
                        2 -> {
                            assert(data is PlayTheGame)
                            println("CLIENT_1: The client has received PlayTheGame()")
                        }
                    }
                }
            }
            println("CLIENT_1: The client has been created")

            println("CLIENT_1: Creating connection")
            client_1.createConnection()

            println("CLIENT_1: Sending Name(\"Test_1\")")
            client_1.sendData(Name("Test_1"))

            println("CLIENT_1: Delay 1.5 seconds...")
            delay(1500)

            println("CLIENT_1: Sending Invitation(\"Test_2\")")
            client_1.sendData(Invitation("Test_2"))
        }

        // Client_2
        GlobalScope.launch {
            val client_2: ClientStub = object : ClientStub() {
                override fun getData(data: Data) {
                    count++
                    when (count) {
                        1 -> {
                            assert(data is AcceptingTheName)
                            println("CLIENT_2: The client has received AcceptingTheName(\"${(data as AcceptingTheName).name}\")")
                            assertEquals(data.name, AcceptingTheName("Test_2").name)
                        }
                        2 -> {
                            assert(data is Invitation)
                            println("CLIENT_2: The client has received Invitation(\"${(data as Invitation).name}\")")
                            assertEquals(data.name, Invitation("Test_1").name)
                            println("CLIENT_2: Sending AcceptingTheInvitation(\"${(data as Invitation).name}\")")
                            sendData(AcceptingTheInvitation(data.name))
                        }
                        3 -> {
                            assert(data is PlayTheGame)
                            println("CLIENT_2: The client has received PlayTheGame()")
                        }
                    }
                }
            }
            println("CLIENT_2: The client has been created")

            println("CLIENT_2: Delay 1 second...")
            delay(1000)

            println("CLIENT_2: Creating connection")
            client_2.createConnection()

            println("CLIENT_2: Sending Name(\"Test_2\")")
            client_2.sendData(Name("Test_2"))
        }
        sleep(4000)
    }
}

