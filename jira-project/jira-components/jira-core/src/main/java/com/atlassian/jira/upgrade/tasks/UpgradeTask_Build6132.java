package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericHelper;
import org.ofbiz.core.entity.jdbc.DatabaseUtil;
import org.ofbiz.core.entity.model.ModelEntity;
import org.ofbiz.core.entity.model.ModelIndex;

/**
 * Adding a unique constraint to the issue_proj_num index - unfortunately this means deleting it and then recreating it
 *
 * @since v6.1
 */
public class UpgradeTask_Build6132 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build6132.class);


    public UpgradeTask_Build6132()
    {
        super(false);
    }

    @Override
    public String getShortDescription()
    {
        return "Adding a unique constraint to the issue table for project and issue number pair";
    }

    @Override
    public String getBuildNumber()
    {
        return "6132";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        GenericHelper helper = getDelegator().getEntityHelper("Issue");
        DatabaseUtil dbUtil = new DatabaseUtil(helper.getHelperName());
        ModelEntity issueEntity = getDelegator().getModelEntity("Issue");
        ModelIndex projNumIndex = new ModelIndex();
        projNumIndex.setName("issue_proj_num");
        projNumIndex.setMainEntity(issueEntity);
        projNumIndex.setUnique(true);
        projNumIndex.addIndexField("number");
        projNumIndex.addIndexField("project");
        dropIndex(dbUtil, issueEntity, projNumIndex);
        createIndex(dbUtil, issueEntity, projNumIndex);
    }

    private void createIndex(final DatabaseUtil dbUtil, final ModelEntity issueEntity, final ModelIndex projNumIndex)
    {
        final String error = dbUtil.createDeclaredIndex(issueEntity, projNumIndex);
        if (error != null)
        {
            log.error("Update failed. Read more about possible reason: "
                    + "https://confluence.atlassian.com/display/JIRAKB/Upgrading+To+JIRA+6.1+Fails+Due+To+Duplicate+Issue+Keys");
            throw new RuntimeException("Could not create index: " + error);
        }
    }

    private void dropIndex(final DatabaseUtil dbUtil, final ModelEntity issueEntity, final ModelIndex projNumIndex)
    {
        final String error = dbUtil.deleteDeclaredIndex(issueEntity, issueEntity.getIndex(projNumIndex.getName()));
        if (error != null)
        {
            throw new RuntimeException("Could not drop index: " + error);
        }
    }

}
