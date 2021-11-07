package io.codiga.plugins.jetbrains.git;

import io.codiga.plugins.jetbrains.model.FileLinePair;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.diff.impl.patch.*;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.vcsUtil.VcsUtil;
import git4idea.GitUtil;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;

import java.util.*;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;

public final class CodigaGitUtils {
    private static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    /**
     * Private constructor so that this class is never instantiated.
     */
    private CodigaGitUtils() { }

    /**
     * Get the current git revision for the file passed as parameter. If no revision is available and/or
     * the project is not a git project, returns Optional.empty()
     *
     * WARNING: this function works only when we have one repository.
     *
     * @param psiFile - the resource select in the UI
     *
     * @return - the revision if one is found.
     */
    public static Optional<String> getGitRevision(PsiFile psiFile) {

        GitRepositoryManager repositoryManager = GitUtil.getRepositoryManager(psiFile.getProject());
        java.util.List<GitRepository> repositories = repositoryManager.getRepositories();
        for(GitRepository gitRepository: repositories){
            if (gitRepository.getCurrentRevision() != null) {
                return Optional.of(gitRepository.getCurrentRevision());
            }
        }
        return Optional.empty();
    }

    /**
     * Return the root object of the repository for a given file.
     * @param psiFile - the file we are inspector
     * @return - the root repository if it even exists.
     */
    public static Optional<VirtualFile> getRepositoryRoot(PsiFile psiFile) {
        try {
            return Optional.of(GitUtil.getRootForFile(psiFile.getProject(), psiFile.getVirtualFile()));
        } catch (VcsException e) {
            LOGGER.debug("[CodigaGitUtils] no vcs found");
            return Optional.empty();
        }
    }

    /**
     * Get the changes for a file and compare with the latest git revision.
     * @param psiFile - the resrouce to get
     * @return the list of Change.
     */
    public static Collection<Change> getWorkingChanges(PsiFile psiFile) {
        try {
            GitRepository gitRepository = GitUtil.getRepositoryForFile(psiFile.getProject(), psiFile.getVirtualFile());
            FilePath filePath = VcsUtil.getFilePath(psiFile.getVirtualFile());
            String revision = gitRepository.getCurrentRevision();
            java.util.List<FilePath> dirtyPaths = ImmutableList.of(filePath);
            return git4idea.changes.GitChangeUtils.getDiffWithWorkingDir(psiFile.getProject(), gitRepository.getRoot(), revision, dirtyPaths, false, false);
        } catch (VcsException e) {
            LOGGER.error("error in getWorkingChanges", e);
        }

        return ImmutableList.of();
    }


    /**
     * Return the file status for a given file in the project.
     * @param psiFile - the PsiFile to inspect/get
     * @return the status of the file (if modified or not).
     */
    public static FileStatus getFileStatus(PsiFile psiFile) {
        ChangeListManager changeListManager = ChangeListManager.getInstance(psiFile.getProject());
        return changeListManager.getStatus(psiFile.getVirtualFile());
    }

    public static Optional<String> getFilePathInRepository(PsiFile psiFile) {
        try {
            GitRepository gitRepository = GitUtil.getRepositoryForFile(psiFile.getProject(), psiFile.getVirtualFile());
            String canonicalPath = gitRepository.getRoot().getCanonicalPath();
            if (canonicalPath != null) {
                return Optional.ofNullable(GitUtil.getRelativePath(canonicalPath, VcsUtil.getFilePath(psiFile.getVirtualFile())));
            }
        } catch (VcsException e) {
            LOGGER.error("getFilePathInRepository, cannot get repository", e);
        }

        return Optional.empty();
    }

    /**
     * Get all the changes for a patch for the working directory. This is the equivalent
     * of doing git diff <filename>
     * @param psiFile - the file we are inspecting and wanting the changes against.
     * @return the list of patch or nothing.
     */
    public static List<TextFilePatch> getPatchesForWorkingDirectoryForFile(PsiFile psiFile) {
        Optional<VirtualFile> repositoryRoot = CodigaGitUtils.getRepositoryRoot(psiFile);
        Optional<String> filePath = CodigaGitUtils.getFilePathInRepository(psiFile);

        if(!repositoryRoot.isPresent()) {
            LOGGER.debug("[getPatchesForWorkingDirectoryForFile] cannot find the repository root");
            return ImmutableList.of();
        }

        if(!filePath.isPresent()) {
            LOGGER.debug("[getPatchesForWorkingDirectoryForFile] cannot find the file path in the repository");
            return ImmutableList.of();
        }

        // create a git command to execute to get the diff
        GitLineHandler gitLineHandler = new GitLineHandler(psiFile.getProject(), repositoryRoot.get(), GitCommand.DIFF);
        gitLineHandler.addParameters(filePath.get());

        // execute the command
        GitCommandResult result = Git.getInstance().runCommand(gitLineHandler);
        if (!result.success()) {
            LOGGER.debug("[getPatchesForWorkingDirectoryForFile] git command failed");
            return ImmutableList.of();
        }

        String patchContent = String.join("\n", result.getOutput());

        LOGGER.debug(String.format("[getPatchesForWorkingDirectoryForFile]  patch content %s", patchContent));

        return getTextFilePatchesFromDiff(patchContent);
    }

    /**
     * Get all the TextFilePatch for a given diff
     * @param patchContent
     * @return
     */
    @VisibleForTesting
    public static List<TextFilePatch> getTextFilePatchesFromDiff(final String patchContent) {
        PatchReader patchReader = new PatchReader(patchContent);
        try{
            return patchReader.readTextPatches();
        }
        catch (PatchSyntaxException patchSyntaxException) {
            LOGGER.debug("failed to parse patches");
        }
        return ImmutableList.of();
    }


    /**
     * For all patches, let's get the new position of all the context file. We do not index all the add or delete
     * for now as this is not what we want to index and Codiga does not show anything on modified code
     * from the current directory.
     * @param textFilePatches - the list of patch files we have
     * @return a map that indicate the initial position of a file/line and the new position, using only context info from patches.
     */
    public static Map<FileLinePair, FileLinePair> indexPatchHunks(List<TextFilePatch> textFilePatches) {
        Map<FileLinePair, FileLinePair> result = new HashMap<>();
        for (TextFilePatch tfp: textFilePatches) {
            if (!tfp.getBeforeFileName().equals(tfp.getAfterFileName())) {
                continue;
            }
            for (PatchHunk ph: tfp.getHunks()) {
                int beforeStartLine = ph.getStartLineBefore() + 1;
                int afterStartLine =  ph.getStartLineAfter() + 1;
                for (PatchLine pl: ph.getLines()) {
                    FileLinePair key = new FileLinePair(tfp.getBeforeName(), beforeStartLine);
                    FileLinePair value = new FileLinePair(tfp.getAfterName(), afterStartLine);
                    if (pl.getType() == PatchLine.Type.CONTEXT) {
                        result.put(key, value);
                        beforeStartLine = beforeStartLine + 1;
                        afterStartLine = afterStartLine + 1;
                    }
                    if (pl.getType() == PatchLine.Type.ADD) {
                        afterStartLine = afterStartLine + 1;
                    }
                    if (pl.getType() == PatchLine.Type.REMOVE) {
                        beforeStartLine = beforeStartLine + 1;
                    }
                }
            }
        }
        return result;
    }
}
