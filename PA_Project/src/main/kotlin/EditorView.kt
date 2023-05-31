import java.awt.*
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class EditorView(private val jsonNode: JSONNode, private val observer: EditorViewObserver) : JPanel() {
    private val observers: MutableList<EditorViewObserver> = mutableListOf()
    internal val mainPanel: JPanel

    init {
        layout = GridLayout(1, 1)
        mainPanel = getPanel()
        add(mainPanel)

        if (jsonNode.value.isNotEmpty()) generateWidgetsFromJSON()
    }

    private fun generateWidgetsFromJSON() {
        if (jsonNode is JSONObject) {
            jsonNode.value.forEach {
                when (it.element) {
                    is JSONObject -> mainPanel.add(getObjectWidget(it.name, it.element))
                    is JSONArray -> mainPanel.add(getArrayWidget(it.name, it.element))
                    else -> mainPanel.add(getWidget(it.name, it.element))
                }
            }
        } else if (jsonNode is JSONArray) {
            jsonNode.value.forEach {
                when (it) {
                    is JSONObject -> mainPanel.add(getObjectWidget("", it))
                    is JSONArray -> mainPanel.add(getArrayWidget("", it))
                    else -> mainPanel.add(getWidget("", it))
                }
            }
        }
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

                        menu.add(getAddButton(panel, menu))
                        menu.add(getObjectButton(panel, menu))
                        menu.add(getArrayButton(panel, menu))
                        menu.show(this@apply, e.x, e.y)
                    }
                }
            })
        }

    private fun getButton(panel: JPanel, menu: JPopupMenu, buttonText: String, widgetFunction: (String) -> Component): JButton {
        val button = JButton(buttonText)
        button.addActionListener {
            if (jsonNode is JSONObject) {
                val text = JOptionPane.showInputDialog(panel, "text")
                if (text != null) {
                    if (text.isEmpty())
                        JOptionPane.showMessageDialog(
                            panel, "Nome da propriedade não pode ser vazio!",
                            "Josue", JOptionPane.ERROR_MESSAGE
                        )
                    else {
                        try {
                            panel.add(widgetFunction(text))
                        } catch (e: IllegalArgumentException) {
                            JOptionPane.showMessageDialog(
                                panel, "Nome da propriedade não pode ser igual a outro dentro do mesmo objeto!",
                                "Josue", JOptionPane.ERROR_MESSAGE
                            )
                        }
                    }
                }
            } else panel.add(widgetFunction(""))
            menu.isVisible = false
            panel.revalidate()
            panel.repaint()
        }
        return button
    }

    private fun getAddButton(panel: JPanel, menu: JPopupMenu) =
        getButton(panel, menu, "Add") { text -> getWidget(text) }

    private fun getObjectButton(panel: JPanel, menu: JPopupMenu) =
        getButton(panel, menu, "Add Object") { text -> getObjectWidget(text) }

    private fun getArrayButton(panel: JPanel, menu: JPopupMenu) =
        getButton(panel, menu, "Add Array") { text -> getArrayWidget(text) }

    private fun getRemoveButton(key: String, textField: JTextField? = null, panel: JPanel, menu: JPopupMenu, element: JSONElement? = null): JButton =
        JButton("Remove").apply {
            addActionListener {
                removeWidget(key, textField, panel, element)
                menu.isVisible = false
                revalidate()
                repaint()
            }
        }

    private fun getMenu(key: String, panel: JPanel, textField: JTextField? = null, element: JSONElement? = null): JPopupMenu =
        JPopupMenu("Message").apply {
            isLightWeightPopupEnabled = false

            add(getAddButton(panel.parent as JPanel, this))
            add(getObjectButton(panel.parent as JPanel, this))
            add(getArrayButton(panel.parent as JPanel, this))
            if(textField != null) add(getRemoveButton(key, textField, panel, this))
            else if(element != null) add(getRemoveButton(key, panel = panel, menu = this, element = element))
        }


    private fun removeWidget(key: String, textField: JTextField?, panel: JPanel, element: JSONElement?) {
        val text: String
        if (textField != null) {
            text = if (textField.text != "null") textField.text else ""
            observers.forEach { it.elementRemoved(jsonNode, key, jsonTypeAssigner(text), panel) }
        } else {
            if (element != null) {
                observers.forEach { it.elementRemoved(jsonNode, key, element, panel) }
            }
        }
    }

    private fun formatWidget(panel: JPanel) {
        panel.apply {
            alignmentX = Component.LEFT_ALIGNMENT
            alignmentY = Component.TOP_ALIGNMENT
            border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        }
    }

    private fun getWidget(key: String, existingElement: JSONElement?= null): JPanel =
        JPanel().apply {
            formatWidget(this)
            layout = BoxLayout(this, BoxLayout.X_AXIS)

            if (key.isNotEmpty()) add(JLabel(key)) else add(JLabel("   "))
            var element: JSONElement = JSONEmpty()
            if( existingElement != null ) element = existingElement
            observers.forEach { it.elementAdded(jsonNode, key, element, this) }

            val textField = getTextField(key, element)
            add(textField)

            val panel = this
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        val menu = getMenu(key, panel, textField)
                        menu.show(this@apply, e.x, e.y)
                    }
                }
            })
        }

    private fun getCompositeWidget(key: String, node: JSONNode): JPanel =
        JPanel().apply {
            formatWidget(this)
            layout = GridLayout(1,1)

            val label = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.X_AXIS)
                alignmentX = Component.LEFT_ALIGNMENT
                alignmentY = Component.TOP_ALIGNMENT
                if (key.isNotEmpty()) add(JLabel(key)) else add(JLabel("   "))
            }
            add(label)

            observers.forEach { it.elementAdded(jsonNode, key, node, this) }

            val objectPanel = EditorView(node, observer)
            objectPanel.addObserver(observer)
            label.add(objectPanel)

            val panel = this
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        val menu = getMenu(key, panel, element = node)
                        menu.show(this@apply, e.x, e.y)
                    }
                }
            })
        }

    private fun getObjectWidget(key: String, jsonObject: JSONObject = JSONObject()): JPanel =
        getCompositeWidget(key, jsonObject)

    private fun getArrayWidget(key: String, jsonArray: JSONArray = JSONArray()): JPanel =
        getCompositeWidget(key, jsonArray)

    private fun getTextField(key: String, element: JSONElement): JTextField {
        var oldElement = element
        val textField = JTextField().apply {
            val textField = this
            if( element !is JSONEmpty ) text = element.toString()
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

    private fun jsonTypeAssigner(value: String): JSONElement {
        return when {
            value.startsWith("\"") && value.endsWith("\"") -> JSONString(value.drop(1).dropLast(1))
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