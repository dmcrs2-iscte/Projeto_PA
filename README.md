Purpose:

This library was created with the purpose of representing JSON files in Kotlin. It includes representations for every JSON data type, including Number, Boolean, String, Empty, Object and Array.

The user may use the library with a text-based approach, i.e. creating representations of JSON objects by defining them manually using the appropriate data classes, or with an editor-based approach, using a GUI and defining JSON objects in a more interactive manner, making use of the widgets and the visual representation of the JSON file being created in real time.


Architecture:

Every JSON data type implements either the JSONNode interface or the JSONLeaf interface. JSONNode represents composite JSON types - JSON Object and JSON Array. On the other hand, JSONLeaf represents the data types which don't contain other nested elements (String, Number, etc.).

JSONNode and JSONLeaf both implement the JSONElement interface, which represents a generic JSON data type. This interface defines a defines two things to be implemented: a val value (the content of the JSON element) and a function 'accept' which takes a visitor as the parameter. Many of the operations regarding the content of the JSON elements were defined with the visitor design pattern in mind.

The key-value pairs of the JSON objects are represented by the data class JSONProperty.

The JSONObserver interface was defined with the purpose of implementing the observer design pattern in the editor GUI. A JSONNode may have observers which are notified when changes occur in the structure of a JSON object or array, triggering an update in the visual representation of the file in the GUI.


Defining JSONObjects:

In order to create a JSON Object, the user may use the JSONObject data class and pass a mutable list of JSONProperties as parameter. These JSONProperties take a key (String) and an element (JSONElement) as parameters.

As an example, say we want to define a JSON object representing John Doe, a 35 year old teacher who teaches algebra and calculus. Here's how it could look:

val jsonObject = JSONObject(
	JSONProperty("first name", JSONString("John")),
        JSONProperty("last name", JSONString("Doe")),
        JSONProperty("age", JSONNumber(35)),
        JSONProperty("teacher", JSONBoolean(true)),
        JSONProperty("courses", JSONArray(mutableListOf(
            	JSONString("algebra"),
            	JSONString("calculus")
        )))
))

The user may also add, remove and replace values after creating the object, using the functions addElement, removeElement and replaceElement. These functions take different parameters depending on them being used on an object or an array.

When used on an object:

addElement(JSONProperty)
removeElement(JSONProperty)
replaceElement(String, JSONElement)

When used on an array:

addElement(JSONElement)
removeElement(JSONElement)
replaceElement(JSONElement, JSONElement)


Other operations:

There are many functions defined for getting information about the objects/arrays:

 - getValuesByName(name): returns a list of the JSON elements corresponding to a specified key
 - getObjectsByProperty(properties): returns a list of all objects with every property given as parameter
 - areNumbers(name), areFloats(name), areNulls(name), areStrings(name), areBooleans(name): returns true if every property with the given key is of the mentioned type, false otherwise
 - isListOfProperties(name):
 - isListOfElements(name):
 - isStructuredArray(name): returns true if the all the elements of the array with key given as parameter have the same structure
 - toTree(): returns the tree-like structure of the object


JSONGenerator:

This class is useful for generating a JSON object directly from any data class the user may create. For example, say we create the following data classes and enum:

data class Student(
        val number: Int,
        val name: String,
        val type: StudentType? = null,
        val height: Double,
        val courses: Collection<String>,
        val scores: Map<String, Int>,
        val valid: Boolean,
        val professor: Professor,
)

data class Professor(
    val age: Int,
    val name: String
)

enum class StudentType {
    Bachelor, Master, Doctoral
}

In order to generate a JSON object for representing an instance of the Student class, we might do the following:

val student = Student(number=100000, name="John", type=StudentType.Bachelor, height=178.1,
	courses=listOf("ALG", "CALC1", "PROG"), 
	scores=mapOf("ALG" to 19, "CALC1" to 16, "PROG" to 20),
	valid=true, professor=Professor(40, "Mary"))

//creates the JSON object corresponding to the instance of Student defined above.
val jsonFromStudent = JSONGenerator.generateJSON(student) 

The user may also use the following annotations:

 - JSONGenerator.AsJSONString: the corresponding attribute will be defined as a JSONString
 - JSONGenerator.UseName(String): the corresponding attribute's key in the JSON object will be the one passed as argument instead of the name defined in the instance value
 - JSONGenerator.ExcludeFromJSON: the corresponding attribute will not be represented in the JSON object