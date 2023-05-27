import java.awt.Component
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.*
import javax.swing.*

class UI(private val jsonObject: JSONObject = JSONObject()) {

    val frame = JFrame("Josue - JSON Object Editor").apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        layout = GridLayout(0, 2)
        size = Dimension(600, 600)

        val left = JPanel()
        left.layout = GridLayout()
        val scrollPane = JScrollPane(getPanel()).apply {
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        }
        left.add(scrollPane)
        add(left)

        val right = JSONView(jsonObject)
        right.isEditable = false
        right.tabSize = 2
        add(right)
    }

    fun open() {
        frame.isVisible = true
    }

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
                            frame.repaint()
                        }
                        menu.add(add)

                        val del = JButton("Delete All")
                        del.addActionListener {
                            components.forEach { remove(it) }
                            menu.isVisible = false
                            revalidate()
                            frame.repaint()
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
            jsonObject.addElement(property)

            add(getTextField(key))
        }

    private fun getTextField(key: String): JTextField =
        JTextField().apply {
            addKeyListener(object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    if (e.keyCode == KeyEvent.VK_ENTER) {
                        jsonObject.replaceElement(key, jsonTypeAssigner(text))
                    }
                }
            })
        }
}