import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.*

class UI(private val jsonObject: JSONObject = JSONObject()) {

    private val frame = JFrame("Josue - JSON Object Editor").apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        layout = GridLayout(0, 2)
        size = Dimension(600, 600)

        val editorView = EditorView(jsonObject)
        add(editorView)

        val jsonView = JSONView(jsonObject)
        add(jsonView)
    }

    fun open() {
        frame.isVisible = true
    }
}