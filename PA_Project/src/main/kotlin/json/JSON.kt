package json

sealed interface JSONElement {
    val value: Any?

    fun accept(v: JSONVisitor)
}

sealed interface JSONLeaf : JSONElement {
    override val value: Any?

    override fun accept(v: JSONVisitor) = v.visit(this)
}

sealed interface JSONNode : JSONElement, JSONObserver {
    override val value: List<*>

    val observers: MutableList<JSONObserver>

    fun addObserver(observer: JSONObserver) = observers.add(observer)

    fun removeObserver(observer: JSONObserver) = observers.remove(observer)

    override fun elementAdded() = observers.forEach { it.elementAdded() }

    override fun elementRemoved() = observers.forEach { it.elementRemoved() }

    override fun elementReplaced() = observers.forEach { it.elementReplaced() }

    fun getElementsByKey(key: String): List<JSONElement> {
        val visitor = GetElementsByKey(key)
        this.accept(visitor)
        return visitor.getValues()
    }

    fun getObjectsByProperties(properties: List<String>): List<JSONObject> {
        val visitor = GetObjectsByProperties(properties)
        this.accept(visitor)
        return visitor.getObjects()
    }

    private fun arePropertiesOfType(name: String, predicate: (Any?) -> Boolean): Boolean {
        val isMatch: (JSONElement) -> Boolean = { predicate(it.value) }
        val visitor = ArePropertiesOfType(name, isMatch)
        this.accept(visitor)
        return visitor.isValid()
    }

    private fun listAux(name: String, predicate: (Any?) -> Boolean): Boolean =
        this.arePropertiesOfType(name) { it is List<*> && it.all(predicate) }

    fun areNumbers(key: String): Boolean = this.arePropertiesOfType(key) { it is Number }

    fun areNulls(key: String): Boolean = this.arePropertiesOfType(key) { it == null }

    fun areStrings(key: String): Boolean = this.arePropertiesOfType(key) { it is String }

    fun areBooleans(key: String): Boolean = this.arePropertiesOfType(key) { it is Boolean }

    fun areObjects(key: String): Boolean = listAux(key) { it is JSONProperty }

    fun areArrays(key: String): Boolean = listAux(key) { it is JSONElement }

    fun arrayIsStructured(key: String): Boolean {
        val visitor = CheckArrayStructure(key)
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

    internal fun toTree(tabs: Int): String {
        return "\"$name\"" + " : " + if (element is JSONNode) element.toTree(tabs + 1) else element.toString()
    }
}

data class JSONString(override val value: String) : JSONLeaf {
    override fun toString() = "\"$value\""
}

data class JSONBoolean(override val value: Boolean) : JSONLeaf {
    override fun toString() = value.toString()
}

data class JSONNumber(override val value: Number) : JSONLeaf {
    override fun toString() = value.toString()
}

data class JSONNull(override val value: Nothing? = null) : JSONLeaf {
    override fun toString() = "null"
}

data class JSONObject(private val mutableList: MutableList<JSONProperty> = mutableListOf()) : JSONNode {
    override val value: List<JSONProperty>
        get() = mutableList.toList()

    init {
        if (mutableList.isNotEmpty()) mutableList.forEach { if (it.element is JSONNode) it.element.addObserver(this) }
    }

    override val observers: MutableList<JSONObserver> = mutableListOf()

    fun addElement(property: JSONProperty) {
        if (mutableList.any { it.name == property.name })
            throw IllegalArgumentException("Cannot add another property with key ${property.name}")
        else if (mutableList.add(property)) observers.forEach { it.elementAdded() }

        if (property.element is JSONNode) property.element.addObserver(this)
    }

    fun removeElement(key: String) {
        val elementToRemove = mutableList.find { it.name == key }
        if (elementToRemove != null) {
            if (elementToRemove.element is JSONNode) (elementToRemove.element).removeObserver(this)
            mutableList.remove(elementToRemove)
            observers.forEach { it.elementRemoved() }
        }
    }

    fun replaceElement(propertyName: String, newElement: JSONElement) {
        val index = mutableList.indexOf(mutableList.find { it.name == propertyName })
        if (index != -1) {
            mutableList[index] = JSONProperty(propertyName, newElement)
            observers.forEach { o -> o.elementReplaced() }
        }
    }

    override fun accept(v: JSONVisitor) {
        if (v.visit(this)) mutableList.forEach { it.accept(v) }
    }

    override fun toString() = mutableList.joinToString(prefix = "{", postfix = "}")
}

data class JSONArray(private val mutableList: MutableList<JSONElement> = mutableListOf()) : JSONNode {
    override val value: List<JSONElement>
        get() = mutableList.toList()

    init {
        if (mutableList.isNotEmpty()) mutableList.forEach { if (it is JSONNode) it.addObserver(this) }
    }

    override val observers: MutableList<JSONObserver> = mutableListOf()

    fun addElement(element: JSONElement) {
        if (mutableList.add(element)) observers.forEach { it.elementAdded() }
        if (element is JSONNode) element.addObserver(this)
    }

    fun removeElement(element: JSONElement) {
        val elementToRemove = mutableList.find { it == element }
        if (elementToRemove != null) {
            if (elementToRemove is JSONNode) elementToRemove.removeObserver(this)
            mutableList.remove(elementToRemove)
            observers.forEach { it.elementRemoved() }
        }
    }

    fun replaceElement(oldElement: JSONElement, newElement: JSONElement) {
        val index = mutableList.indexOf(oldElement)
        if (index != -1) {
            mutableList[index] = newElement
            observers.forEach { it.elementReplaced() }
        }
    }

    override fun accept(v: JSONVisitor) {
        if (v.visit(this)) mutableList.forEach { it.accept(v) }
    }

    override fun toString() = mutableList.joinToString(prefix = "[", postfix = "]")
}

interface JSONObserver {
    fun elementAdded()
    fun elementRemoved()
    fun elementReplaced()
}