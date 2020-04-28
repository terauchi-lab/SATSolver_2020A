package cdcl

import java.io.File

class Solve(private val file: File) {
    fun run() {
        val texts = file.readLines()

        val size = texts[0].split(' ')[2].toInt()
        val clause = mutableListOf<Clause>()
        texts.drop(1).forEach {
            clause.add(Clause(it))
        }
        val literal = mutableListOf<Triple<Int, Boolean, Boolean>>()
        repeat(size) {
            literal.add(Triple(it + 1, second = true, third = false))
        }
    }
}