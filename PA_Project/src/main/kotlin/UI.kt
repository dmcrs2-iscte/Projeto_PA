import java.awt.Dimension
import java.awt.GridLayout
import java.util.*
import javax.swing.*

class UI(private val jsonObject: JSONObject = JSONObject()) {
    private val commands = Stack<Command>()
    fun runCommand(command: Command) {
        commands.push(command)
        command.run()
    }

    private val frame = JFrame("Josue - JSON Object Editor").apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        layout = GridLayout(0, 2)
        size = Dimension(600, 600)

        val editorView = EditorView()
        editorView.addObserver(object : EditorViewObserver {
            override fun elementAdded(property: JSONProperty) = runCommand(AddElement(jsonObject, property))
            override fun elementRemoved(property: JSONProperty) = runCommand(RemoveElement(jsonObject, property))
            override fun elementReplaced(property: JSONProperty, newElement: JSONElement) = runCommand(ReplaceElement(jsonObject, property, newElement))
            override fun allElementsRemoved() = runCommand(RemoveAllElements(jsonObject))
        })
        add(editorView)

        val jsonView = JSONView(jsonObject)
        add(jsonView)
    }

    fun open() {
        frame.isVisible = true
    }
}