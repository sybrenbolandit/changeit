package nl.sybr.dev.command.history

import nl.sybr.dev.command.CommandConfig
import nl.sybr.dev.command.CommandDescription
import org.slf4j.LoggerFactory
import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "show",
    description = [ "List last changes." ]
)
class HistoryShow : Callable<Int> {

    @CommandLine.Option(
        names = ["-n", "--number"],
        description = ["Number of changes to show."]
    )
    protected var number: Int = 10

    override fun call(): Int {
        logger.info("History:")
        allCommands()
            .take(number)
            .forEach { command ->
                logger.info("${if (command.reverted) "[reverted] " else ""}${command.print()}")
            }

        logger.info(if (allCommands().count() > number) "..." else "<end>")

        return 0
    }

    companion object {
        val logger = LoggerFactory.getLogger(HistoryShow::class.java)
    }
}


fun CommandDescription.print(): String {
    return "${this.commandConfig.name} ${this.commandConfig.argsToString()}"
}

fun CommandConfig.argsToString(): String {
    return this.args.joinToString(" ") { arg -> "--${arg.key}=${quote(arg.value)}" }
}

fun quote(value: String): String {
    return if (value.contains(Regex("\\s"))) "'$value'" else value
}