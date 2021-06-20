package game

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import questionspool.Database
import questionspool.QuestionsPool

class GameState(private val gameStateSender: GameStateSender, nameOfFirstPlayer: String, nameOfSecondPlayer: String) {
    private val questionsPool: QuestionsPool = Database

    private val quantityOfQuestions = 5
    private val timeToAnswer = 5.0
    private val timeDecrement = 0.25
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
                gameStateSender.sendQuestion(question)
                var remainingTime = timeToAnswer
                while (remainingTime > 0) {
                    gameStateSender.sendRemainingTime(remainingTime)
                    delay(timeDelay)
                    remainingTime -= timeDecrement
                }
                quantityOfRemainingQuestions--
            }
        }
    }

    fun stopGame() {
        work.cancel()
    }

    fun getAnswer(playerName: String, indexOfAnswer: Int) {
        check(playerNameToScore.contains(playerName))
        playerNameToScore[playerName]!!.plus(if (indexOfAnswer == indexOfCorrectAnswer) 1 else -1)
        gameStateSender.sendScore(playerNameToScore)
    }

    private fun getNumberOfQuestion(): Int {
        return (1..maxNumberOfQuestion).random()
    }
}