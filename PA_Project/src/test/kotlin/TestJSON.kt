import kotlin.test.*

class TestJSON {

    @Test
    fun testAddElement() {
        val jsonObject = JSONObject(mutableListOf(
            JSONProperty("first name", JSONString("John")),
            JSONProperty("last name", JSONString("Doe")),
            JSONProperty("age", JSONNumber(35)),
            JSONProperty("courses", JSONArray(mutableListOf(
                JSONString("CALC"),
                JSONString("ALG"),
                JSONString("PROG")
            )))
        ))

        jsonObject.addElement(JSONProperty("teacher", JSONBoolean(true)))

        val expected = JSONObject(mutableListOf(
            JSONProperty("first name", JSONString("John")),
            JSONProperty("last name", JSONString("Doe")),
            JSONProperty("age", JSONNumber(35)),
            JSONProperty("courses", JSONArray(mutableListOf(
                JSONString("CALC"),
                JSONString("ALG"),
                JSONString("PROG")
            ))),
            JSONProperty("teacher", JSONBoolean(true))
        ))

        assertEquals(expected, jsonObject)

        assertFailsWith<IllegalArgumentException> {
            jsonObject.addElement(JSONProperty("teacher", JSONBoolean(true)))
        }
    }

    @Test
    fun testRemoveElement() {
        val jsonObject = JSONObject(mutableListOf(
            JSONProperty("first name", JSONString("John")),
            JSONProperty("last name", JSONString("Doe")),
            JSONProperty("age", JSONNumber(35)),
            JSONProperty("courses", JSONArray(mutableListOf(
                JSONString("CALC"),
                JSONString("ALG"),
                JSONString("PROG")
            )))
        ))

        jsonObject.removeElement(JSONProperty("age", JSONNumber(35)))

        val expected = JSONObject(mutableListOf(
            JSONProperty("first name", JSONString("John")),
            JSONProperty("last name", JSONString("Doe")),
            JSONProperty("courses", JSONArray(mutableListOf(
                JSONString("CALC"),
                JSONString("ALG"),
                JSONString("PROG")
            )))
        ))

        assertEquals(expected, jsonObject)

        jsonObject.removeElement(JSONProperty("age", JSONNumber(35)))
        assertEquals(expected, jsonObject)
    }

    @Test
    fun testReplaceElement() {
        val jsonObject = JSONObject(mutableListOf(
            JSONProperty("first name", JSONString("John")),
            JSONProperty("last name", JSONString("Doe")),
            JSONProperty("age", JSONNumber(35)),
            JSONProperty("courses", JSONArray(mutableListOf(
                JSONString("CALC"),
                JSONString("ALG"),
                JSONString("PROG")
            )))
        ))

        jsonObject.replaceElement("age", JSONNumber(45))
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
                                    JSONProperty("altura", JSONFloat(1.73))
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
                                    JSONProperty("altura", JSONFloat(1.74))
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
                            JSONProperty("altura", JSONFloat(1.74))
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
                                    JSONProperty("altura", JSONFloat(1.73))
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
                                    JSONProperty("altura", JSONFloat(1.73))
                                )
                            ),
                            JSONObject(
                                mutableListOf(
                                    JSONProperty("numero", JSONNumber(1)),
                                    JSONProperty("nome", JSONString("eu")),
                                    JSONProperty("docente", JSONBoolean(true)),
                                    JSONProperty("altura", JSONFloat(1.73))
                                )
                            ),
                            JSONObject(
                                mutableListOf(
                                    JSONProperty("numero", JSONNumber(1)),
                                    JSONProperty("nome", JSONString("eu")),
                                    JSONProperty("docente", JSONBoolean(true)),
                                    JSONProperty("altura", JSONFloat(1.73))
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
                                    JSONProperty("altura", JSONFloat(1.73))
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
                                    JSONProperty("altura", JSONFloat(1.74))
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
                                    JSONProperty("altura", JSONFloat(1.73))
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
                                    JSONProperty("altura", JSONFloat(1.74))
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
        val adse: Char? = null,
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
            mapOf("MEC" to 1, "ROB" to 2,"TAM" to 3,"MER" to 4,"BOA" to 5), true, Professor(40, "Pedro"), 'a',
            "WillBeExcluded"
        )

        val json = JSONGenerator.generateJSON(student)

        val preExpected = JSONObject(mutableListOf(JSONProperty("newName", JSONString("1")), JSONProperty("name", JSONString("eu")),
            JSONProperty("birth", JSONEmpty()), JSONProperty("height", JSONFloat(1.73)), JSONProperty("courses", JSONArray(
                mutableListOf(JSONString("MEC"),JSONString("ROB"),JSONString("TAM"),JSONString("MER"),JSONString("BOA")))),
            JSONProperty("scores", JSONObject(mutableListOf(JSONProperty("MEC", JSONNumber(1)), JSONProperty("ROB", JSONNumber(2)),
                JSONProperty("TAM", JSONNumber(3)), JSONProperty("MER", JSONNumber(4)), JSONProperty("BOA", JSONNumber(5))))),
            JSONProperty("valid", JSONBoolean(true)), JSONProperty("professor", JSONObject(mutableListOf(JSONProperty("age", JSONNumber(40)),
                JSONProperty("name", JSONString("Pedro"))))), JSONProperty("adse", JSONString("a")), JSONProperty("type", JSONString("Bachelor"))))

        val expected = JSONObject( preExpected.value.sortedBy { it.getName() }.toMutableList())

        println(JSONObject().toTree())

        assertEquals(expected, json)
    }

    @Test
    fun testJsonReplaceElement() {
        val jsonObject = JSONObject(mutableListOf(
            JSONProperty("a", JSONNumber(1)),
            JSONProperty("b", JSONNumber(2)),
            JSONProperty("c", JSONNumber(3)),
            JSONProperty("obj", JSONObject(mutableListOf(
                JSONProperty("x", JSONNumber(101)),
                JSONProperty("y", JSONNumber(102)),
                JSONProperty("z", JSONNumber(103))
            ))
        )))

        jsonObject.replaceElement("c", JSONNumber(4))
    }
}