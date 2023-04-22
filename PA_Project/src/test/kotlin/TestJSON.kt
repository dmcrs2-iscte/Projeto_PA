import kotlin.test.Test
import kotlin.test.assertEquals

class TestJSON {

    private val jsonString = JSONString("abc")
    private val jsonNumber = JSONNumber(123)
    private val jsonBoolean = JSONBoolean(true)
    private val jsonEmpty = JSONEmpty()
    private val jsonFloat = JSONFloat(1.23f)
    private val jsonObject1 = JSONObject(mutableListOf(JSONProperty("jsonString", jsonString), JSONProperty("jsonNumber", jsonNumber)))
    private val jsonArray = JSONArray(mutableListOf(jsonObject1, jsonBoolean, jsonEmpty))

    private val jsonObject = JSONObject(mutableListOf(JSONProperty("jsonArray", jsonArray), JSONProperty("jsonFloat", jsonFloat)))

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
        assertEquals(expected, array.getValuesByName("numero"))
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

        assertEquals(expected, array.getObjectsByProperty(mutableListOf("numero", "nome", "docente")))
    }
}