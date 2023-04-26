import kotlin.reflect.full.memberProperties

class JSONGenerator {
    @Target(AnnotationTarget.PROPERTY)
    annotation class ExcludeFromJSON

    @Target(AnnotationTarget.PROPERTY)
    annotation class AsJSONString

    @Target(AnnotationTarget.PROPERTY)
    annotation class UseName(val name: String)

    companion object GenerateJSON {
        fun generateJSON(instance: Any): JSONObject {
            val jsonObject = JSONObject()
            val properties = instance::class.memberProperties
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

        private fun assignJSONTypes(jsonNode: JSONNode<*>, name: String="", value: Any?, annotations: List<Annotation>) {
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
                    annotations.any { it.annotationClass == AsJSONString::class } ->
                        addToNode(JSONString(value.toString()))
                    is Number -> {
                        if (value.toDouble() == value.toInt().toDouble()) addToNode(JSONNumber(value.toInt()))
                        else addToNode(JSONFloat(value.toFloat()))
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

}