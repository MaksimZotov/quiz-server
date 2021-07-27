package questions

import java.sql.*
import java.util.*

object Database : QuestionsPool {
    private val userName = "f0550944_user"
    private val password = "2048"
    private val catalog = "f0550944_quiz-database"
    private var connection: Connection
    private var statement: Statement

    init {
        val connectionProps = Properties()
        connectionProps["user"] = userName
        connectionProps["password"] = password
        try {
            Class.forName("com.mysql.jdbc.Driver").getDeclaredConstructor().newInstance()
            connection = DriverManager.getConnection(
                    "jdbc:" + "mysql" + "://" +
                            "141.8.193.236" +
                            ":" + "3306" + "/" +
                            "",
                    connectionProps)
            connection.catalog = catalog
            statement = connection.createStatement()
        } catch (ex: SQLException) {
            throw ex
        }
    }

    override fun getQuestion(numberOfQuestion: Int): Triple<String, List<String>, Int> {
        try {
            statement.execute("SELECT * FROM `Questions and answers` WHERE number=" + numberOfQuestion.toString())
            val result = statement.resultSet
            result.next()
            return Triple(
                    result.getString("Question"),
                    listOf(
                        result.getString("Answer 1"),
                        result.getString("Answer 2"),
                        result.getString("Answer 3")
                    ),
                    result.getInt("Correct answer")
            )
        } catch (ex: SQLException) {
            throw ex
        }
    }
}