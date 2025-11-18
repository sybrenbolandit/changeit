package nl.sybr.dev.command.history

import picocli.CommandLine

@CommandLine.Command(
    name = "history",
    subcommands = [
        HistoryShow::class,
        HistoryDelete::class,
    ]
)
class History