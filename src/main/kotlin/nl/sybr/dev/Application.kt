package nl.sybr.dev

import nl.sybr.dev.command.files.*
import nl.sybr.dev.command.git.GitFresh
import nl.sybr.dev.command.git.MergeRequest
import nl.sybr.dev.command.history.History
import nl.sybr.dev.command.history.Redo
import nl.sybr.dev.command.history.Revert
import picocli.CommandLine

@CommandLine.Command(
    name = "changeit",
    description = ["ChangeIt container command"],
    mixinStandardHelpOptions = true,
    subcommands = [
        GitFresh::class,
        Add::class,
        Copy::class,
        Delete::class,
        Move::class,
        Update::class,
        MergeRequest::class,
        History::class,
        Revert::class,
        Redo::class,
    ]
)
class Application : Runnable {
    override fun run() {
        println("Hello from ChangeIt!")
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            CommandLine(Application()).execute(*args)
        }
    }
}