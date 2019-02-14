import midi.parseMidi
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

fun main(args: Array<String>) {
    if (args.isEmpty()) printHelpAndExit()

    var numFlags = 0
    var verbose = false
    for (arg in args) {
        if (arg.length > 1 && arg[0] == '-') {
            numFlags++
            if (arg == "-v") verbose = true
            else if (arg == "--") break;
            else System.err.println("Ignore unknown flag $arg")
        }
        else break
    }
    if (args.size <= numFlags) printHelpAndExit()

    val input = if (args[numFlags] == "-") {
        System.`in`
    } else {
        FileInputStream(File(args[numFlags]))
    }

    val output = if (args.size == numFlags + 1) {
        System.out
    } else {
        FileOutputStream(File(args[numFlags + 1]))
    }

    try {
        parseMidi(input, output, verbose)
    } catch (e: Exception) {
        System.err.println(e.message)
        System.exit(1)
    }
}

private fun printHelpAndExit() {
    System.err.println("""
        SMF to LRC converter - Gilles Waeber 2018
        Extracts the lyrics from a midi file

        Usage: java -jar smftolrc [-v] [<InFile | -> OutFile]
          -v: verbose
    """.trimIndent())
    System.exit(-1)
}