import json.*
import mvc.UI

fun main() {
    val json = JSONObject(mutableListOf(
        JSONProperty("newName", JSONString("1")), JSONProperty("name", JSONString("eu")),
        JSONProperty("birth", JSONNull()), JSONProperty("height", JSONNumber(1.73)), JSONProperty("courses", JSONArray(
            mutableListOf(JSONString("MEC"), JSONString("ROB"), JSONString("TAM"), JSONString("MER"), JSONString("BOA")))
        ),
        JSONProperty("scores", JSONObject(mutableListOf(
            JSONProperty("MEC", JSONNumber(1)), JSONProperty("ROB", JSONNumber(2)),
            JSONProperty("TAM", JSONNumber(3)), JSONProperty("MER", JSONNumber(4)), JSONProperty("BOA", JSONNumber(5))
        ))
        ),
        JSONProperty("valid", JSONBoolean(true)), JSONProperty("professor", JSONObject(mutableListOf(
            JSONProperty("age", JSONNumber(40)),
            JSONProperty("name", JSONString("Pedro"))
        ))
        ), JSONProperty("adse", JSONString("a")), JSONProperty("type", JSONString("Bachelor"))
    ))
    val ui = UI(json)

    ui.open()
}