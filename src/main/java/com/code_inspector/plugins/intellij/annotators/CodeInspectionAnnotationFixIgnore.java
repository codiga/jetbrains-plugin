package com.code_inspector.plugins.intellij.annotators;

import com.code_inspector.api.AddViolationToIgnoreMutation;
import com.code_inspector.api.type.LanguageEnumeration;
import com.code_inspector.plugins.intellij.cache.AnalysisDataCache;
import com.code_inspector.plugins.intellij.graphql.CodeInspectorApi;
import com.google.common.collect.ImmutableList;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.FileContentUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.code_inspector.plugins.intellij.Constants.LOGGER_NAME;
import static com.code_inspector.plugins.intellij.ui.UIConstants.ANNOTATION_FIX_IGNORE_FILE;
import static com.code_inspector.plugins.intellij.ui.UIConstants.ANNOTATION_FIX_IGNORE_PROJECT;

/**
 * This class runs all the code to ignore a violation. It triggers the GraphQL API to ignore a
 * violation and then reload the external analysis for the file.
 */
public class CodeInspectionAnnotationFixIgnore implements IntentionAction {

    private final Long projectId;
    private final String rule;
    private final LanguageEnumeration language;
    private final String tool;
    private final Optional<String> filename;
    private final PsiFile psiFile;

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    private final CodeInspectorApi codeInspectorApi = ServiceManager.getService(CodeInspectorApi.class);

    public CodeInspectionAnnotationFixIgnore(PsiFile _psiFile, Long _projectId, Optional<String> _filename, String _rule, LanguageEnumeration _language, String _tool) {
        this.projectId = _projectId;
        this.rule = _rule;
        this.language = _language;
        this.tool = _tool;
        this.filename = _filename;
        this.psiFile = _psiFile;
    }

    @Override
    public @IntentionName @NotNull String getText() {
        if(!this.filename.isPresent()) {
            return ANNOTATION_FIX_IGNORE_PROJECT;
        }
        return ANNOTATION_FIX_IGNORE_FILE;
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Code Inspector";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        LOGGER.debug("CodeInspectionIgnoreAnnotation - calling isAvailable()");
        return true;
    }

    /**
     * Invoke the fix, do the GraphQL request and reload the analysis for the file.
     * @param project
     * @param editor
     * @param file
     * @throws IncorrectOperationException
     */
    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        LOGGER.debug("CodeInspectionIgnoreAnnotation - calling invoke()");
        
        // Call the GraphQL API.
        Optional<AddViolationToIgnoreMutation.AddViolationToIgnore> res = codeInspectorApi.addViolationToIgnore(this.projectId, this.rule, this.tool, this.language, this.filename, Optional.empty(), "description");
        if(res.isPresent()) {
            LOGGER.debug(res.get().toString());
        } else {
        }

        // invalidate the cache so that at the next fetch, the data is being refetched
        AnalysisDataCache.getInstance().invalidateCache();

        // refresh this file so that the annotation disappear.
        FileContentUtil.reparseFiles(psiFile.getProject(), ImmutableList.of(psiFile.getVirtualFile()), true);
    }

    @Override
    public boolean startInWriteAction() {
        LOGGER.debug("CodeInspectionIgnoreAnnotation - calling startInWriteAction()");
        return true;
    }
}
