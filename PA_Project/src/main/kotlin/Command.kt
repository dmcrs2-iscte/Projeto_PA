import java.awt.Component
import java.awt.Container
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField


interface Command {
    fun run()
    fun undo()
}

class AddElement(private val jsonNode: JSONNode, private val property: JSONProperty, private val component: JComponent): Command {
    override fun run(){
        if( jsonNode is JSONObject ) {
            jsonNode.addElement(property)
        }
    }
    override fun undo() {
        if( jsonNode is JSONObject ) {
            jsonNode.removeElement(property)
            val parent = component.parent
            parent.remove(component)
            parent.revalidate()
            parent.repaint()
        }
    }
}

class RemoveElement(private val jsonNode: JSONNode, private val property: JSONProperty, private val component: JComponent): Command {
    private var parent = Container()
    private var index = -1

    init {
        parent = component.parent
        index = parent.components.toList().indexOf(component)
    }

    override fun run() {
        if( jsonNode is JSONObject ) {
            jsonNode.removeElement(property)
            val parent = component.parent
            parent.remove(component)
            parent.revalidate()
            parent.repaint()
        }
    }
    override fun undo() {
        if( jsonNode is JSONObject ) {
            jsonNode.addElement(property)
            parent.add(component, index)
        }
    }
}

class ReplaceElement(private val jsonNode: JSONNode, private val property: JSONProperty, private val newElement: JSONElement, private val component: JComponent): Command {
    override fun run() {
        if( jsonNode is JSONObject ){
            jsonNode.replaceElement(property.name, newElement)
        }
    }
    override fun undo() {
        if( jsonNode is JSONObject ) {
            jsonNode.replaceElement(property.name, property.element)
            (component as JTextField).text = property.element.toString()
        }
    }
}

class RemoveAllElements(private val jsonObject: JSONObject, private val component: JComponent): Command {
    private val jsonBackup: List<JSONProperty> = jsonObject.value.toList()
    private val panel: JPanel = component.components[0] as JPanel
    private val componentsBackup: List<Component> = panel.components.toList()

    override fun run() {
        jsonBackup.forEach { jsonObject.removeElement(it) }

        panel.removeAll()
        component.revalidate()
        component.repaint()
    }

    override fun undo() {
        jsonBackup.forEach { jsonObject.addElement(it) }

        componentsBackup.forEach { panel.add(it) }
        component.revalidate()
        component.repaint()
    }
}