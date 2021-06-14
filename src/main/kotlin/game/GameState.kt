package game

import data.Question
import sessions.GameSession

class GameState(val gameSession: GameSession, firstPlayerName: String, secondPlayerName: String) {
    private val firstPlayerToScore = mapOf<String, Int>(firstPlayerName to 0)
    private val secondPlayerToScore = mapOf<String, Int>(secondPlayerName to 0)
    private var remainingTime = 0.0

    fun getAnswer(playerName: String) {
        TODO()
    }

    fun getQuestion(): Question {
        TODO()
    }
}