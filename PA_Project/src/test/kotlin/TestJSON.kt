import json.*
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
                JSONString("ALG")
            ))
            )
        ))

        val jsonArray = JSONArray(mutableListOf(
            JSONNumber(1),
            JSONNumber(2)
        ))

        jsonObject.addElement(JSONProperty("teacher", JSONBoolean(true)))
        jsonArray.addElement(JSONNumber(3))

        val expectedJsonObject = JSONObject(mutableListOf(
            JSONProperty("first name", JSONString("John")),
            JSONProperty("last name", JSONString("Doe")),
            JSONProperty("age", JSONNumber(35)),
            JSONProperty("courses", JSONArray(mutableListOf(
                JSONString("CALC"),
                JSONString("ALG")
            ))
            ),
            JSONProperty("teacher", JSONBoolean(true))
        ))

        val expectedJsonArray = JSONArray(mutableListOf(
            JSONNumber(1),
            JSONNumber(2),
            JSONNumber(3)
        ))

        assertEquals(expectedJsonObject, jsonObject)
        assertEquals(expectedJsonArray, jsonArray)

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
                JSONString("ALG")
            ))
            )
        ))

        val jsonArray = JSONArray(mutableListOf(
            JSONNumber(1),
            JSONNumber(2)
        ))

        jsonObject.removeElement("age")
        jsonArray.removeElement(JSONNumber(2))

        val expectedJsonObject = JSONObject(mutableListOf(
            JSONProperty("first name", JSONString("John")),
            JSONProperty("last name", JSONString("Doe")),
            JSONProperty("courses", JSONArray(mutableListOf(
                JSONString("CALC"),
                JSONString("ALG")
            ))
            )
        ))

        val expectedJsonArray = JSONArray(mutableListOf(
            JSONNumber(1)
        ))

        assertEquals(expectedJsonObject, jsonObject)
        assertEquals(expectedJsonArray, jsonArray)

        jsonObject.removeElement("age")
        assertEquals(expectedJsonObject, jsonObject)
        jsonArray.removeElement(JSONNumber(2))
        assertEquals(expectedJsonArray, jsonArray)
    }

    @Test
    fun testReplaceElement() {
        val jsonObject = JSONObject(mutableListOf(
            JSONProperty("first name", JSONString("John")),
            JSONProperty("last name", JSONString("Doe")),
            JSONProperty("age", JSONNumber(35)),
            JSONProperty("courses", JSONArray(mutableListOf(
                JSONString("CALC"),
                JSONString("ALG")
            ))
            )
        ))

        val jsonArray = JSONArray(mutableListOf(
            JSONNumber(1),
            JSONNumber(2)
        ))

        jsonObject.replaceElement("age", JSONNumber(45))
        jsonArray.replaceElement(JSONNumber(2), JSONNumber(3))

        val expectedJsonObject = JSONObject(mutableListOf(
            JSONProperty("first name", JSONString("John")),
            JSONProperty("last name", JSONString("Doe")),
            JSONProperty("age", JSONNumber(45)),
            JSONProperty("courses", JSONArray(mutableListOf(
                JSONString("CALC"),
                JSONString("ALG")
            ))
            )
        ))

        val expectedJsonArray = JSONArray(mutableListOf(
            JSONNumber(1),
            JSONNumber(3)
        ))

        assertEquals(expectedJsonObject, jsonObject)
        assertEquals(expectedJsonArray, jsonArray)

        jsonObject.replaceElement("doesNotExist", JSONNumber(2))
        assertEquals(expectedJsonObject, jsonObject)
        jsonArray.replaceElement(JSONNumber(4), JSONNumber(5))
        assertEquals(expectedJsonArray, jsonArray)
    }

    @Test
    fun testGetElementsByKey() {
        val array = JSONArray(mutableListOf(
            JSONObject(mutableListOf(
                JSONProperty("number", JSONNumber(1)),
                JSONProperty("name", JSONString("John"))
            )),
            JSONObject(mutableListOf(
                JSONProperty("number", JSONNumber(2)),
                JSONProperty("name", JSONString("Mary")),
                JSONProperty("nestedObject", JSONObject(mutableListOf(
                    JSONProperty("number", JSONNumber(3)),
                    JSONProperty("name", JSONNumber(4))
                )))
            ))
        ))

        val expected = mutableListOf<JSONElement>(JSONNumber(1), JSONNumber(2), JSONNumber(3))

        assertEquals(expected, array.getElementsByKey("number"))
        assertNotEquals(expected, array.getElementsByKey("name"))
    }

    @Test
    fun testGetObjectByProperty() {
        val array = JSONArray(mutableListOf(
            JSONObject(mutableListOf(
                JSONProperty("number", JSONNumber(1)),
                JSONProperty("name", JSONString("John")),
                JSONProperty("teacher", JSONBoolean(true))
            )),
            JSONObject(mutableListOf(
                JSONProperty("number", JSONNumber(2))
            )),
            JSONObject(mutableListOf(
                JSONProperty("nested", JSONObject(mutableListOf(
                    JSONProperty("number", JSONNumber(3)),
                    JSONProperty("name", JSONString("Mary")),
                    JSONProperty("teacher", JSONBoolean(false))
                )))
            ))
        ))

        val expected = listOf(
            JSONObject(mutableListOf(
                JSONProperty("number", JSONNumber(1)),
                JSONProperty("name", JSONString("John")),
                JSONProperty("teacher", JSONBoolean(true))
            )),
            JSONObject(mutableListOf(
                JSONProperty("number", JSONNumber(3)),
                JSONProperty("name", JSONString("Mary")),
                JSONProperty("teacher", JSONBoolean(false))
            ))
        )

        assertEquals(expected, array.getObjectsByProperties(listOf("number", "name", "teacher")))
        assertNotEquals(expected, array.getObjectsByProperties(listOf("number")))
    }

    @Test
    fun testCheckPropertyTypes() {
        val jsonObject = JSONObject(mutableListOf(
            JSONProperty("students", JSONArray(mutableListOf(
                JSONObject(mutableListOf(
                    JSONProperty("number", JSONNumber(1)),
                    JSONProperty("name", JSONString("John")),
                    JSONProperty("teacher", JSONBoolean(true)),
                    JSONProperty("courses", JSONArray(mutableListOf(
                        JSONString("MEC"),
                        JSONString("ROB")
                    ))),
                    JSONProperty("professor", JSONObject(mutableListOf(
                        JSONProperty("name", JSONString("Peter"))
                    )))
                )),
                JSONObject(mutableListOf(
                    JSONProperty("number", JSONNumber(3)),
                    JSONProperty("name", JSONString("Mary")),
                    JSONProperty("date", JSONNull()),
                    JSONProperty("courses", JSONArray(mutableListOf(
                        JSONString("TAM"),
                        JSONString("MER"),
                        JSONString("BOA")
                    ))),
                    JSONProperty("professor", JSONObject(mutableListOf(
                        JSONProperty("name", JSONString("Rose"))
                    )))
                ))
            )))
        ))

        assertTrue(jsonObject.areNumbers("number"))
        assertTrue(jsonObject.areStrings("name"))
        assertTrue(jsonObject.areBooleans("teacher"))
        assertTrue(jsonObject.areNulls("date"))
        assertTrue(jsonObject.areArrays("courses"))
        assertTrue(jsonObject.areObjects("professor"))

        assertFalse(jsonObject.areNumbers("name"))
        assertFalse(jsonObject.areStrings("number"))
        assertFalse(jsonObject.areBooleans("date"))
        assertFalse(jsonObject.areNulls("teacher"))
        assertFalse(jsonObject.areArrays("professor"))
        assertFalse(jsonObject.areObjects("courses"))
    }

    @Test
    fun testCheckArrayInternalStructure() {
        val jsonObject = JSONObject(mutableListOf(
            JSONProperty("array1", JSONArray(mutableListOf(
                JSONObject(mutableListOf(
                    JSONProperty("name", JSONString("john")),
                    JSONProperty("age", JSONNumber(1)),
                )),
                JSONObject(mutableListOf(
                    JSONProperty("name", JSONString("doe")),
                    JSONProperty("age", JSONNumber(2)),
                ))
            ))),
            JSONProperty("array2", JSONArray(mutableListOf(
                JSONObject(mutableListOf(
                    JSONProperty("name", JSONString("john")),
                    JSONProperty("age", JSONNumber(1)),
                    JSONProperty("valid", JSONBoolean(false))
                )),
                JSONObject(mutableListOf(
                    JSONProperty("name", JSONString("doe")),
                    JSONProperty("age", JSONNumber(2)),
                ))
            )))
        ))

        assertTrue(jsonObject.arrayIsStructured("array1"))
        assertFalse(jsonObject.arrayIsStructured("array2"))
    }

    data class Student(
        @JSONGenerator.AsJSONString
        @JSONGenerator.UseName("NUMBER")
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
        Bachelor
    }

    @Test
    fun testJsonGenerator() {
        val student = Student(1, "john", StudentType.Bachelor,
            null, 1.73, listOf("MEC","ROB","TAM","MER","BOA"),
            mapOf("MEC" to 1, "ROB" to 2,"TAM" to 3,"MER" to 4,"BOA" to 5),
            true, Professor(40, "mary"), 'a',
            "WillBeExcluded"
        )

        val json = JSONGenerator.generateJSON(student)

        val expected = JSONObject(mutableListOf(
            JSONProperty("adse", JSONString("a")),
            JSONProperty("birth", JSONNull()),
            JSONProperty("courses", JSONArray(mutableListOf(
                JSONString("MEC"),
                JSONString("ROB"),
                JSONString("TAM"),
                JSONString("MER"),
                JSONString("BOA")
            ))),
            JSONProperty("height", JSONNumber(1.73)),
            JSONProperty("name", JSONString("john")),
            JSONProperty("NUMBER", JSONString("1")),
            JSONProperty("professor", JSONObject(mutableListOf(
                JSONProperty("age", JSONNumber(40)),
                JSONProperty("name", JSONString("mary"))
            ))),
            JSONProperty("scores", JSONObject(mutableListOf(
                JSONProperty("MEC", JSONNumber(1)),
                JSONProperty("ROB", JSONNumber(2)),
                JSONProperty("TAM", JSONNumber(3)),
                JSONProperty("MER", JSONNumber(4)),
                JSONProperty("BOA", JSONNumber(5))
            ))),
            JSONProperty("type", JSONString("Bachelor")),
            JSONProperty("valid", JSONBoolean(true))
        ))

        assertEquals(expected, json)
    }
}