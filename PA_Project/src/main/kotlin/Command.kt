
import java.awt.Container
import javax.swing.JComponent

interface Command {
    fun run()
    fun undo()
}

class AddElement(private val jsonObject: JSONObject, private val property: JSONProperty, private val component: JComponent): Command {
    override fun run() = jsonObject.addElement(property)
    override fun undo(){
        jsonObject.removeElement(property)
        val parent = component.parent
        parent.remove(component)
        parent.revalidate()
        parent.repaint()
    }
}

class RemoveElement(private val jsonObject: JSONObject, private val property: JSONProperty, private val component: JComponent): Command {

    private var parent = Container()
    private var index = -1

    init {
        parent = component.parent
        index = parent.components.toList().indexOf(component)
    }
    override fun run() = jsonObject.removeElement(property)
    override fun undo() {
        jsonObject.addElement(property)
        parent.add(component)
    }
}

class ReplaceElement(private val jsonObject: JSONObject, private val property: JSONProperty, private val newElement: JSONElement, private val component: JComponent): Command {
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