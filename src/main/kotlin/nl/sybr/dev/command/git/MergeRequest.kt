package nl.sybr.dev.command.git

import nl.sybr.dev.command.*
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand
import org.eclipse.jgit.transport.PushResult
import org.eclipse.jgit.transport.RefSpec
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "mr",
    description = ["Create a merge request."],
)
class MergeRequest : HistoryCommand(), Callable<Int> {

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


    override fun logCommand(): CommandConfig {
        return CommandConfig(
            "mr",
            logContext(
                mutableListOf(
                    CommandArg("gitlevel", gitlevel)
                )
            )
        )
    }

    override fun callCommand(): CommandResult {
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

    override fun revert(commandDescription: CommandDescription) {
        val mrResult = commandDescription.result as MergeRequestResult
        val git = gitOpen()
        if (mrResult.mrUrl != null) logger.info("Closing MR (by deleting source branch).")
        if (mrResult.pushed) deleteRemoteBrach(git)
        if (mrResult.committed) reset(git)
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

    private fun deleteRemoteBrach(git: Git) {
        logger.info("Delete remote brach")
        val refSpec = RefSpec()
            .setSource(null)
            .setDestination("refs/heads/${git.repository.branch}")
        git.push().setRefSpecs(refSpec).call()
    }

    private fun reset(git: Git) {
        logger.info("Reset hard ~1")
        git.reset().setRef("HEAD~1").setMode(ResetCommand.ResetType.HARD).call()
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
    val mrUrl: String?
) : CommandResult(exitCode)
