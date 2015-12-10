package com.atlassian.jira.web.action.admin.index;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import com.google.common.annotations.VisibleForTesting;
import org.apache.log4j.Logger;

/**
 * Reindex just the issues in the background
 *
 * @since v5.2
 */
@Internal
@VisibleForTesting
public class ReIndexBackgroundIndexerCommand extends AbstractAsyncIndexerCommand
{
    private final boolean reIndexComments;
    private final boolean reIndexChangeHistory;

    public ReIndexBackgroundIndexerCommand(final IndexLifecycleManager indexManager, final Logger log, final I18nHelper i18nHelper, final I18nHelper.BeanFactory i18nBeanFactory)
    {
        super(null, indexManager, log, i18nHelper, i18nBeanFactory);
        reIndexComments = false;
        reIndexChangeHistory = false;
    }

    public ReIndexBackgroundIndexerCommand(final IndexLifecycleManager indexManager, final boolean reIndexComments, final boolean reIndexChangeHistory, final Logger log, final I18nHelper i18nHelper, final I18nHelper.BeanFactory i18nBeanFactory)
    {
        super(null, indexManager, log, i18nHelper, i18nBeanFactory);
        this.reIndexComments = reIndexComments;
        this.reIndexChangeHistory = reIndexChangeHistory;
    }

    @Override
    public IndexCommandResult doReindex(final Context context, final IndexLifecycleManager indexManager)
    {
        final long reindexTime = indexManager.reIndexAllIssuesInBackground(context, reIndexComments, reIndexChangeHistory);
        return new IndexCommandResult(reindexTime);
    }
}
