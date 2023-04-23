import kotlin.test.Test
import kotlin.test.assertEquals

class TestJSON {

    private val jsonString = JSONString("abc")
    private val jsonNumber = JSONNumber(123)
    private val jsonBoolean = JSONBoolean(true)
    private val jsonEmpty = JSONEmpty()
    private val jsonFloat = JSONFloat(1.23f)
    private val jsonObject1 =
        JSONObject(mutableListOf(JSONProperty("jsonString", jsonString), JSONProperty("jsonNumber", jsonNumber)))
    private val jsonArray = JSONArray(mutableListOf(jsonObject1, jsonBoolean, jsonEmpty))

    private val jsonObject =
        JSONObject(mutableListOf(JSONProperty("jsonArray", jsonArray), JSONProperty("jsonFloat", jsonFloat)))

    @Test
    fun testJSON() {
        jsonObject.addElement(JSONProperty("newString", jsonString))
        val newObject = JSONObject(
            mutableListOf(
                JSONProperty("jsonArray", jsonArray),
                JSONProperty("jsonFloat", jsonFloat),
                JSONProperty("newString", jsonString)
            )
        )
        assertEquals(jsonObject, newObject)
    }

    @Test
    fun testGetValueByName() {
        val array = JSONArray(
            mutableListOf(
                JSONObject(
                    mutableListOf(
                        JSONProperty("numero", JSONNumber(1)),
                        JSONProperty("nome", JSONString("eu"))
                    )
                ),
                JSONObject(
                    mutableListOf(
                        JSONProperty("numero", JSONNumber(2)),
                        JSONProperty("nome", JSONString("tu"))
                    )
                )
            )
        )

        val expected = mutableListOf<JSONElement>(JSONNumber(1), JSONNumber(2))
        assertEquals(expected, array.getValuesByName("numero"))
    }

    @Test
    fun testGetObjectByProperty() {
        val array = JSONArray(
            mutableListOf(
                JSONObject(
                    mutableListOf(
                        JSONProperty("numero", JSONNumber(1)),
                        JSONProperty("nome", JSONString("eu")),
                        JSONProperty("docente", JSONBoolean(true))
                    )
                ),
                JSONObject(
                    mutableListOf(
                        JSONProperty("numero", JSONNumber(2))
                    )
                ),
                JSONObject(
                    mutableListOf(
                        JSONProperty("numero", JSONNumber(3)),
                        JSONProperty("nome", JSONBoolean(true))
                    )
                )
            )
        )

        val expected = mutableListOf(
            JSONObject(
                mutableListOf(
                    JSONProperty("numero", JSONNumber(1)),
                    JSONProperty("nome", JSONString("eu")),
                    JSONProperty("docente", JSONBoolean(true))
                )
            )
        )

        assertEquals(expected, array.getObjectsByProperty(mutableListOf("numero", "nome", "docente")))
    }

    @Test
    fun testCheckPropertyValue() {
        val obj1 = JSONObject(
            mutableListOf(
                JSONProperty( "Inscritos",
                    JSONArray(
                        mutableListOf(
                            JSONObject(
                                mutableListOf(
                                    JSONProperty("numero", JSONNumber(1)),
                                    JSONProperty("nome", JSONString("eu")),
                                    JSONProperty("docente", JSONBoolean(true)),
                                    JSONProperty("altura", JSONFloat(1.73f))
                                )
                            ),
                            JSONObject(
                                mutableListOf(
                                    JSONProperty("numero", JSONNumber(2)),
                                    JSONProperty("data", JSONEmpty())
                                )
                            ),
                            JSONObject(
                                mutableListOf(
                                    JSONProperty("numero", JSONNumber(3)),
                                    JSONProperty("nome", JSONString("tu")),
                                    JSONProperty("data", JSONEmpty()),
                                    JSONProperty("altura", JSONFloat(1.74f))
                                )
                            )
                        )
                    )
                ),
                JSONProperty("Outros",
                    JSONObject(
                        mutableListOf(
                            JSONProperty("numero", JSONNumber(3)),
                            JSONProperty("nome", JSONString("tu")),
                            JSONProperty("data", JSONEmpty()),
                            JSONProperty("altura", JSONFloat(1.74f))
                        )
                    ))
            )
        )

        val obj2 = JSONObject(
            mutableListOf(
                JSONProperty("Outros",
                    JSONArray(
                        mutableListOf(
                            JSONObject(
                                mutableListOf(
                                    JSONProperty("numero", JSONNumber(1)),
                                    JSONProperty("nome", JSONString("eu")),
                                    JSONProperty("docente", JSONBoolean(true)),
                                    JSONProperty("altura", JSONFloat(1.73f))
                                )
                            ),
                            JSONObject(
                                mutableListOf(
                                    JSONProperty("numero", JSONString("2")),
                                    JSONProperty("data", JSONEmpty()),
                                    JSONProperty("docente", JSONEmpty())
                                )
                            ),
                            JSONObject(
                                mutableListOf(
                                    JSONProperty("numero", JSONNumber(3)),
                                    JSONProperty("nome", JSONBoolean(true)),
                                    JSONProperty("data", JSONString("01-02-2023")),
                                    JSONProperty("altura", JSONNumber(2))
                                )
                            )
                        )
                    )
                ),
                JSONProperty("Inscritos",
                    JSONObject(
                        mutableListOf(
                            JSONProperty("numero", JSONNumber(3)),
                            JSONProperty("nome", JSONBoolean(true)),
                            JSONProperty("data", JSONString("01-02-2023")),
                            JSONProperty("altura", JSONNumber(2))
                        )
                    )
                )
            )
        )

        assertEquals(true, obj1.areStrings("nome"))
        assertEquals(true, obj1.areBooleans("docente"))
        assertEquals(true, obj1.areFloats("altura"))
        assertEquals(true, obj1.areNumbers("numero"))
        assertEquals(true, obj1.areNulls("data"))
        assertEquals(true, obj1.arePropertyLists("Outros"))
        assertEquals(true, obj1.areElementLists("Inscritos"))

        assertEquals(false, obj2.areStrings("nome"))
        assertEquals(false, obj2.areBooleans("docente"))
        assertEquals(false, obj2.areFloats("altura"))
        assertEquals(false, obj2.areNumbers("numero"))
        assertEquals(false, obj2.areNulls("data"))
    }
}