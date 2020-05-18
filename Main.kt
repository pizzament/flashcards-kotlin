package flashcards

import java.io.File
import java.util.*

val scanner = Scanner(System.`in`)
val logs = mutableListOf<String>()
val mistakes = mutableMapOf<String, Int>()

fun main(args: Array<String>) {
    val cards = mutableMapOf<String, String>()
    var exportFileName = ""

    if (args.isNotEmpty()) {
        if(args[0] == "-import") {
            importFile(cards, args[1])

            if(args.getOrNull(2) == "-export") {
                exportFileName = args[3]
            }
        } else if (args[0] == "-export") {
            exportFileName = args[1]

            if(args.getOrNull(2) == "-import") {
                importFile(cards, args[3])
            }
        }
    }

    while(true) {
        printlnAndLog("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):")

        when (nextLineAndLog()) {
            "add" -> add(cards)
            "remove" -> remove(cards)
            "import" -> import(cards)
            "export" -> export(cards)
            "ask" -> ask(scanner, cards)
            "log" -> log()
            "hardest card" -> hardestCard()
            "reset stats" -> resetStats()
            "exit" -> {
                printlnAndLog("Bye bye!")

                if(exportFileName.isNotBlank()) {
                    exportFile(cards, exportFileName)
                }

                return
            }
            else -> printlnAndLog("Oops! Try again...")
        }
        printlnAndLog()
    }
}

fun add(cards: MutableMap<String, String>) {
    printlnAndLog("The card:")
    val term = nextLineAndLog()

    if (cards.keys.contains(term)) {
        printlnAndLog("The card \"$term\" already exists.")
    } else {
        printlnAndLog("The definition of the card:")
        val definition = nextLineAndLog()

        if (cards.values.contains(definition)) {
            printlnAndLog("The definition \"$definition\" already exists.")
        } else {
            cards[term] = definition
            mistakes[term] = 0
            printlnAndLog("The pair (\"$term\":\"$definition\") has been added.")
        }
    }
}

fun remove(cards: MutableMap<String, String>) {
    printlnAndLog("The card:")
    val term = nextLineAndLog()

    if (cards.keys.contains(term)) {
        cards.remove(term)
        mistakes.remove(term)
        printlnAndLog("The card has been removed.")
    } else {
        printlnAndLog("Can't remove \"$term\": there is no such card.")
    }
}

fun import(cards: MutableMap<String, String>) {
    printlnAndLog("File name:")
    val fileName = nextLineAndLog()
    importFile(cards, fileName)
}

fun importFile(cards: MutableMap<String, String>, fileName: String) {
    try {
        var index = 0
        var term = ""
        var count = 0

        File(fileName).forEachLine {
            when (index) {
                0 -> {
                    term = it
                }
                1 -> {
                    cards[term] = it
                    count++
                }
                else -> {
                    mistakes[term] = it.toInt()
                }
            }

            index = (index + 1) % 3
        }

        printlnAndLog("$count cards have been loaded.")
    } catch(e: Exception) {
        printlnAndLog("File not found.")
    }
}

fun export(cards: MutableMap<String, String>) {
    printlnAndLog("File name:")
    val fileName = nextLineAndLog()
    exportFile(cards, fileName)
}

fun exportFile(cards: MutableMap<String, String>, fileName: String) {
    var text = ""

    cards.forEach { (t, u) ->
        text += "$t\n$u\n${mistakes[t]}\n"
    }

    File(fileName).writeText(text)
    printlnAndLog("${cards.keys.size} cards have been saved.")
}

fun ask(scanner: Scanner, cards: MutableMap<String, String>) {
    printlnAndLog("How many times to ask?")
    val times = scanner.nextInt()
    logs.add(times.toString())
    nextLineAndLog()

    repeat(times) {
        val key = cards.keys.random()
        printlnAndLog("Print the definition of \"$key\":")
        val answer = nextLineAndLog()
        val definition = cards[key]

        if(definition == answer) {
            printlnAndLog("Correct answer")
        } else {
            mistakes[key] = mistakes[key]!! + 1
            if(cards.values.contains(answer)) {
                printlnAndLog("Wrong answer. The correct one is \"$definition\", you've just written the definition of \"${cards.filterValues { it == answer }.keys.first()}\".")
            } else {
                printlnAndLog("Wrong answer. The correct one is \"$definition\".")
            }
        }
    }
}

fun printlnAndLog(s: String = "") {
    println(s)
    logs.add(s + "\n")
}

fun nextLineAndLog(): String {
    val line = scanner.nextLine()
    logs.add(line)

    return line
}

fun log() {
    printlnAndLog("File name:")
    val fileName = nextLineAndLog()
    File(fileName).writeText(logs.toString())
    printlnAndLog("The log has been saved.")
}

fun hardestCard() {
    val max = mistakes.maxBy { e -> e.value }

    if(max == null || max.value == 0) {
        printlnAndLog("There are no cards with errors.")
    } else {
        val hardestEntries = mistakes.filter { e -> e.value == max.value }.entries

        if(hardestEntries.size == 1) {
            printlnAndLog("The hardest card is \"${max.key}\". You have ${max.value} errors answering it.")
        } else {
            var hard = ""

            for(entry in hardestEntries) {
                hard += "\"${entry.key}\", "
            }

            hard = hard.substring(0, hard.length - 2)
            printlnAndLog("The hardest cards are $hard. You have ${max.value} errors answering it.")
        }
    }
}

fun resetStats() {
    for(key in mistakes.keys) {
        mistakes[key] = 0
    }

    printlnAndLog("Card statistics has been reset.")
}