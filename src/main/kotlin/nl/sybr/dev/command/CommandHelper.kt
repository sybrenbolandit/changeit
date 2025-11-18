package nl.sybr.dev.command

import nl.sybr.dev.command.files.Add
import nl.sybr.dev.command.files.Copy
import nl.sybr.dev.command.files.Delete
import nl.sybr.dev.command.files.Move
import nl.sybr.dev.command.files.Update
import nl.sybr.dev.command.git.MergeRequest
import picocli.CommandLine
import java.io.File
import java.util.stream.Collectors

fun findCommandClass(name: String): HistoryCommand {
    return when(name) {
        "add" -> Add()
        "delete" -> Delete()
        "move" -> Move()
        "copy" -> Copy()
        "update" -> Update()
        "mr" -> MergeRequest()
        else -> throw IllegalStateException("Unknown command: $name")
    }
}

fun callCommand(commandDescription: CommandDescription, context: File): Int {
    val command = findCommandClass(commandDescription.commandConfig.name)
    val cmd = CommandLine(command)

    val argList = commandDescription.commandConfig.args.stream()
        .map { arg ->  "--${arg.key}=${arg.value}" }
        .collect(Collectors.toCollection(::ArrayList))
    argList.add("--context=$context")
    argList.add("--no-history=true")

    return cmd.execute(*argList.toTypedArray())
}