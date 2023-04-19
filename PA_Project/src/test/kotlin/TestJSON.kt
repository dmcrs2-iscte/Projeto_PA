import kotlin.test.Test

class TestJSON {

    val jsonString = JSONString("abc")
    val jsonNumber = JSONNumber(123)
    val jsonBoolean = JSONBoolean(true)
    val jsonEmpty = JSONEmpty()
    val jsonFloat = JSONFloat(1.23f)
    val jsonObject1 = JSONObject(mutableListOf(JSONProperty("jsonString", jsonString), JSONProperty("jsonNumber", jsonNumber)))
    val jsonArray = JSONArray(mutableListOf(jsonObject1, jsonBoolean, jsonEmpty))

    val jsonObject = JSONObject(mutableListOf(JSONProperty("jsonArray", jsonArray), JSONProperty("jsonFloat", jsonFloat)))

    @Test
    fun testJSON(){
        jsonObject.addElement(JSONProperty("newString", jsonString))
        val newObject = JSONObject(mutableListOf(JSONProperty("jsonArray", jsonArray), JSONProperty("jsonFloat", jsonFloat), JSONProperty("newString", jsonString)))
        println(jsonObject == newObject)
    }
}