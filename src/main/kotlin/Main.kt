import java.io.File

fun main(args: Array<String>) {
    if (args.isNotEmpty()) {
        if (args.size >= 2 && args[1].first() == 'c') cdcl.Solve(File(args[0])).run()
        else dpll.Solve(File(args[0])).run()
    } else println("no file")
}