package cdcl

data class Literal(
    val number: Int,
    var bool: Boolean?,
    var level: Int?,
    val factor: MutableList<List<Int>>
)