import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridLayout
import java.util.*
import javax.swing.*

class UI(private val jsonObject: JSONObject = JSONObject()) {
    private val commands = Stack<Command>()
    private fun runCommand(command: Command) {
        commands.push(command)
        command.run()
    }

    private val editorViewObserver: EditorViewObserver = object : EditorViewObserver {
        override fun elementAdded(jsonNode: JSONNode, key: String, element: JSONElement, component: JComponent) =
            runCommand(AddElement(jsonNode, key, element, component))

        override fun elementRemoved(jsonNode: JSONNode, key: String, element: JSONElement, component: JComponent) =
            runCommand(RemoveElement(jsonNode, key, element, component))

        override fun elementReplaced(jsonNode: JSONNode, key: String, element: JSONElement, newElement: JSONElement, component: JComponent) =
            runCommand(ReplaceElement(jsonNode, key, element, newElement, component))
    }

    private val panel = JPanel()
    private val frame = JFrame("Josue - JSON Object Editor").apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        layout = BorderLayout()
        size = Dimension(600, 600)

        val editorView = EditorView(jsonObject, editorViewObserver)
        editorView.addObserver(editorViewObserver)

        add(panel.apply {
            layout = GridLayout(0, 2)

            add(editorView)
            add(JSONView(jsonObject))
        }, BorderLayout.CENTER)

        add(JPanel().apply {
            layout = GridLayout(0, 2)
            add(JButton("Undo").apply {
                addActionListener {
                    if (!commands.isEmpty()) {
                        commands.pop().undo()
                        revalidate()
                        repaint()
                    }
                }
            })
            add(JButton("Delete All").apply {
                addActionListener {
                    runCommand(RemoveAllElements(jsonObject, editorView.mainPanel))
                    revalidate()
                    repaint()
                }
            })
        }, BorderLayout.PAGE_END)
    }

    fun open() {
        frame.isVisible = true
    }
}