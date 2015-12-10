package com.atlassian.jira.upgrade.tasks;
import java.util.List;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;

import org.apache.log4j.Logger;

/**
 * Requires a JIRA re-index to add lucene indexes to support Cascading Select fields.
 *
 * @since v6.1
 */
public class UpgradeTask_Build6142 extends AbstractUpgradeTask
{
    private final Logger log = Logger.getLogger(getClass());
    private final CustomFieldManager customFieldManager;

    public UpgradeTask_Build6142(final CustomFieldManager customFieldManager)
    {
        super(false);
        this.customFieldManager = customFieldManager;
    }
    @Override
    public String getBuildNumber()
    {
        return "6142";
    }

    @Override
    public String getShortDescription()
    {
        return "Run a reindex if the instance has Cascading Select fields since their values will now be added to the Lucene index.";
    }

    @Override
    public boolean isReindexRequired()
    {
        // Only perform a reindex if the instance has cascading fields
        List<CustomField> customFields = customFieldManager.getCustomFieldObjects();
        for (CustomField customField : customFields)
        {
            if (customField.getCustomFieldType() instanceof CascadingSelectCFType)
            {
                log.info("A Reindex will be performed because at least 1 Cascade Select Custom Field is present");
                return true;
            }
        }

        log.info("No reindex will be performed.");
        return false;
    }

    @Override
    public void doUpgrade(final boolean setupMode) throws Exception
    {
    }
}