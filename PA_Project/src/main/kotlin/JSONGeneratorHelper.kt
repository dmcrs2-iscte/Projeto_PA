import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible



data class Student(
    val number: Int,
    val name: String,
    //val type: StudentType? = null,
    val birth: String? = null,
    val height: Double,
    val courses: Collection<String>,
    val scores: Map<String, Int>,
    val valid: Boolean
)

enum class StudentType {
    Bachelor, Master, Doctoral
}

fun main() {
    /*val student = Student(1,"eu", StudentType.Doctoral)
    val properties = Student::class.memberProperties
    properties.forEach { prop ->
        val value = prop.call(student)
        println("${prop.name}: ${prop.returnType} = $value")
    }*/
}