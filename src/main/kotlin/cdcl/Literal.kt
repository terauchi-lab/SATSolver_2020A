package cdcl

data class Literal(
    val number: Int,
    var bool: Boolean?,
    val factor: MutableList<Int>
)