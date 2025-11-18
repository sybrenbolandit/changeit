package nl.sybr.dev.command

import nl.sybr.dev.command.history.createNewHistory
import nl.sybr.dev.command.history.writeHistory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
import java.util.*
import java.util.concurrent.Callable

abstract class HistoryCommand : CommandContext(), Callable<Int> {

    @CommandLine.Option(
        names = ["--no-history"],
        description = ["Flag that no history should be taken."],
    )
    protected var noHistory: Boolean = false

    override fun call(): Int {
        if (noHistory) {
            val result = callCommand()
            return  result.exitCode
        } else {
            val command = logPreCommand()
            val result = callCommand()
            logPostCommand(command, result)
            return  result.exitCode
        }
    }

    abstract fun logCommand(): CommandConfig

    abstract fun callCommand(): CommandResult

    abstract fun revert(commandDescription: CommandDescription)

    private fun logPreCommand(): CommandDescription {
        val date = Date()
        val logFile = createNewHistory(date)
        val command = CommandDescription(logCommand(), date)
        mapper.writeValue(logFile, command)
        return command
    }

    private fun logPostCommand(command: CommandDescription, result: CommandResult) {
        command.result = result
        writeHistory(command)
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(HistoryCommand::class.java)
    }
}

data class CommandConfig(val name: String, val args: List<CommandArg>)

data class CommandArg(val key: String, val value: String)

data class CommandDescription(
    val commandConfig: CommandConfig,
    val dateTime: Date,
    var reverted: Boolean = false,
    var result: CommandResult? = null
)
