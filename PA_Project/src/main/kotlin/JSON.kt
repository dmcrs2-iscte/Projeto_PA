sealed interface JSONElement {
    val value: Any?

    fun accept(v: JSONVisitor)
}

sealed interface JSONLeaf: JSONElement {
    override val value: Any?

    override fun accept(v: JSONVisitor) = v.visit(this)
}

sealed interface JSONNode: JSONElement {
    override val value: MutableList<*>

    val observers: MutableList<JSONObserver>

    fun addObserver(observer: JSONObserver) = observers.add(observer)

    fun getValuesByName(name: String): MutableList<JSONElement> {
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
        val isMatch: (JSONElement) -> Boolean = { predicate(it.value) }
        val visitor = CheckPropertyValues(name, isMatch)
        this.accept(visitor)
        return visitor.isValid()
    }

    private fun listAux(name: String, predicate: (Any?) -> Boolean): Boolean =
        this.arePropertiesOfType(name) { it is List<*> && it.all(predicate) }

    fun areNumbers(name: String): Boolean = this.arePropertiesOfType(name) { it is Int }

    fun areFloats(name: String): Boolean = this.arePropertiesOfType(name) { it is Double }

    fun areNulls(name: String): Boolean = this.arePropertiesOfType(name) { it == null }

    fun areStrings(name: String): Boolean = this.arePropertiesOfType(name) { it is String }

    fun areBooleans(name: String): Boolean = this.arePropertiesOfType(name) { it is Boolean }

    fun isListOfProperties(name: String): Boolean = listAux(name) { it is JSONProperty }

    fun isListOfElements(name: String): Boolean = listAux(name) { it is JSONElement }

    fun isStructuredArray(name: String): Boolean {
        val visitor = CheckArrayStructure(name)
        this.accept(visitor)
        return visitor.isValid()
    }
}

data class JSONProperty(internal val name: String, internal val element: JSONElement) {
    fun accept(v: JSONVisitor) {
        if (v.visit(this)) element.accept(v)
    }

    override fun toString(): String = "\"$name\":$element"

    fun getName(): String = name
    fun getElement(): JSONElement = element
}


data class JSONString(override val value: String): JSONLeaf {
    override fun toString() = "\"$value\""
}

data class JSONNumber(override val value: Int): JSONLeaf {
    override fun toString() = value.toString()
}

data class JSONBoolean(override val value: Boolean): JSONLeaf {
    override fun toString() = value.toString()
}

data class JSONFloat(override val value: Double): JSONLeaf {
    override fun toString() = value.toString()
}

data class JSONEmpty(override val value: Nothing? = null): JSONLeaf {
    override fun toString() = "null"
}

data class JSONObject(override val value: MutableList<JSONProperty> = mutableListOf()): JSONNode {
    override val observers: MutableList<JSONObserver> = mutableListOf()

    fun addElement(element: JSONProperty) {
        if (value.any { it.name == element.name }) throw IllegalArgumentException("A JSONProperty with name '${element.name}' already exists inside this JSONObject")
        else if (value.add(element)) observers.forEach { it.elementAdded() }
    }

    fun removeElement(element: JSONProperty) {
        if (value.remove(element)) observers.forEach { it.elementRemoved() }
    }

    fun replaceElement(oldElement: JSONProperty, newElement: JSONElement) {
        val index = value.indexOf(oldElement)
        val newProperty = JSONProperty(oldElement.name, newElement)
        value[index] = newProperty
        observers.forEach { it.elementReplaced() }
    }

    override fun accept(v: JSONVisitor) {
        if (v.visit(this)) value.forEach { it.accept(v) }
    }

    override fun toString() = value.joinToString (prefix = "{", postfix = "}")
}

data class JSONArray(override val value: MutableList<JSONElement> = mutableListOf()): JSONNode {
    override val observers: MutableList<JSONObserver> = mutableListOf()

    fun addElement(element: JSONElement) {
        if (value.add(element)) observers.forEach { it.elementAdded() }
    }

    fun removeElement(element: JSONElement) {
        if (value.remove(element)) observers.forEach { it.elementRemoved() }
    }

    fun replaceElement(oldElement: JSONElement, newElement: JSONElement) {
        val index = value.indexOf(oldElement)
        value[index] = newElement
        observers.forEach { it.elementReplaced() }
    }

    override fun accept(v: JSONVisitor) {
        if (v.visit(this)) value.forEach { it.accept(v) }
    }

    override fun toString() = value.joinToString(prefix = "[", postfix = "]")
}

interface JSONObserver {
    fun elementAdded()
    fun elementRemoved()
    fun elementReplaced()
}