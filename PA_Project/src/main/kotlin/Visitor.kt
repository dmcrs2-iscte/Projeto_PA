sealed interface JSONVisitor {
    fun visit(n: JSONNode): Boolean = true

    fun visit(l: JSONLeaf) {}

    fun visit(o: JSONObject): Boolean = true

    fun visit(p: JSONProperty): Boolean = true
}

internal class GetValuesByName(private val name: String) : JSONVisitor {
    private var list = mutableListOf<JSONElement>()

    override fun visit(p: JSONProperty) : Boolean {
        if (p.name == name) list.add(p.element)
        return true
    }

    fun getValues(): MutableList<JSONElement> = list
}

internal class GetObjectsByProperty(private val properties: List<String>) : JSONVisitor {
    private var list = mutableListOf<JSONObject>()

    override fun visit(o: JSONObject): Boolean {
        if (properties.all { property -> o.value.any { it.name == property } }) list.add(o)
        return true
    }

    fun getObjects(): MutableList<JSONObject> = list
}

internal class CheckPropertyValues(private val name: String, private val lambda: (JSONElement) -> Boolean = { true }) : JSONVisitor {
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
        if (p.name == name && p.element is JSONArray){
            val array = p.element.value
            if (array.all{ it is JSONObject}) {
                val expected = (array.firstOrNull() as? JSONObject)?.value?.map { it.name to it.element::class }?.toSet()
                if (array.all { (it as? JSONObject)?.value?.map { it.name to it.element::class }?.toSet() == expected }) valid = true
            }
        }
        return true
    }

    fun isValid(): Boolean = valid
}