package nl.sybr.dev.command.history

import nl.sybr.dev.command.callCommand
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import picocli.CommandLine
import java.io.File
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "redo",
    description = ["Redo last reverted change."]
)
class Redo : Callable<Int> {

    override fun call(): Int {
        logger.info("Redo last reverted command.")
        val revertedCommand = lastRevertedCommand() ?: throw IllegalStateException("No command to revert!")
        val contextArg =
            revertedCommand.commandConfig.args.filter { it.key == "context" }.map { it.value }.firstOrNull()
        val context = if (StringUtils.isNotBlank(contextArg)) "." else contextArg
        callCommand(revertedCommand, File(context!!))
        writeUnrevertCommand(revertedCommand)

        return 0
    }

    companion object {
        val logger = LoggerFactory.getLogger(Redo::class.java)
    }
}
