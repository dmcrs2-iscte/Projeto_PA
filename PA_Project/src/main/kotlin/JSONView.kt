import javax.swing.JTextArea

class JSONView(private val json: JSONNode) : JTextArea() {
    init {
        refresh()
        json.addObserver(object: JSONObserver {
            override fun elementAdded() { refresh() }
            override fun elementRemoved() { refresh() }
            override fun elementReplaced() { refresh() }
            override fun deletedAllElements() { refresh() }
        })
    }

    private fun refresh() {
        text = json.toTree()
    }
}