import kotlin.test.Test
import kotlin.test.assertEquals

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
    fun testJSON() {
        jsonObject.addElement(JSONProperty("newString", jsonString))
        val newObject = JSONObject(mutableListOf(JSONProperty("jsonArray", jsonArray), JSONProperty("jsonFloat", jsonFloat), JSONProperty("newString", jsonString)))
        assertEquals(jsonObject, newObject)
    }

    @Test
    fun testGetValueByName() {
        val array = JSONArray(mutableListOf(
            JSONObject(mutableListOf(
                JSONProperty("numero", JSONNumber(1)),
                JSONProperty("nome", JSONString("eu"))
            )),
            JSONObject(mutableListOf(
                JSONProperty("numero", JSONNumber(2)),
                JSONProperty("nome", JSONString("tu"))
            ))
        )
        )

        val expected = mutableListOf<JSONElement>(JSONNumber(1), JSONNumber(2))
        val visitor = GetValuesByName("numero")
        array.accept(visitor)
        assertEquals(expected, visitor.getValues())
    }

    @Test
    fun testGetObjectByProperty() {
        val array = JSONArray(mutableListOf(
            JSONObject(mutableListOf(
                JSONProperty("numero", JSONNumber(1)),
                JSONProperty("nome", JSONString("eu")),
                JSONProperty("docente", JSONBoolean(true))
            )),
            JSONObject(mutableListOf(
                JSONProperty("numero", JSONNumber(2))
            )),
            JSONObject(mutableListOf(
                JSONProperty("numero", JSONNumber(3)),
                JSONProperty("nome", JSONBoolean(true))
            ))
        )
        )

        val expected = mutableListOf(JSONObject(mutableListOf(
            JSONProperty("numero", JSONNumber(1)),
            JSONProperty("nome", JSONString("eu")),
            JSONProperty("docente", JSONBoolean(true))
        )))

        val visitor = GetObjectsByProperty(mutableListOf("numero", "nome", "docente"))
        array.accept(visitor)
        assertEquals(expected, visitor.getObjects())
    }
}