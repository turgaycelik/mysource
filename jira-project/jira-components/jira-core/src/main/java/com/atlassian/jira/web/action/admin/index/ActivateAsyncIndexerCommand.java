package com.atlassian.jira.web.action.admin.index;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import com.atlassian.johnson.JohnsonEventContainer;
import org.apache.log4j.Logger;

/**
 * Activate is every so slightly different to re-index
 *
 * @since v3.13
 */
@Internal
public class ActivateAsyncIndexerCommand extends AbstractAsyncIndexerCommand
{
    private final boolean deactivateIndexFirst;

    public ActivateAsyncIndexerCommand(final boolean deactivateIndexFirst, final JohnsonEventContainer eventCont, final IndexLifecycleManager indexManager, final Logger log, final I18nHelper i18nHelper, final I18nHelper.BeanFactory i18nBeanFactory)
    {
        super(eventCont, indexManager, log, i18nHelper, i18nBeanFactory);
        this.deactivateIndexFirst = deactivateIndexFirst;
    }

    @Override
    public IndexCommandResult doReindex(final Context context, final IndexLifecycleManager indexManager)
    {
        try
        {
            if (deactivateIndexFirst)
            {
                indexManager.deactivate();
            }
            final long reindexTime = indexManager.activate(context);
            return new IndexCommandResult(reindexTime);
        }
        catch (final Exception e)
        {
            getLog().error("Exception reindexing: " + e, e);

            final ErrorCollection errors = new SimpleErrorCollection();
            errors.addErrorMessage(getI18nHelper().getText("admin.errors.error.while.activating.indexes") + " " + e);
            return new IndexCommandResult(errors);
        }
    }
}
