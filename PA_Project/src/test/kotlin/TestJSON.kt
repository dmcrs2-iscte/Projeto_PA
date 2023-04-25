import kotlin.test.*

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
        val newObject = JSONObject(
            mutableListOf(
                JSONProperty("jsonArray", jsonArray),
                JSONProperty("jsonFloat", jsonFloat),
                JSONProperty("newString", jsonString)
            )
        )
        assertEquals(jsonObject, newObject)
        assertFailsWith<IllegalArgumentException> { jsonObject.addElement(JSONProperty("newString", jsonEmpty)) }
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

        assertTrue(obj1.areStrings("nome"))
        assertTrue(obj1.areBooleans("docente"))
        assertTrue(obj1.areFloats("altura"))
        assertTrue(obj1.areNumbers("numero"))
        assertTrue(obj1.areNulls("data"))
        assertTrue(obj1.isListOfProperties("Outros"))
        assertTrue(obj1.isListOfElements("Inscritos"))

        assertFalse(obj2.areStrings("nome"))
        assertFalse(obj2.areBooleans("docente"))
        assertFalse(obj2.areFloats("altura"))
        assertFalse(obj2.areNumbers("numero"))
        assertFalse(obj2.areNulls("data"))
        assertFalse(obj2.isListOfProperties("Outros"))
        assertFalse(obj2.isListOfElements("Inscritos"))
    }

    @Test
    fun testCheckArrayInternalStructure(){
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
                                    JSONProperty("numero", JSONNumber(1)),
                                    JSONProperty("nome", JSONString("eu")),
                                    JSONProperty("docente", JSONBoolean(true)),
                                    JSONProperty("altura", JSONFloat(1.73f))
                                )
                            ),
                            JSONObject(
                                mutableListOf(
                                    JSONProperty("numero", JSONNumber(1)),
                                    JSONProperty("nome", JSONString("eu")),
                                    JSONProperty("docente", JSONBoolean(true)),
                                    JSONProperty("altura", JSONFloat(1.73f))
                                )
                            )
                        )
                    )
                )
            )
        )

        val obj2 = JSONObject(
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
                )
            )
        )

        val obj3 = JSONObject(
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
                            ),
                            JSONEmpty()
                        )
                    )
                )
            )
        )

        assertTrue(obj1.isStructuredArray("Inscritos"))
        assertFalse(obj2.isStructuredArray("Inscritos"))
        assertFalse(obj3.isStructuredArray("Inscritos"))
    }

    data class Student(
        @JSONGenerator.AsJSONString
        @JSONGenerator.UseName("newName")
        val number: Int,
        val name: String,
        val type: StudentType? = null,
        val birth: String? = null,
        val height: Double,
        val courses: Collection<String>,
        val scores: Map<String, Int>,
        val valid: Boolean,
        val professor: Professor,
        @JSONGenerator.ExcludeFromJSON
        val toExclude: String
    )

    data class Professor(
        val age: Int,
        val name: String
    )

    enum class StudentType {
        Bachelor//, Master, Doctoral
    }

    @Test
    fun testJsonGenerator() {
        val student = Student(1, "eu", StudentType.Bachelor, null, 1.73, listOf("MEC","ROB","TAM","MER","BOA"),
            mapOf("MEC" to 1, "ROB" to 2,"TAM" to 3,"MER" to 4,"BOA" to 5), true, Professor(40, "Pedro"),
            "WillBeExcluded"
        )

        val json = JSONGenerator.generateJSON(student)

        val expected = JSONObject(mutableListOf(JSONProperty("newName", JSONString("1")), JSONProperty("name", JSONString("eu")),
            JSONProperty("birth", JSONEmpty()), JSONProperty("height", JSONFloat(1.73f)), JSONProperty("courses", JSONArray(
                mutableListOf(JSONString("MEC"),JSONString("ROB"),JSONString("TAM"),JSONString("MER"),JSONString("BOA")))),
            JSONProperty("scores", JSONObject(mutableListOf(JSONProperty("MEC", JSONNumber(1)), JSONProperty("ROB", JSONNumber(2)),
                JSONProperty("TAM", JSONNumber(3)), JSONProperty("MER", JSONNumber(4)), JSONProperty("BOA", JSONNumber(5))))),
            JSONProperty("valid", JSONBoolean(true)), JSONProperty("professor", JSONObject(mutableListOf(JSONProperty("age", JSONNumber(40)),
                JSONProperty("name", JSONString("Pedro"))))), JSONProperty("type", JSONString("Bachelor"))))

        expected.value.sortBy { it.getName() }

        assertEquals(expected, json)
    }
}