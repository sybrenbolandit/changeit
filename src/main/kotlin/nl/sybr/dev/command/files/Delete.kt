package nl.sybr.dev.command.files

import nl.sybr.dev.command.CommandResult
import org.apache.commons.io.FileUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
import kotlin.io.path.absolutePathString

@CommandLine.Command(
    name = "delete",
    description = ["Delete files."],
)
class Delete : FilesCommand() {

    override fun callWithEnv(): CommandResult {

        val matchList = eligibleFiles(commandContext.toPath())
        val deletions = matchList.stream()
            .peek { path -> logger.info("Deleting file: ${path.fileName}") }
            .map { path -> FileDeletion(path.absolutePathString(), path.toFile().readText()) }
            .toList()
        matchList.stream().forEach { path -> FileUtils.delete(path.toFile()) }

        return DeleteResult(0, deletions)
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(Delete::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            val exitCode = CommandLine(Delete()).execute(*args)
            System.exit(exitCode)
        }
    }
}

data class DeleteResult(override val exitCode: Int, val deletions: List<FileDeletion>) : CommandResult(exitCode)

data class FileDeletion(val location: String, val content: String)
