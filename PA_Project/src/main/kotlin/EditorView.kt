import java.awt.Component
import java.awt.GridLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class EditorView(private val json: JSONObject) : JPanel() {
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

            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        val menu = JPopupMenu("Message")

                        val add = JButton("Add")
                        add.addActionListener {
                            val text = JOptionPane.showInputDialog("text")
                            add(getWidget(text))
                            menu.isVisible = false
                            revalidate()
                            repaint()
                        }
                        menu.add(add)

                        val del = JButton("Delete All")
                        del.addActionListener {
                            components.forEach { remove(it) }
                            json.removeAllElements()
                            menu.isVisible = false
                            revalidate()
                            repaint()
                        }
                        menu.add(del)

                        menu.show(this@apply, 100, 100)
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
            json.addElement(property)

            add(getTextField(key))
        }

    private fun getTextField(key: String): JTextField =
        JTextField().apply {
            addKeyListener(object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    if (e.keyCode == KeyEvent.VK_ENTER) {
                        json.replaceElement(key, jsonTypeAssigner(text))
                    }
                }
            })
        }
}

interface EditorViewObserver {

}