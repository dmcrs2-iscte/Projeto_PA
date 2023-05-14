import java.lang.IllegalArgumentException
import kotlin.reflect.full.declaredMemberProperties

class JSONGenerator : Subject {
    @Target(AnnotationTarget.PROPERTY)
    annotation class ExcludeFromJSON

    @Target(AnnotationTarget.PROPERTY)
    annotation class AsJSONString

    @Target(AnnotationTarget.PROPERTY)
    annotation class UseName(val name: String)

    private val observers = mutableListOf<Observer>()

    companion object GenerateJSON {
        fun generateJSON(instance: Any): JSONObject {
            if (!instance::class.isData) throw IllegalArgumentException("Argument is not an object of data class")
            val jsonObject = JSONObject()
            val properties = instance::class.declaredMemberProperties
            properties.forEach { p ->
                if (p.annotations.none { it.annotationClass == ExcludeFromJSON::class }) {
                    val value = p.call(instance)
                    if (p.annotations.any { it.annotationClass == UseName::class }) {
                        val useNameAnnotation = p.annotations.first { it.annotationClass == UseName::class } as UseName
                        assignJSONTypes(jsonObject, useNameAnnotation.name, value, p.annotations)
                    } else {
                        assignJSONTypes(jsonObject, p.name, value, p.annotations)
                    }
                }
            }
            return jsonObject
        }

        private fun assignJSONTypes(jsonNode: JSONNode, name: String="", value: Any?, annotations: List<Annotation>) {
            fun addToNode(element: JSONElement) {
                if (jsonNode is JSONObject) jsonNode.addElement(JSONProperty(name, element))
                else if (jsonNode is JSONArray) jsonNode.addElement(element)
            }

            if (annotations.any { it.annotationClass == AsJSONString::class }) {
                when (value) {
                    null -> addToNode(JSONString("null"))
                    else -> addToNode(JSONString(value.toString()))
                }
            } else {
                when (value) {
                    null -> addToNode(JSONEmpty())
                    is Number -> {
                        if (value.toDouble() == value.toInt().toDouble()) addToNode(JSONNumber(value.toInt()))
                        else addToNode(JSONFloat(value.toDouble()))
                    }
                    is Boolean -> addToNode(JSONBoolean(value))
                    is Char, is String -> addToNode(JSONString(value.toString()))
                    is Iterable<*> -> {
                        val array = JSONArray()
                        value.forEach { assignJSONTypes(array, value=it, annotations=emptyList()) }
                        addToNode(array)
                    }
                    is Map<*, *> -> {
                        val o = JSONObject()
                        value.forEach { (k, v) -> assignJSONTypes(o, k.toString(), v, annotations=emptyList()) }
                        addToNode(o)
                    }
                    is Enum<*> -> addToNode(JSONString(value.toString()))
                    else -> addToNode(generateJSON(value))
                }
            }
        }
    }

    override fun addObserver(observer: Observer) {
        observers.add(observer)
    }

    override fun removeObserver(observer: Observer) {
        observers.remove(observer)
    }

    override fun notifyObservers() {
        observers.forEach { it.update(this) }
    }
}