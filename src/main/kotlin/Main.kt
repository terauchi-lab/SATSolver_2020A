import java.io.File

fun main(args: Array<String>) {
    if (args.isNotEmpty()) Solve(File(args[0])).run()
}