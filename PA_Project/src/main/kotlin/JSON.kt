interface JSONVisitor {
    fun visit(c: JSONNode): Boolean = true
    fun endVisit(c: JSONNode) {}
    fun visit(l: JSONLeaf) {}
}
sealed interface JSONElement{
    fun accept(v: JSONVisitor)
}

sealed interface JSONLeaf: JSONElement{
    val value: Any?
    override fun accept(v: JSONVisitor) {
        v.visit(this)
    }
}

sealed interface JSONNode: JSONElement

data class JSONProperty(internal val name: String, internal val element: JSONElement){

}

data class JSONString(override val value: String): JSONLeaf

data class JSONNumber(override val value: Int): JSONLeaf

data class JSONBoolean(override val value: Boolean): JSONLeaf

data class JSONFloat(override val value: Float): JSONLeaf

data class JSONEmpty(override val value: Nothing? = null): JSONLeaf

data class JSONObject(internal var list: MutableList<JSONProperty> = mutableListOf()): JSONNode {
    fun addElement(property: JSONProperty) {
        list.add(property)
    }

    override fun accept(v: JSONVisitor) {
        if (v.visit(this)) list.forEach { it.element.accept(v) }
        v.endVisit(this)
    }

}

data class JSONArray(internal var list: MutableList<JSONElement> = mutableListOf()) : JSONNode {
    fun addElement(element: JSONElement) {
        list.add(element)
    }
    override fun accept(v: JSONVisitor) {
        if (v.visit(this)) list.forEach { it.accept(v) }
        v.endVisit(this)
    }

}