sealed interface JSONElement {
    fun accept(v: JSONVisitor)
}

sealed interface JSONLeaf: JSONElement{
    val value: Any?

    override fun accept(v: JSONVisitor) {
        v.visit(this)
    }
}

sealed interface JSONNode<T>: JSONElement {
    val list: MutableList<T>

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

    private fun arePropertiesOfType(name: String, predicate: (Any?) -> Boolean): Boolean {
        val isMatch: (JSONElement) -> Boolean = { element: JSONElement ->
            when (element) {
                is JSONLeaf -> predicate(element.value)
                is JSONNode<*> -> predicate(element.list)
            }
        }
        val visitor = CheckPropertyValues(name, isMatch)
        this.accept(visitor)
        return visitor.isValid()
    }

    fun areNumbers(name: String): Boolean = this.arePropertiesOfType(name) { it is Int }
    fun areFloats(name: String): Boolean = this.arePropertiesOfType(name) { it is Float }
    fun areNulls(name: String): Boolean = this.arePropertiesOfType(name) { it == null }
    fun areStrings(name: String): Boolean = this.arePropertiesOfType(name) { it is String }
    fun areBooleans(name: String): Boolean = this.arePropertiesOfType(name) { it is Boolean }

    fun arePropertyLists(name: String): Boolean = this.arePropertiesOfType(name) { this.arePropertiesOfType(name) {
        it is List<*> && it.all { item -> item is JSONProperty } } }

    fun areElementLists(name: String): Boolean = this.arePropertiesOfType(name) { this.arePropertiesOfType(name) {
        it is List<*> && it.all { item -> item is JSONElement } } }

    fun isStructuredArray(name: String): Boolean{
        val visitor = CheckArrayStructure(name)
        this.accept(visitor)
        return visitor.isValid()
    }
}

data class JSONProperty(internal val name: String, internal val element: JSONElement) {
    fun accept(v: JSONVisitor) {
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

data class JSONObject(override val list: MutableList<JSONProperty>): JSONNode<JSONProperty> {
    fun addElement(property: JSONProperty) {
        list.add(property)
    }

    override fun accept(v: JSONVisitor) {
        if (v.visit(this)) list.forEach { it.accept(v) }
    }

    override fun toString(): String {
        return list.joinToString (prefix = "{", postfix = "}")
    }
}

data class JSONArray(override val list: MutableList<JSONElement>) : JSONNode<JSONElement> {
    fun addElement(element: JSONElement) {
        list.add(element)
    }

    override fun accept(v: JSONVisitor) {
        if (v.visit(this)) list.forEach { it.accept(v) }
    }

    override fun toString(): String {
        return list.joinToString (prefix = "[", postfix = "]")
    }
}