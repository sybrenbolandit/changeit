package nl.sybr.dev.command.files

import nl.sybr.dev.command.CommandArg
import nl.sybr.dev.command.HistoryCommand
import org.slf4j.LoggerFactory
import picocli.CommandLine
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.*

abstract class FilesCommand : HistoryCommand() {

    @CommandLine.Option(
        names = ["--files"],
        description = ["Specify which files to target."],
        required = true
    )
    protected lateinit var fileFilter: String

    @CommandLine.Option(
        names = ["--nr", "--non-recursive"],
        description = ["Flag if you want to target files onl in current directory."],
        defaultValue = "false"
    )
    protected var nonRecursive: Boolean = false

    protected fun eligibleFiles(context: Path): List<Path> {
        val matchesList: MutableList<Path> = mutableListOf()
        val matcherVisitor: FileVisitor<Path> = object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                val fs = FileSystems.getDefault()
                val matcher = fs.getPathMatcher("glob:$fileFilter")
                if (matcher.matches(file.fileName)) {
                    matchesList.add(file)
                }
                return FileVisitResult.CONTINUE
            }
        }
        try {
            Files.walkFileTree(
                context,
                EnumSet.noneOf(FileVisitOption::class.java),
                if (nonRecursive) 1 else Int.MAX_VALUE,
                matcherVisitor
            )
        } catch (exception: IOException) {
            throw IllegalStateException("Error in matching files.", exception)
        }
        logger.info("Number of eligible files: ${matchesList.size}")

        return matchesList
    }

    protected fun logFileFilter(args: List<CommandArg>): List<CommandArg> {
        val extendedArgs = args.plus(CommandArg("files", fileFilter))
        return if (nonRecursive) extendedArgs.plus(CommandArg("nonRecursive", nonRecursive.toString())) else extendedArgs
    }

    companion object {
        val logger = LoggerFactory.getLogger(FilesCommand::class.java)
    }
}