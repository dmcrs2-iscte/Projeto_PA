interface Command {
    fun run()
    fun undo()
}

class AddElement(private val jsonObject: JSONObject, private val property: JSONProperty): Command {
    override fun run() = jsonObject.addElement(property)
    override fun undo() = jsonObject.removeElement(property)
}

class RemoveElement(private val jsonObject: JSONObject, private val property: JSONProperty): Command {
    override fun run() = jsonObject.removeElement(property)
    override fun undo() = jsonObject.addElement(property)
}

class ReplaceElement(private val jsonObject: JSONObject, private val property: JSONProperty, private val newElement: JSONElement): Command {
    override fun run() = jsonObject.replaceElement(property.name, newElement)
    override fun undo() = jsonObject.replaceElement(property.name, property.element)
}

class RemoveAllElements(private val jsonObject: JSONObject): Command {
    private var backup = mutableListOf<JSONProperty>()
    init {
        backup = jsonObject.value.toMutableList()
    }

    override fun run() = backup.forEach { jsonObject.removeElement(it) }
    override fun undo() = backup.forEach { jsonObject.addElement(it) }
}