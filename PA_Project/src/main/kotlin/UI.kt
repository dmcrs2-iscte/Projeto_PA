class UI : Observer {
    override fun update(subject: Subject) {
        TODO("Not yet implemented")
    }

    private val editor = Editor()

    fun open() {
        editor.open()
    }
}

fun main() {
    UI().open()
}