package nl.sybr.dev

import nl.sybr.dev.command.files.Add
import nl.sybr.dev.command.files.Copy
import nl.sybr.dev.command.files.Delete
import nl.sybr.dev.command.files.Move
import nl.sybr.dev.command.files.Update
import nl.sybr.dev.command.git.MergeRequest
import picocli.CommandLine

@CommandLine.Command(
    name = "changeit",
    description = ["ChangeIt container command"],
    mixinStandardHelpOptions = true,
    subcommands = [
        Add::class,
        Copy::class,
        Delete::class,
        Move::class,
        Update::class,
        MergeRequest::class
    ]
)
class Application : Runnable {
    override fun run() {
        println("Hello from ChangeIt!")
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            CommandLine.run(Application(), *args)
        }
    }
}