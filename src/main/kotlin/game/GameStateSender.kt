package game

interface GameStateSender {
    fun sendScore(playerNameToScore: Map<String, Int>)
    fun sendQuestion(question: Triple<String, List<String>, Int>)
    fun sendRemainingTime(remainingTime: Double)
    fun sendFinish()
}