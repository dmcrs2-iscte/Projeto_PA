import java.awt.*
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.*

class EditorView(private val jsonNode: JSONNode, private val commands: Stack<Command>) : JPanel() {
    private val observers: MutableList<EditorViewObserver> = mutableListOf()

    init {
        layout = GridLayout()
        add(ScrollPane().apply {
            add(getPanel())
        })
    }

    fun runCommand(command: Command) {
        commands.push(command)
        command.run()
    }

    fun addObserver(observer: EditorViewObserver) = observers.add(observer)

    private fun getPanel(): JPanel =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            alignmentY = Component.TOP_ALIGNMENT
            border = BorderFactory.createLineBorder(Color.black)
            val panel = this

            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        val menu = JPopupMenu("Message")

                        menu.isLightWeightPopupEnabled = false

                        menu.add(addAddButton(panel, menu))
                        menu.add(addObjectButton(panel, menu))

                        menu.show(this@apply, e.x, e.y)
                    }
                }
            })
        }

    private fun addAddButton(panel: JPanel, menu: JPopupMenu): JButton {
        val add = JButton("Add")
        add.addActionListener {
            if( jsonNode is JSONObject) {
                val text = JOptionPane.showInputDialog(panel, "text")
                if (text != null) {
                    if (text.isEmpty()) {
                        JOptionPane.showMessageDialog(
                            panel,
                            "Nome da propriedade não pode ser vazio!",
                            "Josue",
                            JOptionPane.ERROR_MESSAGE
                        )
                    } else {
                        try {
                            panel.add(getWidget(text))
                        } catch (e: IllegalArgumentException) {
                            JOptionPane.showMessageDialog(
                                panel,
                                "Nome da propriedade não pode ser igual a outro dentro do mesmo objeto!",
                                "Josue",
                                JOptionPane.ERROR_MESSAGE
                            )
                        }
                    }
                }
            }else{
                panel.add(getWidget(""))
            }
            menu.isVisible = false
            revalidate()
            repaint()
        }
        return add
    }

    private fun getWidget(key: String): JPanel =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            alignmentY = Component.TOP_ALIGNMENT
            border = BorderFactory.createLineBorder(Color.black)

            if(key.isNotEmpty()) add(JLabel(key)) else add(JLabel("   "))
            val element = JSONEmpty()
            observers.forEach { it.elementAdded(jsonNode, key, element, this) }

            val textField = getTextField(key, element)
            add(textField)

            val panel = this
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        val menu = JPopupMenu("Message")

                        menu.isLightWeightPopupEnabled = false

                        val remove = JButton("Remove")
                        remove.addActionListener {
                            removeWidget(key, textField, panel)
                            menu.isVisible = false
                            revalidate()
                            repaint()
                        }
                        menu.add(remove)
                        menu.add(addAddButton(panel.parent as JPanel, menu))
                        menu.add(addObjectButton(panel.parent as JPanel, menu))
                        menu.show(this@apply, e.x, e.y)
                    }
                }
            })

        }

    private fun getTextField(key: String, element: JSONElement): JTextField {
        var oldElement = element
        val textField = JTextField().apply {
            val textField = this
            addKeyListener(object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    if (e.keyCode == KeyEvent.VK_ENTER) {
                        val newElement = jsonTypeAssigner(text)
                        if (newElement.value != oldElement.value) {
                            observers.forEach { it.elementReplaced(jsonNode, key, oldElement, newElement, textField) }
                            oldElement = newElement
                        }
                    }
                }
            })
        }
        return textField
    }

    private fun addObjectButton(panel: JPanel, menu: JPopupMenu): JButton {
        val addObject = JButton("Add Object")
        addObject.addActionListener {
            if ( jsonNode is JSONObject ) {
                val text = JOptionPane.showInputDialog(panel, "text")
                if (text != null) {
                    if (text.isEmpty()) {
                        JOptionPane.showMessageDialog(
                            panel,
                            "Nome da propriedade não pode ser vazio!",
                            "Josue",
                            JOptionPane.ERROR_MESSAGE
                        )
                    } else {
                        try {
                            panel.add(getObjectWidget(text))
                        } catch (e: IllegalArgumentException) {
                            JOptionPane.showMessageDialog(
                                panel,
                                "Nome da propriedade não pode ser igual a outro dentro do mesmo objeto!",
                                "Josue",
                                JOptionPane.ERROR_MESSAGE
                            )
                        }
                    }
                }
            } else {
                panel.add(getObjectWidget(""))
            }
            menu.isVisible = false
            panel.revalidate()
            panel.repaint()
        }
        return addObject
    }

    private fun getObjectWidget(key: String): JPanel =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            alignmentY = Component.TOP_ALIGNMENT
            border = BorderFactory.createLineBorder(Color.black)

            val label = JPanel().apply {
                layout = FlowLayout(FlowLayout.LEFT)
                if(key.isNotEmpty()) add(JLabel(key)) else add(JLabel("   "))
            }
            add(label)

            val jsonObject = JSONObject()
            observers.forEach { it.elementAdded(jsonNode, key, jsonObject, this) }

            val objectPanel = EditorView(jsonObject, commands)

            objectPanel.addObserver(object : EditorViewObserver {
                override fun elementAdded(jsonNode: JSONNode, key: String, element: JSONElement, component: JComponent) =
                    runCommand(AddElement(jsonNode, key, element, component))

                override fun elementRemoved(jsonNode: JSONNode, key: String, element: JSONElement, component: JComponent) =
                    runCommand(RemoveElement(jsonNode, key, element, component))

                override fun elementReplaced(jsonNode: JSONNode, key: String, element: JSONElement, newElement: JSONElement, component: JComponent) =
                    runCommand(ReplaceElement(jsonNode, key, element, newElement, component))
            })
            label.add(objectPanel)

            val panel = this

            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        val menu = JPopupMenu("Message")

                        menu.add(addAddButton(panel.parent as JPanel, menu))

                        menu.add(addObjectButton(panel.parent as JPanel, menu))
                        menu.add(addArrayButton(panel.parent as JPanel, menu))

                        menu.show(this@apply, e.x, e.y)
                    }
                }
            })
        }

    private fun removeWidget(key: String, textField: JTextField, panel: JPanel) {
        val text = if (textField.text != "null") textField.text else ""
        observers.forEach { it.elementRemoved(jsonNode, key, jsonTypeAssigner(text), panel) }
    }

    private fun jsonTypeAssigner(value: String): JSONElement {
        return when {
            value.startsWith("\"") && value.endsWith("\"") -> JSONString(value)
            value.toIntOrNull() != null -> JSONNumber(value.toInt())
            value.toDoubleOrNull() != null -> JSONFloat(value.toDouble())
            value.toBooleanStrictOrNull() != null -> JSONBoolean(value.toBooleanStrict())
            value.isEmpty() -> JSONEmpty()
            else -> JSONString(value)
        }
    }
}

interface EditorViewObserver {
    fun elementAdded(jsonNode: JSONNode, key: String, element: JSONElement, component: JComponent)
    fun elementRemoved(jsonNode: JSONNode, key: String, element: JSONElement, component: JComponent)
    fun elementReplaced(jsonNode: JSONNode, key: String, element: JSONElement, newElement: JSONElement, component: JComponent)
}