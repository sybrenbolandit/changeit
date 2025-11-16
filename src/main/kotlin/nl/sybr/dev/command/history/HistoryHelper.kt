package nl.sybr.dev.command.history

import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import nl.sybr.dev.command.CommandDescription
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.*

val dateFormat = SimpleDateFormat("yyyyMMdd-HHmmss")
    get() = field

fun createNewHistory(date: Date): File {
    val logDir = logDir()
    logDir.mkdirs()
    val logFile = logDir.resolve("command-${dateFormat.format(date)}.json")
    logFile.createNewFile()
    deleteRevertedConfigs()

    return logFile
}

fun writeHistory(command: CommandDescription) {
    val historyFile = findHistoryFile(command.dateTime)
    val configJson = jacksonMapperBuilder().build().writerWithDefaultPrettyPrinter().writeValueAsString(command)
    Files.write(historyFile.toPath(), configJson.toByteArray(StandardCharsets.UTF_8))
}

fun allCommands(): List<CommandDescription> {
    return historyFiles()
        .filter { it.length() > 0 }
        .map { file -> jacksonMapperBuilder().build().readValue(file, CommandDescription::class.java) }
        .sortedBy { it.dateTime }
        .reversed()
}

fun deleteHistory(command: CommandDescription) {
    FileUtils.delete(findHistoryFile(command.dateTime))
}

fun currentCommand(): CommandDescription? {
    return allCommands().filter { !it.reverted }.maxByOrNull { it.dateTime }
}

fun writeRevertCommand(command: CommandDescription) {
    command.reverted = true
    writeHistory(command)
}

fun writeUnrevertCommand(command: CommandDescription) {
    command.reverted = false
    writeHistory(command)
}

fun lastRevertedCommand(): CommandDescription? {
    return allCommands().filter { it.reverted }.minByOrNull { it.dateTime }
}

private fun logDir(): File {
    return File("${System.getProperty("user.home")}/.cit/history")
}

private fun deleteRevertedConfigs() {
    allCommands()
        .filter { it.reverted }
        .forEach { FileUtils.delete(findHistoryFile(it.dateTime))}
}

private fun historyFiles(): List<File> {
    return logDir().listFiles()?.toList() ?: listOf()
}

private fun findHistoryFile(date: Date): File {
    return historyFiles().find { file -> file.name == "command-${dateFormat.format(date)}.json" }
        ?: throw RuntimeException("Could not find history file")
}