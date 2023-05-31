import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridLayout
import java.util.*
import javax.swing.*

class UI(private val jsonObject: JSONObject = JSONObject()) {
    private val commands = Stack<Command>()
    private val frame = JFrame("Josue - JSON Object Editor")
    private val editorViewObserver: EditorViewObserver

    init{

        editorViewObserver = object : EditorViewObserver {
            override fun elementAdded(jsonNode: JSONNode, key: String, element: JSONElement, component: JComponent) =
                runCommand(AddElement(jsonNode, key, element, component))

            override fun elementRemoved(jsonNode: JSONNode, key: String, element: JSONElement, component: JComponent) =
                runCommand(RemoveElement(jsonNode, key, element, component))

            override fun elementReplaced(
                jsonNode: JSONNode,
                key: String,
                element: JSONElement,
                newElement: JSONElement,
                component: JComponent
            ) =
                runCommand(ReplaceElement(jsonNode, key, element, newElement, component))
        }

        if(jsonObject.value.isEmpty()){
            initializeUI()
        }else{
            buildUIFromJSON()
        }
    }
    private fun runCommand(command: Command) {
        commands.push(command)
        command.run()
    }

    private fun initializeUI() {

        val panel = JPanel()
        frame.apply {
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
    }

    private fun buildUIFromJSON(){

        fun build(jsonNode: JSONNode, editorView: EditorView): EditorView {
            jsonNode.value.forEach {
                var element: JSONElement = JSONEmpty()
                var key = ""
                if(jsonNode is JSONObject){
                    element = (it as JSONProperty).element
                    key = it.name
                }else{
                    element = (it as JSONElement)
                }
                when (element) {
                    is JSONObject -> {
                        val panel = editorView.getObjectWidget(key)
                        val newEditorView = EditorView(JSONArray(), editorViewObserver)
                        newEditorView.addObserver(editorViewObserver)
                        editorView.mainPanel.add(panel.add(build(element, newEditorView)))
                    }
                    is JSONArray -> {
                        val panel = editorView.getObjectWidget(key)
                        val newEditorView = EditorView(JSONArray(), editorViewObserver)
                        newEditorView.addObserver(editorViewObserver)
                        editorView.mainPanel.add(panel.add(build(element, newEditorView)))
                    }
                    else -> editorView.mainPanel.add(editorView.getWidget(key, element))
                }
            }
            return editorView
        }

        val panel = JPanel()
        frame.apply {
            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            layout = BorderLayout()
            size = Dimension(600, 600)

            //println(editorViewObserver)

            val editorView = build(jsonObject, EditorView(JSONObject(), editorViewObserver))

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
    }
    fun open() {
        frame.isVisible = true
    }
}