package nl.sybr.dev.command

import org.eclipse.jgit.api.Git
import picocli.CommandLine
import java.io.File

abstract class CommandContext  {

    @CommandLine.Option(
        names = ["--context"],
        description = ["The relative path to the target context."],
        defaultValue = "."
    )
    protected var commandContext: File = File(".")

    abstract fun callWithEnv() : CommandResult

    protected fun gitOpen(): Git {
        return Git.open(commandContext.resolve(".git"))
    }
}

open class CommandResult(open val exitCode: Int)
