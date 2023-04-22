sealed interface JSONElement {
    fun accept(v: JSONVisitor)
}

sealed interface JSONLeaf: JSONElement{
    val value: Any?

    override fun accept(v: JSONVisitor) {
        v.visit(this)
    }
}

sealed interface JSONNode: JSONElement {
    fun getValuesByName(name: String): MutableList<JSONElement>{
        val visitor = GetValuesByName(name)
        this.accept(visitor)
        return visitor.getValues()
    }

    fun getObjectsByProperty(properties: List<String>): MutableList<JSONObject> {
        val visitor = GetObjectsByProperty(properties)
        this.accept(visitor)
        return visitor.getObjects()
    }
}

data class JSONProperty(internal val name: String, internal val element: JSONElement): JSONElement {
    override fun accept(v: JSONVisitor) {
        if (v.visit(this)) element.accept(v)
    }

    override fun toString(): String {
        return "\"$name\":$element"
    }
}


data class JSONString(override val value: String): JSONLeaf {
    override fun toString(): String {
        return "\"$value"
    }
}

data class JSONNumber(override val value: Int): JSONLeaf {
    override fun toString(): String {
        return value.toString()
    }
}

data class JSONBoolean(override val value: Boolean): JSONLeaf {
    override fun toString(): String {
        return value.toString()
    }
}

data class JSONFloat(override val value: Float): JSONLeaf {
    override fun toString(): String {
        return value.toString()
    }
}

data class JSONEmpty(override val value: Nothing? = null): JSONLeaf {
    override fun toString(): String {
        return "null"
    }
}


data class JSONObject(internal var list: MutableList<JSONProperty> = mutableListOf()): JSONNode {
    fun addElement(property: JSONProperty) {
        list.add(property)
    }

    override fun accept(v: JSONVisitor) {
        if (v.visit(this)) list.forEach { it.accept(v) }
        v.endVisit(this)
    }

    override fun toString(): String {
        return list.joinToString (prefix = "{", postfix = "}")
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

    override fun toString(): String {
        return list.joinToString (prefix = "[", postfix = "]")
    }
}