sealed interface JSONElement {
    val value: Any?

    fun accept(v: JSONVisitor)
}

sealed interface JSONLeaf: JSONElement {
    override val value: Any?

    override fun accept(v: JSONVisitor) = v.visit(this)
}

sealed interface JSONNode: JSONElement, JSONObserver {
    override val value: MutableList<*>

    val observers: MutableList<JSONObserver>

    fun addObserver(observer: JSONObserver) = observers.add(observer)
    fun removeObserver(observer: JSONObserver) = observers.remove(observer)

    override fun elementAdded() {
        observers.forEach { it.elementAdded() }
    }

    override fun elementRemoved() {
        observers.forEach { it.elementRemoved() }
    }

    override fun elementReplaced() {
        observers.forEach { it.elementReplaced() }
    }

    fun getElementsByKey(key: String): MutableList<JSONElement> {
        val visitor = GetElementsByKey(key)
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

    fun toTree(tabs: Int = 1): String {
        fun getIndentation(tbs: Int): String = "\n" + "\t".repeat(tbs)

        var jsonToString = if (this is JSONObject) "{" else "["
        value.forEach {
            jsonToString += getIndentation(tabs) +
            when (it) {
                is JSONNode -> it.toTree(tabs + 1) + ","
                is JSONProperty -> it.toTree(tabs) + ","
                else -> it.toString() + ","
            }
        }
        if (this.value.isNotEmpty()) jsonToString = jsonToString.dropLast(1)

        jsonToString += getIndentation(tabs - 1) + if (this is JSONObject) "}" else "]"
        return jsonToString
    }
}

data class JSONProperty(internal val name: String, internal val element: JSONElement) {
    init {
        require(name.isNotEmpty()) { "Property name must not be empty" }
    }

    fun accept(v: JSONVisitor) {
        if (v.visit(this)) element.accept(v)
    }

    override fun toString(): String = "\"$name\":$element"

    fun getName(): String = name
    fun getElement(): JSONElement = element

    internal fun toTree(tabs: Int): String{
        return name + " : " + if(element is JSONNode) element.toTree(tabs + 1) else element.toString()
    }
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
    init{
        if (value.isNotEmpty()) value.forEach { if (it.element is JSONNode) it.element.addObserver(this) }
    }

    override val observers: MutableList<JSONObserver> = mutableListOf()

    fun addElement(property: JSONProperty) {
        if (value.any { it.name == property.name })
            throw IllegalArgumentException("Cannot add another property with key ${property.name}")
        else if (value.add(property)) observers.forEach { it.elementAdded() }

        if (property.element is JSONNode) property.element.addObserver(this)
    }

    fun removeElement(key: String) {
        val elementToRemove = value.find { it.name == key }
        if (elementToRemove != null) {
            if (elementToRemove.element is JSONNode) (elementToRemove.element).removeObserver(this)
            value.remove(elementToRemove)
            observers.forEach { it.elementRemoved() }
        }
    }

    fun replaceElement(propertyName: String, newElement: JSONElement) {
        val index = value.indexOf(value.find { it.name == propertyName })
        if (index != -1) {
            value[index] = JSONProperty(propertyName, newElement)
            observers.forEach { o -> o.elementReplaced() }
        }
    }

    override fun accept(v: JSONVisitor) {
        if (v.visit(this)) value.forEach { it.accept(v) }
    }

    override fun toString() = value.joinToString (prefix = "{", postfix = "}")
}

data class JSONArray(override val value: MutableList<JSONElement> = mutableListOf()): JSONNode {
    init{
        if (value.isNotEmpty()) value.forEach { if (it is JSONNode) it.addObserver(this) }
    }

    override val observers: MutableList<JSONObserver> = mutableListOf()

    fun addElement(element: JSONElement) {
        if (value.add(element)) observers.forEach { it.elementAdded() }
        if (element is JSONNode) element.addObserver(this)
    }

    fun removeElement(element: JSONElement) {
        val elementToRemove = value.find { it == element }
        if (elementToRemove != null) {
            if (elementToRemove is JSONNode) elementToRemove.removeObserver(this)
            value.remove(elementToRemove)
            observers.forEach { it.elementRemoved() }
        }
    }

    fun replaceElement(oldElement: JSONElement, newElement: JSONElement) {
        val index = value.indexOf(oldElement)
        if (index != -1) {
            value[index] = newElement
            observers.forEach { it.elementReplaced() }
        }
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