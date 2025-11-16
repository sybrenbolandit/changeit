package nl.sybr.dev.command.history

import org.slf4j.LoggerFactory
import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "delete",
    description = ["Delete last changes."]
)
class HistoryDelete : Callable<Int> {

    @CommandLine.Option(
        names = ["-n", "--number"],
        description = ["Number of changes to delete."]
    )
    protected var number: Int? = null

    override fun call(): Int {
        if (number == null) {
            val sure = System.console().readLine("Are you sure you want to delete the whole history? (y/n): ")
            val deleteAll = sure.toBoolean() || "y".equals(sure, ignoreCase = true)
            if (deleteAll) number = allCommands().count() else return 0
        }

        logger.info("Updated history:")
        allCommands()
            .take(number!!)
            .forEach { command ->
                deleteHistory(command)
                logger.info("[deleted] ${if (command.reverted) "[reverted] " else ""}${command.print()}")
            }

        val nextCommand = allCommands().getOrNull(number!!)
        logger.info(nextCommand?.print() ?: "<end>")

        return 0
    }

    companion object {
        val logger = LoggerFactory.getLogger(HistoryDelete::class.java)
    }
}
