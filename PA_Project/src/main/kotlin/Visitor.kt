sealed interface JSONVisitor {
    fun visit(c: JSONNode): Boolean = true
    fun endVisit(c: JSONNode) {}

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

    fun getValues(): MutableList<JSONElement> {
        return list
    }
}

internal class GetObjectsByProperty(private val properties: List<String>) : JSONVisitor {
    private var list = mutableListOf<JSONObject>()

    override fun visit(o: JSONObject): Boolean {
        if (properties.all { property -> o.list.any { it.name == property } }) {
            list.add(o)
        }
        return true
    }

    fun getObjects(): MutableList<JSONObject> {
        return list
    }
}

internal class CheckPropertyValues(private val name: String, private val lambda:(JSONElement) -> Boolean = { true }) : JSONVisitor {
    private var valid: Boolean = true

    override fun visit(p: JSONProperty): Boolean {
        if (p.name == name && !lambda(p.element)) valid = false
        return true
    }

    fun isValid(): Boolean{
        return valid
    }
}

//internal class CheckInternalStructure