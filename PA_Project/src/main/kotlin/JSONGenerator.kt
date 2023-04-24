import java.text.CollationElementIterator
import kotlin.reflect.*
import kotlin.reflect.full.memberProperties

class JSONGenerator {

    fun generateJSON(instance: Any): JSONObject {
        val jsonObject = JSONObject()
        val clazz = instance::class
        val properties = clazz.memberProperties
        properties.forEach { prop ->
            val value = prop.call(instance)
            assignJsonTypes(jsonObject, prop.name, value)
        }
        return jsonObject
    }

    private fun assignJsonTypes(jsonNode: JSONNode<*>, name: String="", value: Any? ) {
        fun addElement(element: JSONElement) {
            if (jsonNode is JSONObject) {
                jsonNode.addElement(JSONProperty(name, element))
            } else if (jsonNode is JSONArray) {
                jsonNode.addElement(element)
            }
        }

        when (value) {
            null -> addElement(JSONEmpty())
            is Number ->{
                if(value.toDouble() == value.toInt().toDouble()) addElement(JSONNumber(value.toInt()))
                else addElement(JSONFloat(value.toFloat()))
            }
            is Boolean -> addElement(JSONBoolean(value))
            is Char, is String -> addElement(JSONString(value.toString()))
            is Collection<*> -> {
                val array = JSONArray()
                value.forEach { assignJsonTypes(array, value = it) }
                addElement(array)
            }
            is Map<*, *> -> {
                val obj = JSONObject()
                value.forEach { (key, value) -> assignJsonTypes(obj, key.toString(), value) }
                addElement(obj)
            }
            /*is Enum<*> -> {
                val obj = JSONObject()

            }*/

        }
    }
}