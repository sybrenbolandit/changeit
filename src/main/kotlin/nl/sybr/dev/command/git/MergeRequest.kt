package nl.sybr.dev.command.git

import nl.sybr.dev.command.CommandContext
import nl.sybr.dev.command.CommandResult
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.PushResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine

@CommandLine.Command(
    name = "mr",
    description = ["Create a merge request."],
)
class MergeRequest : CommandContext() {

    @CommandLine.Option(
        names = ["--gitlevel"],
        description = ["Do you want to create a merge request? [commit|push|mr]"],
        required = true
    )
    protected lateinit var gitlevel: String

    @CommandLine.Option(
        names = ["-m", "--message"],
        description = ["The commit message."]
    )
    protected var commitMessage: String? = null


    override fun callWithEnv(): CommandResult {
        val git = gitOpen()

        val commit = listOf("commit", "push", "mr").contains(gitlevel)
        if (commit) {
            if (commitMessage == null) {
                commitMessage = System.console().readLine("Commit message: ")
            }
            add(git)
            commit(git, commitMessage)
        }

        return createMr(git, gitlevel, commit, commitMessage!!)
    }

    private fun createMr(git: Git, gitlevel: String, committed: Boolean, commitMessage: String): MergeRequestResult {
        var mrUrl: String? = null
        val push = listOf("push", "mr").contains(gitlevel)
        if (push) {
            mrUrl = push(git, "mr".equals(gitlevel))
        }

        return MergeRequestResult(0, committed, commitMessage, push, mrUrl)
    }

    private fun add(git: Git) {
        logger.info("Add .")
        git.add().addFilepattern(".").call()
    }

    private fun commit(git: Git, message: String?) {
        logger.info("Commit")
        git.commit().setMessage(message).call()
    }

    private fun push(git: Git, createMr: Boolean): String? {
        logger.info("Push")
        val pushCommand = git.push()

        if (createMr) {
            logger.info("Create MR")
            pushCommand.setPushOptions(mutableListOf("merge_request.create"))
        }

        val pushResults: Iterable<PushResult> = pushCommand.call()
        if (createMr) {
            val mrUrl = """https://\S*""".toRegex().find(pushResults.first().messages)
            logger.info("MR URL: $mrUrl")
            return mrUrl?.value ?: throw IllegalStateException("MR URL not present in git response.")
        }

        return null
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(MergeRequest::class.java)
    }
}

data class MergeRequestResult(
    override val exitCode: Int,
    val committed: Boolean,
    val commitMessage: String?,
    val pushed: Boolean,
    val mrUrl: String?): CommandResult(exitCode)
