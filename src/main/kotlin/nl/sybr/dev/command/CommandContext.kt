package nl.sybr.dev.command

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import nl.sybr.dev.command.files.AddResult
import nl.sybr.dev.command.files.CopyResult
import nl.sybr.dev.command.files.DeleteResult
import nl.sybr.dev.command.files.MoveResult
import nl.sybr.dev.command.files.UpdateResult
import nl.sybr.dev.command.git.MergeRequestResult
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

    protected val mapper = jacksonMapperBuilder().enable(SerializationFeature.INDENT_OUTPUT).build()

    protected fun logContext(args: List<CommandArg>): List<CommandArg> {
        return if (commandContext.name != ".") args.plus(CommandArg("context", commandContext.toString())) else args
    }

    protected fun gitOpen(): Git {
        return Git.open(commandContext.resolve(".git"))
    }
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = AddResult::class, name = "AddResult"),
    JsonSubTypes.Type(value = DeleteResult::class, name = "DeleteResult"),
    JsonSubTypes.Type(value = MoveResult::class, name = "MoveResult"),
    JsonSubTypes.Type(value = CopyResult::class, name = "CopyResult"),
    JsonSubTypes.Type(value = UpdateResult::class, name = "UpdateResult"),
    JsonSubTypes.Type(value = MergeRequestResult::class, name = "MergeRequestResult"),
)
open class CommandResult(open val exitCode: Int)
