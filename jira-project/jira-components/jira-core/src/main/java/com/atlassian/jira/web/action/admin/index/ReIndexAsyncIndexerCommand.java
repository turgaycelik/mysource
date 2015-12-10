package com.atlassian.jira.web.action.admin.index;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import com.atlassian.johnson.JohnsonEventContainer;
import com.google.common.annotations.VisibleForTesting;
import org.apache.log4j.Logger;

/**
 * Reindex is slightly different to Activate
 *
 * @since v3.13
 */
@Internal
@VisibleForTesting
public class ReIndexAsyncIndexerCommand extends AbstractAsyncIndexerCommand
{
    public ReIndexAsyncIndexerCommand(final JohnsonEventContainer eventCont, final IndexLifecycleManager indexManager, final Logger log, final I18nHelper i18nHelper, final I18nHelper.BeanFactory i18nBeanFactory)
    {
        super(eventCont, indexManager, log, i18nHelper, i18nBeanFactory);
    }

    @Override
    public IndexCommandResult doReindex(final Context context, final IndexLifecycleManager indexManager)
    {
        final long reindexTime = indexManager.reIndexAll(context);
        return new IndexCommandResult(reindexTime);
    }
}
