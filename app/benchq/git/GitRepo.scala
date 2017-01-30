package benchq
package git

import benchq.model.{Branch, KnownRevision}
import better.files._

import scala.sys.process._
import scala.util.Try

class GitRepo(config: Config) {
  import config.GitRepo._

  val repoUrl = "https://github.com/scala/scala.git"
  def checkoutDirectory = File(checkoutLocation)
  def checkoutDirectoryJ = checkoutDirectory.toJava

  def cloneIfNonExisting(): Unit = {
    if (!checkoutDirectory.exists)
      Process(s"git clone $repoUrl ${checkoutDirectory.name}", checkoutDirectory.parent.toJava).!!
  }

  def fetchOrigin(): Unit = {
    cloneIfNonExisting()
    Process("git fetch -f origin --tags", checkoutDirectoryJ).!!
  }

  def newMergeCommitsSince(knownRevision: KnownRevision): List[String] = {
    fetchOrigin()
    // --first-parent to pick only merge commits, and direct commits to the branch
    // http://stackoverflow.com/questions/10248137/git-how-to-list-commits-on-this-branch-but-not-from-merged-branches
    Process(
      s"git log --first-parent --pretty=format:'%H' ${knownRevision.revision}..origin/${knownRevision.branch.entryName}",
      checkoutDirectoryJ).lineStream.toList
  }

  def leastBranchContaining(sha: String): Try[Option[Branch]] = {
    fetchOrigin()
    val originPrefix = "origin/"
    Try {
      // Throws an exception if `sha` is not known
      val containingBranches =
        Process(s"git branch -r --contains $sha", checkoutDirectoryJ).lineStream
          .map(_.trim)
          .collect({
            case s if s.startsWith(originPrefix) => s.substring(originPrefix.length)
          })
          .toSet
      Branch.values
        .filter(b => containingBranches(b.entryName))
        .toList
        .sortBy(_.entryName)
        .headOption
    }
  }
}
