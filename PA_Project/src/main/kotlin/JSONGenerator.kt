import kotlin.reflect.full.memberProperties

class JSONGenerator {

    companion object GenerateJSON {
        fun generateJSON(instance: Any): JSONObject {
            val jsonObject = JSONObject()
            val properties = instance::class.memberProperties
            properties.forEach { p ->
                val value = p.call(instance)
                assignJSONTypes(jsonObject, p.name, value)
            }
            return jsonObject
        }

        private fun assignJSONTypes(jsonNode: JSONNode<*>, name: String="", value: Any?) {
            fun addToObject(element: JSONElement) {
                if (jsonNode is JSONObject) jsonNode.addElement(JSONProperty(name, element))
                else if (jsonNode is JSONArray) jsonNode.addElement(element)
            }

            when (value) {
                null -> addToObject(JSONEmpty())
                is Number -> {
                    if (value.toDouble() == value.toInt().toDouble()) addToObject(JSONNumber(value.toInt()))
                    else addToObject(JSONFloat(value.toFloat()))
                }
                is Boolean -> addToObject(JSONBoolean(value))
                is Char, is String -> addToObject(JSONString(value.toString()))
                is Collection<*> -> {
                    val array = JSONArray()
                    value.forEach { assignJSONTypes(array, value=it) }
                    addToObject(array)
                }
                is Map<*, *> -> {
                    val o = JSONObject()
                    value.forEach { (k, v) -> assignJSONTypes(o, k.toString(), v) }
                    addToObject(o)
                }
                is Enum<*> -> addToObject(JSONString(value.toString()))
                else -> addToObject(generateJSON(value))
            }
        }
    }

}