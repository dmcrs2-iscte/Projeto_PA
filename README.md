# *Purpose:*

This library was created with the purpose of representing JSON files in Kotlin. It includes representations for every JSON data type, including Number, Boolean, String, Null, Object and Array.

The user may use the library with a text-based approach, i.e. creating representations of JSON objects by defining them manually using the appropriate data classes, or with an editor-based approach, using a GUI and defining JSON objects in a more interactive manner, making use of the widgets and the visual representation of the JSON file being created in real time.


# *Architecture:*

Every JSON data type implements either the JSONNode interface or the JSONLeaf interface. JSONNode represents composite JSON types - JSON Object and JSON Array. On the other hand, JSONLeaf represents the data types which don't contain other nested elements (String, Number, etc.).

JSONNode and JSONLeaf both implement the JSONElement interface, which represents a generic JSON data type. This interface defines a defines two things to be implemented: a val value (the content of the JSON element) and a function 'accept' which takes a visitor as the parameter. Many of the available operations for retrieving information about the content of the JSON elements were defined with the visitor design pattern in mind (they are all explained below).

The key-value pairs of the JSON objects are represented by the data class JSONProperty, which takes in a String "key" and an element "JSONElement" as parameters.


# *Defining all the JSONElements:*

## *JSONNumbers:*

JSON equivalents to Numbers. They are defined by:
```kotlin
val jsonNumber_1 = JSONNumber(123)
val jsonNumber_2 = JSONNumber(2.0)
val jsonNumber_3 = JSONNumber(3.0f)
//...
```

## *JSONStrings:*

JSON equivalents to Strings. They are defined by:
```kotlin
val jsonString = JSONString("abc")
```

## *JSONBooleans:*

JSON equivalents to Booleans. They are defined by:
```kotlin
val jsonBoolean = JSONBoolean(true)
```

## *JSONNulls:*

JSON equivalents to nulls. They are defined by:
```kotlin
val jsonNull = JSONNull()
```

## *JSONArrays:*

In order to create a JSON Array, the user may use the JSONArray data class and pass a mutable list of JSONElements as a parameter.
As an example, say we want to define a JSONArray representing a list of school courses:
```kotlin
val jsonArray = JSONArray(mutableListOf(
	JSONString("algebra"),
	JSONString("calculus")
))
```

## *JSONObjects:*

In order to create a JSON Object, the user may use the JSONObject data class and pass a mutable list of JSONProperties as a parameter.

### *Defining a JSONProperty:*

JSONProperty does not implement JSONElement, so it is not considered an element, but rather as a helper class that has the structure needed for the JSONObject to use internally.
```kotlin
val jsonProperty = JSONProperty("first name", JSONString("John"))
```

### *Defining a JSONObject:*

A JSONObject is represented by a list of JSONProperties.
As an example, say we want to define a JSONObject representing John Doe, a 35 year old teacher who teaches algebra and calculus:
```kotlin
val jsonObject = JSONObject(mutableListOf(
	JSONProperty("first name", JSONString("John")),
        JSONProperty("last name", JSONString("Doe")),
        JSONProperty("age", JSONNumber(35)),
        JSONProperty("teacher", JSONBoolean(true)),
        JSONProperty("courses", JSONArray(mutableListOf(
            	JSONString("algebra"),
            	JSONString("calculus")
        )))
))
```

# *Operations:*

## *Setter operations:*

The user may add, remove and replace values after creating a JSONNode, using the functions addElement, removeElement and replaceElement. These functions take different parameters depending on them being used on an object or an array:

| Command | JSONObject Argument | JSONArray Argument | Description |
| ------- | ------------------- | ------------------ | ----------- |
| addElement | (property: JSONProperty) | (element: JSONElement) | For JSONObjects, adds property. For JSONArrays, adds element |
| removeElement | (key: String) | (element: JSONElement) | For JSONObjects, removes the JSONProperty associated with key. For JSONArrays, removes element |
| replaceElement | (key: String, element: JSONElement) | (oldElement: JSONElement, newElement: JSONElement) | For JSONObjects, replaces the JSONElement associated with key with element. For JSONArrays, replaces oldElement with newElement | 


## *Getter operations:*

### *There are many functions defined for getting information about the nodes:*

| Command | Description |
| ------- | ----------- |
| getElementsByKey(key: String) | Returns a list of the JSONElements corresponding to key |
| getObjectsByProperty(properties: List<String>) | Returns a list of the JSONObjects with every property corresponding to the keys in properties |
| areNumbers(key: String) | Returns true if every property with the given key is a Number, false otherwise |
| areNulls(key: String) | Returns true if every property with the given key is null, false otherwise |
| areStrings(key: String) | Returns true if every property with the given key is a String, false otherwise |
| areBooleans(key: String) | Returns true if every property with the given key is a Boolean, false otherwise |
| areObjects(key: String) | Returns true if every property with the given key is a List of JSONProperties, false otherwise |
| areArrays(key: String) | Returns true if every property with the given key is a List of JSONElements, false otherwise |
| isStructuredArray(key: String) | Returns true if the all the elements of the array with key given as parameter have the same structure |
| toTree() | Returns the tree-like structure of the object |


# *JSONGenerator:*

This class is useful for generating a JSON object directly from any data class the user may create. For example, say we create the following data classes and enum:

```kotlin
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
```

In order to generate a JSON object for representing an instance of the Student class, we might do the following:

```kotlin
val student = Student(number=100000, name="John", type=StudentType.Bachelor, height=178.1,
	courses=listOf("ALG", "CALC1", "PROG"), 
	scores=mapOf("ALG" to 19, "CALC1" to 16, "PROG" to 20),
	valid=true, professor=Professor(40, "Mary"))

//creates the JSON object corresponding to the instance of Student defined above.
val jsonFromStudent = JSONGenerator.generateJSON(student) 
```
# *Annotations:*

The user may also use the following annotations:

| Annotation | Description |
| ---------- | ----------- |
| JSONGenerator.AsJSONString | The corresponding attribute will be defined as a JSONString |
| JSONGenerator.UseName(String) | The corresponding attribute's key in the JSONObject will be the one passed as argument instead of the name defined in the instance value |
| JSONGenerator.ExcludeFromJSON | The corresponding attribute will not be represented in the JSONObject |

# *Observers:*

Observers that are implemented:

| Observer | Motivation |
| -------- | ----------- |
|JSONObserver | Implemented by JSONNodes facilitating communication between the JSON model and, for example, front end applications by the user. When a node is added to another node becoming nested, the parent node becomes an Observer of the nested one, so as to relay information to the root node. If an app is Observing the root node, then all changes inside it, including inside the nested nodes, are relayed to this app |

This observer is notified when:

| Operation | Description |
| --------- | ----------- |
| elementAdded() | Receives information that an element was added to the node it's observing |
| elementRemoved() | Receives information that an element was removed from the node it's observing |
| elementReplaced() | Receives information that an element was replaced in the node it's observing |
