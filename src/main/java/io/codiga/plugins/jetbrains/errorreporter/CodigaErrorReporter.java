package io.codiga.plugins.jetbrains.errorreporter;

import com.intellij.ide.DataManager;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsActions;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.util.Consumer;
import com.rollbar.notifier.Rollbar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

import static com.rollbar.notifier.config.ConfigBuilder.withAccessToken;
import static io.codiga.plugins.jetbrains.Constants.PLUGIN_ID;

/**
 * Custom error reporter to send exceptions to rollbar.
 *
 * See https://www.plugin-dev.com/intellij/general/error-reporting/ for more information.
 */
public class CodigaErrorReporter extends ErrorReportSubmitter {
    private final static String ROLLBAR_ACCESS_TOKEN = "9c55e476e37246f7906681cbd4a66aa5";

    @Override
    public @NlsActions.ActionText @NotNull String getReportActionText() {
        return "Send to Codiga";
    }

    @NlsContexts.DetailedDescription
    @Nullable
    public String getPrivacyNoticeText() {
        return "Error will be shared with Codiga according to its privacy policy.";
    }

    @Override
    public boolean submit(@NotNull IdeaLoggingEvent[] events,
                          @Nullable String additionalInfo,
                          @NotNull Component parentComponent,
                          @NotNull Consumer<? super SubmittedReportInfo> consumer) {
        DataManager mgr = DataManager.getInstance();
        DataContext context = mgr.getDataContext(parentComponent);
        Project project = CommonDataKeys.PROJECT.getData(context);

        // make use of IntelliJ's background tasks
        new Task.Backgroundable(project, "Sending error report to Codiga") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    final Rollbar rollbar = Rollbar.init(
                            withAccessToken(ROLLBAR_ACCESS_TOKEN)
                                    .environment("production")
                                    .build());

                    for(IdeaLoggingEvent e: events) {
                        String pluginVersion = "unknown";
                        try {
                            pluginVersion = PluginManagerCore.getPlugin(PluginId.getId(PLUGIN_ID)).getVersion();
                        }
                        catch (NullPointerException npe) {
                            pluginVersion = "errorWhenTryingToGetVersion";
                        }
                        rollbar.error("Version: " + pluginVersion + "\n" + e.getThrowableText());
                    }
                    rollbar.close(true);
                    ApplicationManager.getApplication().invokeLater(() -> {
                        consumer.consume(new SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.NEW_ISSUE));
                    });
                } catch (Exception e) {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        consumer.consume(new SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.FAILED));
                    });
                }
            }
        }.queue(); // <-- don't miss the queue() call here!
        return true;
    }
}
