import javax.swing.JTextArea

class JSONView(private val json: JSONObject) : JTextArea() {
    init {
        refresh()
        json.addObserver(object: JSONObserver {
            override fun elementAdded() { refresh() }
            override fun elementRemoved() { refresh() }
            override fun elementReplaced() { refresh() }
        })

        isEditable = false
        tabSize = 2
    }

    private fun refresh() {
        text = json.toTree()
    }
}