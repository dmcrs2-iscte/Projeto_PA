package json

sealed interface JSONVisitor {
    fun visit(n: JSONNode): Boolean = true

    fun visit(l: JSONLeaf) {}

    fun visit(o: JSONObject): Boolean = true

    fun visit(p: JSONProperty): Boolean = true
}

internal class GetElementsByKey(private val name: String) : JSONVisitor {
    private val list = mutableListOf<JSONElement>()

    override fun visit(p: JSONProperty) : Boolean {
        if (p.name == name) list.add(p.element)
        return true
    }

    fun getValues(): MutableList<JSONElement> = list
}

internal class GetObjectsByProperties(private val properties: List<String>) : JSONVisitor {
    private var list = mutableListOf<JSONObject>()

    override fun visit(o: JSONObject): Boolean {
        if (properties.all { property -> o.value.any { it.name == property } }) list.add(o)
        return true
    }

    fun getObjects(): MutableList<JSONObject> = list
}

internal class ArePropertiesOfType(private val name: String, private val lambda: (JSONElement) -> Boolean = { true }) : JSONVisitor {
    private var valid: Boolean = true

    override fun visit(p: JSONProperty): Boolean {
        if (p.name == name && !lambda(p.element)) valid = false
        return true
    }

    fun isValid(): Boolean = valid
}

internal class CheckArrayStructure(private val name: String) : JSONVisitor {
    private var valid: Boolean = false

    override fun visit(p: JSONProperty): Boolean {
        if (p.name == name && p.element is JSONArray) {
            val elements = p.element.value
            if (elements.size > 1 && elements.all { it::class == elements[0]::class }) {
                val firstElement = elements[0]
                if (firstElement is JSONObject) {
                    val expected = firstElement.value.map { it.name to it.element::class }.toSet()
                    valid = elements.all {
                        (it as? JSONObject)?.value?.map { p -> p.name to p.element::class }?.toSet() == expected
                    }
                }
            } else valid = true
        }
        return true
    }

    fun isValid(): Boolean = valid
}