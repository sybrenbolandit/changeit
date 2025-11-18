package nl.sybr.dev.command.git

import nl.sybr.dev.command.*
import org.eclipse.jgit.api.Git
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "fresh",
    description = ["Create fresh git branch."],
)
class GitFresh : HistoryCommand(), Callable<Int> {

    @CommandLine.Option(
        names = ["-b", "--branch"],
        description = ["Name of the new branch."],
        required = true
    )
    protected lateinit var branchName: String


    override fun logCommand(): CommandConfig {
        return CommandConfig(
            "fresh",
            logContext(
                mutableListOf(
                    CommandArg("branch", branchName)
                )
            )
        )
    }

    override fun callCommand(): CommandResult {
        val git = gitOpen()
        val sourceBranch = git.repository.branch
        stash(git)
        createBranch(git, branchName)

        return GitFreshResult(0, sourceBranch, branchName)
    }

    override fun revert(commandDescription: CommandDescription) {
        val freshResult = commandDescription.result as GitFreshResult
        val git = gitOpen()
        git.checkout().setName(freshResult.sourceBranch).call()
        git.branchDelete().setBranchNames(freshResult.targetBranch).call()
        logger.info("Successfully deleted git branch: ${freshResult.targetBranch}")
    }

    private fun stash(git: Git) {
        logger.info("Stashing changes")
        git.stashCreate().call()
    }

    private fun createBranch(git: Git, branchName: String?) {
        logger.info("Creating new branch: $branchName")
        git.checkout().setCreateBranch(true).setName(branchName).call()
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(GitFresh::class.java)
    }
}

data class GitFreshResult(override val exitCode: Int, val sourceBranch: String, val targetBranch: String) :
    CommandResult(exitCode)
