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

        add(JScrollPane(getPanel()).apply {
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        })
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
                        val add = JButton("Add")
                        add.addActionListener {
                            val text = JOptionPane.showInputDialog(panel, "text")
                            if(text != null) if(text.isEmpty()) JOptionPane.showMessageDialog(panel,"Nome da propriedade não pode ser vazio!","Josue", JOptionPane.ERROR_MESSAGE) else add(getWidget(text))
                            menu.isVisible = false
                            revalidate()
                            repaint()
                        }
                        menu.add(add)

                        if(panel.parent is JPanel){
                            val remove = JButton("Remove")
                            remove.addActionListener{

                            }
                        }

                        menu.show(this@apply, e.x, e.y)
                    }
                }
            })
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

            add(getTextField(property))
        }

    private fun getTextField(property: JSONProperty): JTextField =
        JTextField().apply {
            val textField = this
            var oldProperty = property
            addKeyListener(object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    if (e.keyCode == KeyEvent.VK_ENTER) {
                        val newElement = jsonTypeAssigner(text)
                        if(newElement.value != oldProperty.element.value) {
                            observers.forEach { it.elementReplaced(oldProperty, newElement, textField) }
                            oldProperty = JSONProperty(oldProperty.name, newElement)
                        }
                    }
                }
            })
        }
}

interface EditorViewObserver {
    fun elementAdded(property: JSONProperty, component: JComponent)
    fun elementRemoved(property: JSONProperty, component: JComponent)
    fun elementReplaced(property: JSONProperty, newElement: JSONElement, component: JComponent)
}