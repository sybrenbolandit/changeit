package nl.sybr.dev.command.files

import nl.sybr.dev.command.CommandArg
import nl.sybr.dev.command.CommandConfig
import nl.sybr.dev.command.CommandDescription
import nl.sybr.dev.command.CommandResult
import org.apache.commons.io.FileUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
import java.io.File
import java.nio.file.Path
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "copy",
    description = ["Copy files."],
)
class Copy : FilesCommand(), Callable<Int> {

    @CommandLine.Option(
        names = ["--operation"],
        description = ["The moving operations to use."],
        required = true
    )
    protected lateinit var operation: String

    override fun logCommand(): CommandConfig {
        return CommandConfig(
            "copy",
            logFileFilter(
                mutableListOf(
                    CommandArg("operation", operation)
                )
            )
        )
    }

    override fun callCommand(): CommandResult {
        logger.info("Copying with operation: $operation")

        val matchList = eligibleFiles(commandContext.toPath())
        val copies = matchList.stream()
            .map { path -> FileCopy(path.toFile(), File(operate(path))) }
            .peek { fileCopy -> logger.info("Copying file: ${fileCopy.source} to: ${fileCopy.target}") }
            .toList()
        matchList.stream()
            .forEach { path -> FileUtils.copyFile(path.toFile(), File(operate(path))) }

        return CopyResult(0, copies)
    }

    override fun revert(commandDescription: CommandDescription) {
        val copyResult = commandDescription.result as CopyResult
        copyResult.copies.stream()
            .forEach { fileCopy ->
                FileUtils.delete(fileCopy.target)
                logger.info("Deleted file: ${fileCopy.target}")
            }
    }

    private fun operate(path: Path): String {
        return String.format(operation, path.toString())
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(Copy::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            val exitCode = CommandLine(Copy()).execute(*args)
            System.exit(exitCode)
        }
    }
}

data class CopyResult(override val exitCode: Int, val copies: List<FileCopy>): CommandResult(exitCode)

data class FileCopy(val source: File, val target: File)
