package nl.sybr.dev.command.files

import nl.sybr.dev.command.CommandContext
import nl.sybr.dev.command.CommandResult
import org.apache.commons.io.FileUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
import java.io.File

@CommandLine.Command(
    name = "add",
    description = ["Add a file."],
)
class Add : CommandContext() {

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

    override fun callWithEnv(): CommandResult {
        logger.info("Adding file: $sourceFile")

        val targetFile = File(commandContext, target)
        FileUtils.copyFile(sourceFile, targetFile)
        logger.info("New file location: $targetFile")

        return AddResult(0, targetFile.absolutePath)
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

data class AddResult(override val exitCode: Int, val targetLocation: String): CommandResult(exitCode)
