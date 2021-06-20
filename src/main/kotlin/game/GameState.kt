package game

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import log
import questionspool.Database
import questionspool.QuestionsPool

class GameState(private val gameStateSender: GameStateSender, val nameOfFirstPlayer: String, val nameOfSecondPlayer: String) {
    private val questionsPool: QuestionsPool = Database

    private val quantityOfQuestions = 5
    private val timeToAnswer = 10
    private val timeDecrement = 1
    private val timeDelay = (timeDecrement * 1000).toLong()

    private val maxNumberOfQuestion = 3

    private val playerNameToScore = mutableMapOf(nameOfFirstPlayer to 0, nameOfSecondPlayer to 0)
    private var indexOfCorrectAnswer = -1

    private var work: Job

    init {
        work = GlobalScope.launch {
            var quantityOfRemainingQuestions = quantityOfQuestions
            while (quantityOfRemainingQuestions > 0) {
                val question = questionsPool.getQuestion(getNumberOfQuestion())
                log("GAME: Sending to clients \"${nameOfFirstPlayer}\" and " +
                        "\"${nameOfSecondPlayer}\" the question $question")
                gameStateSender.sendQuestion(question)
                indexOfCorrectAnswer = question.third
                var remainingTime = timeToAnswer
                while (remainingTime >= 0) {
                    log("GAME: Sending to clients \"${nameOfFirstPlayer}\" and " +
                            "\"${nameOfSecondPlayer}\" the remaining time $remainingTime")
                    gameStateSender.sendRemainingTime(remainingTime)
                    delay(timeDelay)
                    remainingTime -= timeDecrement
                }
                gameStateSender.sendRemainingTime(remainingTime)
                quantityOfRemainingQuestions--
            }
            log("GAME: Sending to clients \"${nameOfFirstPlayer}\" and " +
                    "\"${nameOfSecondPlayer}\" that game is finished")
            gameStateSender.sendFinish()
        }
    }

    fun stopGame() {
        work.cancel()
        log("GAME: The game is canceled for clients \"${nameOfFirstPlayer}\" and \"${nameOfSecondPlayer}\"")
    }

    fun getAnswer(playerName: String, indexOfAnswer: Int) {
        check(playerNameToScore.contains(playerName))
        playerNameToScore[playerName] = playerNameToScore[playerName]!! +
                if (indexOfAnswer == indexOfCorrectAnswer) 1 else -1

        log("GAME: Sending to clients \"${nameOfFirstPlayer}\" and " +
                "\"${nameOfSecondPlayer}\" the score $playerNameToScore")

        gameStateSender.sendScore(playerNameToScore)
    }

    private fun getNumberOfQuestion(): Int {
        return (1..maxNumberOfQuestion).random()
    }
}