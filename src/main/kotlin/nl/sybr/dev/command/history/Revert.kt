package nl.sybr.dev.command.history

import nl.sybr.dev.command.findCommandClass
import org.slf4j.LoggerFactory
import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "revert",
    description = [ "Revert the last change." ]
)
class Revert : Callable<Int> {

    override fun call(): Int {
        logger.info("Reverting last command.")
        val currentCommand = currentCommand() ?: throw IllegalStateException("No command to revert!")
        val lastCommand = findCommandClass(currentCommand.commandConfig.name)
        lastCommand.revert(currentCommand)
        writeRevertCommand(currentCommand)

        return 0
    }

    companion object {
        val logger = LoggerFactory.getLogger(Revert::class.java)
    }
}
