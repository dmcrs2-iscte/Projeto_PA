import java.awt.Component
import java.awt.Container
import javax.swing.JComponent
import javax.swing.JTextField


interface Command {
    fun run()
    fun undo()
}

internal class AddElement(private val jsonNode: JSONNode, private val key: String, private val element: JSONElement, private val component: JComponent): Command {
    override fun run() {
        if (jsonNode is JSONObject) jsonNode.addElement(JSONProperty(key, element))
        else if (jsonNode is JSONArray) jsonNode.addElement(element)
    }

    override fun undo() {
        if (jsonNode is JSONObject) jsonNode.removeElement(JSONProperty(key, element))
        else if (jsonNode is JSONArray) jsonNode.removeElement(element)

        val parent = component.parent
        parent.remove(component)
        parent.revalidate()
        parent.repaint()
    }
}

internal class RemoveElement(private val jsonNode: JSONNode, private val key: String, private val element: JSONElement, private val component: JComponent): Command {
    private var parent = Container()
    private var index = -1

    init {
        parent = component.parent
        index = parent.components.toList().indexOf(component)
    }

    override fun run() {
        if (jsonNode is JSONObject) jsonNode.removeElement(JSONProperty(key, element))
        else if (jsonNode is JSONArray) jsonNode.removeElement(element)

        val parent = component.parent
        parent.remove(component)
        parent.revalidate()
        parent.repaint()
    }

    override fun undo() {
        if (jsonNode is JSONObject) jsonNode.addElement(JSONProperty(key, element))
        else if (jsonNode is JSONArray) jsonNode.addElement(element)

        parent.add(component, index)
        parent.revalidate()
        parent.repaint()
    }
}

internal class ReplaceElement(private val jsonNode: JSONNode, private val key: String, private val element: JSONElement, private val newElement: JSONElement, private val component: JComponent): Command {
    override fun run() {
        if (jsonNode is JSONObject) jsonNode.replaceElement(key, newElement)
        else if (jsonNode is JSONArray) jsonNode.replaceElement(element, newElement)
    }

    override fun undo() {
        if (jsonNode is JSONObject) jsonNode.replaceElement(key, element)
        else if (jsonNode is JSONArray) jsonNode.replaceElement(newElement, element)

        (component as JTextField).text = element.toString()
    }
}

internal class RemoveAllElements(private val jsonObject: JSONObject, private val component: JComponent): Command {
    private val jsonBackup: List<JSONProperty> = jsonObject.value.toList()
    private val componentsBackup: List<Component> = component.components.toList()

    override fun run() {
        jsonBackup.forEach { jsonObject.removeElement(it) }

        component.removeAll()
        component.revalidate()
        component.repaint()
    }

    override fun undo() {
        jsonBackup.forEach { jsonObject.addElement(it) }

        componentsBackup.forEach { component.add(it) }
        component.revalidate()
        component.repaint()
    }
}