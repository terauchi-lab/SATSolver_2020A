import java.io.File

fun main(args: Array<String>) {
    //if (args.isNotEmpty()) dpll.Solve(File(args[0])).run()
    if (args.isNotEmpty()) cdcl.Solve(File(args[0])).run()
    else println("no file")
}