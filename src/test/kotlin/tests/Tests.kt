package tests

import Logging
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
            val logging = Logging("Client_1")
            val log: (text: String) -> Unit = { text -> logging.log(text)}
            
            val client_1: ClientStub = object : ClientStub() {
                override fun getData(data: Data) {
                    super.getData(data)
                    count++
                    when (count) {
                        1 -> {
                            assert(data is AcceptingTheName)
                            log("The client has received AcceptingTheName: \"${(data as AcceptingTheName).name}\"")
                            assertEquals(data.name, AcceptingTheName("Test_1").name)
                        }
                    }
                }
            }
            log("The client has been created")

            log("Creating connection")
            client_1.createConnection()

            log("Sending Name(\"Test_1\")")
            client_1.sendData(Name("Test_1"))
        }

        // Client_2
        GlobalScope.launch {
            val logging = Logging("Client_2")
            val log: (text: String) -> Unit = { text -> logging.log(text)}
            
            val client_2: ClientStub = object : ClientStub() {
                override fun getData(data: Data) {
                    super.getData(data)
                    count++
                    when (count) {
                        1 -> {
                            assert(data is RefusalTheName)
                            log("The client has received RefusalTheName(\"${(data as RefusalTheName).name}\")")
                            assertEquals(data.name, AcceptingTheName("Test_1").name)
                        }
                        2 -> {
                            assert(data is AcceptingTheName)
                            log("The client has received AcceptingTheName(\"${(data as AcceptingTheName).name}\")")
                            assertEquals(data.name, AcceptingTheName("Test_2").name)
                        }
                    }
                }
            }
            log("The client has been created")

            log("Delay 1 second...")
            delay(1000)

            log("Creating connection")
            client_2.createConnection()

            log("Sending Name(\"Test_1\")")
            client_2.sendData(Name("Test_1"))

            log("Delay 1 second...")
            delay(1000)

            log("Sending Name(\"Test_2\")")
            client_2.sendData(Name("Test_2"))
        }
        sleep(3000)
    }

    @Test
    fun testGame_1() {
        // Client_1
        GlobalScope.launch {
            val logging = Logging("Client_1")
            val log: (text: String) -> Unit = { text -> logging.log(text)}
            
            val client_1: ClientStub = object : ClientStub() {
                override fun getData(data: Data) {
                    super.getData(data)
                    count++
                    when (count) {
                        1 -> {
                            assert(data is AcceptingTheName)
                            log("The client has received AcceptingTheName(\"${(data as AcceptingTheName).name}\")")
                            assertEquals(data.name, AcceptingTheName("Test_1").name)
                        }
                        2 -> {
                            assert(data is PlayTheGame)
                            log("The client has received PlayTheGame()")
                        }
                        else -> {
                            if (data is Question) {
                                log("The client has received Question(" +
                                        data.question + " " + data.answers + " " + data.indexOfCorrectAnswer +
                                        ")")
                            } else if (data is RemainingTime) {
                                log("The client has received RemainingTime(" +
                                        data.time +
                                        ")")
                            } else if (data is FinishTheGame) {
                                log("The client has received FinishTheGame()")
                            }
                        }
                    }
                }
            }
            log("The client has been created")

            log("Creating connection")
            client_1.createConnection()

            log("Sending Name(\"Test_1\")")
            client_1.sendData(Name("Test_1"))

            log("Delay 1.5 seconds...")
            delay(1500)

            log("Sending Invitation(\"Test_2\")")
            client_1.sendData(Invitation("Test_2"))
        }

        // Client_2
        GlobalScope.launch {
            val logging = Logging("Client_2")
            val log: (text: String) -> Unit = { text -> logging.log(text)}
            
            val client_2: ClientStub = object : ClientStub() {
                override fun getData(data: Data) {
                    super.getData(data)
                    count++
                    when (count) {
                        1 -> {
                            assert(data is AcceptingTheName)
                            log("The client has received AcceptingTheName(\"${(data as AcceptingTheName).name}\")")
                            assertEquals(data.name, AcceptingTheName("Test_2").name)
                        }
                        2 -> {
                            assert(data is Invitation)
                            log("The client has received Invitation(\"${(data as Invitation).name}\")")
                            assertEquals(data.name, Invitation("Test_1").name)
                            log("Sending AcceptingTheInvitation(\"${(data as Invitation).name}\")")
                            sendData(AcceptingTheInvitation(data.name))
                        }
                        3 -> {
                            assert(data is PlayTheGame)
                            log("The client has received PlayTheGame()")
                        }
                        else -> {
                            if (data is Question) {
                                log("The client has received Question(" +
                                        data.question + " " + data.answers + " " + data.indexOfCorrectAnswer +
                                        ")")
                            } else if (data is RemainingTime) {
                                log("The client has received RemainingTime(" +
                                        data.time +
                                        ")")
                            } else if (data is FinishTheGame) {
                                log("The client has received FinishTheGame()")
                            }
                        }
                    }
                }
            }
            log("The client has been created")

            log("Delay 1 second...")
            delay(1000)

            log("Creating connection")
            client_2.createConnection()

            log("Sending Name(\"Test_2\")")
            client_2.sendData(Name("Test_2"))
        }
        sleep(70000)
    }
}