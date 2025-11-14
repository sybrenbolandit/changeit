package nl.sybr.dev.command.files

import nl.sybr.dev.command.CommandResult
import org.apache.commons.io.FileUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "update",
    description = ["String update lines of files."],
)
class Update : FilesCommand(), Callable<Int> {

    @CommandLine.Option(
        names = ["--matcher"],
        description = ["The regex to match the string."],
        required = true
    )
    protected lateinit var matcher: String

    @CommandLine.Option(
        names = ["--replacement"],
        description = ["The string to replace the matches."],
        required = true
    )
    protected lateinit var replacement: String


    override fun call(): Int {
        val commandResult = callWithEnv()
        return commandResult.exitCode
    }

    override fun callWithEnv(): CommandResult {
        logger.info("Updating with matcher: $matcher, and replacement: $replacement")

        val matchList = eligibleFiles(commandContext.toPath())
        val updatedFiles = matchList.stream()
            .peek { path -> logger.info("Updating file: ${path.fileName}") }
            .map(Path::toFile)
            .map { file -> UpdatedFile(
                file,
                if (file.readText().contains(replacement)) file.readText() else null
            ) }
            .toList()
        matchList.stream()
            .forEach { path -> replace(path.toFile(), matcher, replacement) }

        return UpdateResult(0, matcher, replacement, updatedFiles)
    }

    private fun replace(file: File, matcher: String, replacement: String) {
       val lines = FileUtils.readLines(file, Charsets.UTF_8)
        val lineEnding = detectLineEnding(file.toPath())
        val newLines = lines.stream().map { line -> line.replace(matcher, replacement) }.toList()
        FileUtils.writeLines(file, newLines, lineEnding)
    }

    private fun detectLineEnding(path: Path): String {
        Files.newBufferedReader(path).use { reader ->
            var ch: Int
            while(reader.read().also { ch = it } != 1) {
                if (ch == '\r'.code) {
                    reader.mark(1)
                    val next = reader.read()
                    return if (next == '\n'.code) "\r\n" else "\n"
                } else {
                    return "\n"
                }
            }
        }
        return System.lineSeparator()
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(Update::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            val exitCode = CommandLine(Update()).execute(*args)
            System.exit(exitCode)
        }
    }
}

data class UpdateResult(
    override val exitCode: Int,
    val matcher: String,
    val replacement: String,
    val updatedFiles: List<UpdatedFile>) : CommandResult(exitCode)

data class UpdatedFile(val file: File, val originalContent: String?)
