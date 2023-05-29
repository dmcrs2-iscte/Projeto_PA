import java.awt.BorderLayout
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

    private val panel = JPanel()
    private val frame = JFrame("Josue - JSON Object Editor").apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        layout = BorderLayout()
        size = Dimension(600, 600)


        val editorView = EditorView()
        editorView.addObserver(object : EditorViewObserver {
            override fun elementAdded(property: JSONProperty, component: JComponent) =
                runCommand(AddElement(jsonObject, property, component))

            override fun elementRemoved(property: JSONProperty, component: JComponent) =
                runCommand(RemoveElement(jsonObject, property, component))

            override fun elementReplaced(property: JSONProperty, newElement: JSONElement, component: JComponent) =
                runCommand(ReplaceElement(jsonObject, property, newElement, component))
        })

        add(panel.apply {
            layout = GridLayout(0, 2)

            add(JScrollPane(editorView))
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
                runCommand(RemoveAllElements(jsonObject, editorView))
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