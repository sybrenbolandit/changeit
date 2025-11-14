package nl.sybr.dev.command.files

import nl.sybr.dev.command.CommandResult
import org.apache.commons.io.FileUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
import java.io.File
import java.nio.file.Path

@CommandLine.Command(
    name = "move",
    description = ["Move files."],
)
class Move : FilesCommand() {

    @CommandLine.Option(
        names = ["--operation"],
        description = ["The moving operations to use."],
        required = true
    )
    protected lateinit var operation: String

    override fun callWithEnv(): CommandResult {
        logger.info("Moving with operation: $operation")

        val matchList = eligibleFiles(commandContext.toPath())
        val moves = matchList.stream()
            .map { path -> FileMove(path.toFile(), File(operate(path))) }
            .peek { fileMove -> logger.info("Moving file: ${fileMove.source} to: ${fileMove.target}") }
            .toList()
        matchList.stream()
            .forEach { path ->
                FileUtils.copyFile(path.toFile(), File(operate(path)))
                FileUtils.delete(path.toFile())
            }

        return MoveResult(0, moves)
    }

    private fun operate(path: Path): String {
        return String.format(operation, path.toString())
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(Move::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            val exitCode = CommandLine(Move()).execute(*args)
            System.exit(exitCode)
        }
    }
}

data class MoveResult(override val exitCode: Int, val moves: List<FileMove>) : CommandResult(exitCode)

data class FileMove(val source: File, val target: File)
