import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.MouseListener
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
        layout = BorderLayout()
        size = Dimension(600, 600)

        val editorView = EditorView()
        add(JPanel().apply {
            layout = GridLayout(0,2)
            editorView.addObserver(object : EditorViewObserver {
                override fun elementAdded(property: JSONProperty) = runCommand(AddElement(jsonObject, property))
                override fun elementRemoved(property: JSONProperty) = runCommand(RemoveElement(jsonObject, property))
                override fun elementReplaced(property: JSONProperty, newElement: JSONElement) =
                    runCommand(ReplaceElement(jsonObject, property, newElement))
            })
            add(editorView)

            val jsonView = JSONView(jsonObject)
            add(jsonView)
        }, BorderLayout.CENTER)

        add(JPanel().apply {
            layout = GridLayout(0,2)
            add(JButton("Undo").apply {
                addActionListener {
                    if(!commands.isEmpty()) {
                        commands.pop().undo()
                        revalidate()
                        repaint()
                    }
                    println(commands)
                }
            })
            add(JButton("Delete All").apply {

            })
        }, BorderLayout.PAGE_END)
    }

    fun open() {
        frame.isVisible = true
    }
}