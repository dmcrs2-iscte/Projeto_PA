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
            fun addToObject(element: JSONElement) {
                if (jsonNode is JSONObject) jsonNode.addElement(JSONProperty(name, element))
                else if (jsonNode is JSONArray) jsonNode.addElement(element)
            }

            if (annotations.any { it.annotationClass == AsJSONString::class }) {
                when (value) {
                    null -> addToObject(JSONString("null"))
                    else -> addToObject(JSONString(value.toString()))
                }
            } else {
                when (value) {
                    null -> addToObject(JSONEmpty())
                    annotations.any { it.annotationClass == AsJSONString::class } ->
                        addToObject(JSONString(value.toString()))
                    is Number -> {
                        if (value.toDouble() == value.toInt().toDouble()) addToObject(JSONNumber(value.toInt()))
                        else addToObject(JSONFloat(value.toFloat()))
                    }
                    is Boolean -> addToObject(JSONBoolean(value))
                    is Char, is String -> addToObject(JSONString(value.toString()))
                    is Iterable<*> -> {
                        val array = JSONArray()
                        value.forEach { assignJSONTypes(array, value=it, annotations=emptyList()) }
                        addToObject(array)
                    }
                    is Map<*, *> -> {
                        val o = JSONObject()
                        value.forEach { (k, v) -> assignJSONTypes(o, k.toString(), v, annotations=emptyList()) }
                        addToObject(o)
                    }
                    is Enum<*> -> addToObject(JSONString(value.toString()))
                    else -> addToObject(generateJSON(value))
                }
            }
        }
    }

}