import java.awt.Component
import java.awt.GridLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class EditorView : JPanel() {
    private val observers: MutableList<EditorViewObserver> = mutableListOf()

    init {
        layout = GridLayout()
        add(getPanel())
    }

    fun addObserver(observer: EditorViewObserver) = observers.add(observer)

    private fun getPanel(): JPanel =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            alignmentY = Component.TOP_ALIGNMENT
            val panel = this

            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        val menu = JPopupMenu("Message")

                        menu.add(addAddButton(panel, menu))

                        menu.show(this@apply, e.x, e.y)
                    }
                }
            })
        }

    private fun addAddButton(panel: JPanel, menu: JPopupMenu): JButton {
        val add = JButton("Add")
        add.addActionListener {
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
            menu.isVisible = false
            revalidate()
            repaint()
        }
        return add
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

    private fun getWidget(key: String): JPanel =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            alignmentY = Component.TOP_ALIGNMENT

            add(JLabel(key))

            val property = JSONProperty(key, JSONEmpty())
            observers.forEach { it.elementAdded(property, this) }

            val textField = getTextField(property)
            add(textField)

            val panel = this

            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        val menu = JPopupMenu("Message")
                        val remove = JButton("Remove")
                        remove.addActionListener {
                            removeWidget(property, textField, panel)
                            menu.isVisible = false
                            revalidate()
                            repaint()
                        }
                        menu.add(remove)

                        menu.add(addAddButton(panel.parent as JPanel, menu))

                        menu.show(this@apply, e.x, e.y)
                    }
                }
            })

        }

    private fun getTextField(property: JSONProperty): JTextField {
        var oldProperty = property
        val textField = JTextField().apply {
            val textField = this
            addKeyListener(object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    if (e.keyCode == KeyEvent.VK_ENTER) {
                        val newElement = jsonTypeAssigner(text)
                        if (newElement.value != oldProperty.element.value) {
                            observers.forEach { it.elementReplaced(oldProperty, newElement, textField) }
                            oldProperty = JSONProperty(oldProperty.name, newElement)
                        }
                    }
                }
            })
        }
        return textField
    }

    private fun removeWidget(property: JSONProperty, textField: JTextField, panel: JPanel) {
        val text = if (textField.text != "null") textField.text else ""
        observers.forEach { it.elementRemoved(JSONProperty(property.name, jsonTypeAssigner(text)), panel) }
    }

}

interface EditorViewObserver {
    fun elementAdded(property: JSONProperty, component: JComponent)
    fun elementRemoved(property: JSONProperty, component: JComponent)
    fun elementReplaced(property: JSONProperty, newElement: JSONElement, component: JComponent)
}