class Logging() {
    var tag = ""

    constructor(tag: String) : this() {
        this.tag = tag
    }

    fun log(text: String) {
        if (tag != "") {
            println("$tag: $text")
        } else {
            println(text)
        }
    }
}