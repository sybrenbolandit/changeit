package nl.sybr.dev.command.files

import nl.sybr.dev.command.CommandArg
import nl.sybr.dev.command.CommandConfig
import nl.sybr.dev.command.CommandDescription
import nl.sybr.dev.command.CommandResult
import nl.sybr.dev.command.HistoryCommand
import org.apache.commons.io.FileUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
import java.io.File
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "add",
    description = ["Add a file."],
)
class Add : HistoryCommand(), Callable<Int> {

    @CommandLine.Option(
        names = ["--source"],
        description = ["Specify a file to add."],
        required = true
    )
    protected lateinit var sourceFile: File

    @CommandLine.Option(
        names = ["--target"],
        description = ["The target location."],
        required = true
    )
    protected lateinit var target: String

    override fun logCommand(): CommandConfig {
        return CommandConfig(
            "add",
            logContext(
                mutableListOf(
                    CommandArg("source", sourceFile.toPath().toString()),
                    CommandArg("target", target),
                )
            )
        )
    }

    override fun callCommand(): CommandResult {
        logger.info("Adding file: $sourceFile")

        val targetFile = File(commandContext, target)
        FileUtils.copyFile(sourceFile, targetFile)
        logger.info("New file location: $targetFile")

        return AddResult(0, targetFile.absolutePath)
    }

    override fun revert(commandDescription: CommandDescription) {
        val addResult = commandDescription.result as AddResult
        FileUtils.delete(File(addResult.targetLocation))
        logger.info("Deleted file: ${addResult.targetLocation}")
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(Add::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            val exitCode = CommandLine(Add()).execute(*args)
            System.exit(exitCode)
        }
    }
}

data class AddResult(override val exitCode: Int, val targetLocation: String) : CommandResult(exitCode)
