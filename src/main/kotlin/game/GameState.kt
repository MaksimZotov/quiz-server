package game

import Logging
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import questions.Database
import questions.QuestionsPool

class GameState(
        private val gameStateSender: GameStateSender,
        private val names: List<String>) {

    private val maxNumberOfQuestion = 3
    private val quantityOfQuestions = 5
    private val timeToAnswer = 10
    private val timeDecrement = 1

    private val timeDelay = (timeDecrement * 1000).toLong()

    private val logging = Logging("GameState")
    private val log: (text: String) -> Unit = { text -> logging.log(text) }

    private val questionsPool: QuestionsPool = Database

    private val playerNameToScore = mutableMapOf<String, Int>()
    private var indexOfCorrectAnswer = -1

    private var job: Job
    private var jobIsClosed = false

    init {
        names.forEach { playerNameToScore[it] = 0 }

        job = GlobalScope.launch {
            var quantityOfRemainingQuestions = quantityOfQuestions
            loop@ while (quantityOfRemainingQuestions > 0) {
                val question = questionsPool.getQuestion(getNumberOfQuestion())
                log("Sending to clients $names the question $question")
                if (!jobIsClosed) {
                    gameStateSender.sendQuestion(question)
                } else {
                    break
                }
                indexOfCorrectAnswer = question.third
                var remainingTime = timeToAnswer
                while (remainingTime >= 0) {
                    log("Sending to clients $names the remaining time $remainingTime")
                    if (!jobIsClosed) {
                        gameStateSender.sendRemainingTime(remainingTime)
                    } else {
                        break@loop
                    }
                    delay(timeDelay)
                    remainingTime -= timeDecrement
                }
                quantityOfRemainingQuestions--
            }
            if (!jobIsClosed) {
                log("Sending to clients $names that game is finished")
                gameStateSender.sendFinish()
            }
        }
    }

    fun stopGame() {
        jobIsClosed = true
        log("The game is stopped for clients $names")
    }

    fun getAnswer(playerName: String, indexOfAnswer: Int) {
        check(playerNameToScore.contains(playerName))
        playerNameToScore[playerName] = playerNameToScore[playerName]!! +
                if (indexOfAnswer == indexOfCorrectAnswer) 1 else -1

        log("Sending to clients $names the score $playerNameToScore")

        gameStateSender.sendScore(playerNameToScore)
    }

    private fun getNumberOfQuestion(): Int {
        return (1..maxNumberOfQuestion).random()
    }
}