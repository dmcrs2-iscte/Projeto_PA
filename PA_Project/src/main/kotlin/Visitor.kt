sealed interface JSONVisitor {
    fun visit(c: JSONNode): Boolean = true
    fun endVisit(c: JSONNode) {}
    fun visit(l: JSONLeaf) {}
    fun visit(p: JSONProperty): Boolean = true
}

class GetValueByName(val name: String) : JSONVisitor {
    private var list = mutableListOf<JSONElement>()

    override fun visit(p: JSONProperty) : Boolean {
        if(p.name == name) list.add(p.element)
        return true
    }

    fun getValues(): MutableList<JSONElement> {
        return list
    }

}