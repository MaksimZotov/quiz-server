package questions

interface QuestionsPool {
    fun getQuestion(numberOfQuestion: Int): Triple<String, List<String>, Int>
}