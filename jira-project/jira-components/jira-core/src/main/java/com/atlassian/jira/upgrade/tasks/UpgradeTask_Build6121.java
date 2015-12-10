package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.entity.Delete;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.entity.ViewEntity;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.MovedIssueKey;
import com.atlassian.jira.model.ChangeGroup;
import com.atlassian.jira.model.ChangeItem;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.util.Visitor;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

/**
 * Populate the MovedIssueKey DB table from Change History Items.
 *
 * @since v6.1
 */
public class UpgradeTask_Build6121 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build6121.class);

    public UpgradeTask_Build6121()
    {
        super(false);
    }

    @Override
    public String getBuildNumber()
    {
        return "6121";
    }

    @Override
    public String getShortDescription()
    {
        return "Populate the MovedIssueKey DB table from Change History Items.";
    }

    @Override
    public void doUpgrade(final boolean setupMode) throws Exception
    {
        final EntityEngine entityEngine = getEntityEngine();
        // First clear the MovedIssueKey table, just in case we are being re-run
        entityEngine.delete(Delete.from(Entity.MOVED_ISSUE_KEY).all());

        // Find all the Change Items for the Issue Key field and build the MovedIssueKey table from that.
        Select.columns(ChangeItem.OLDSTRING, ChangeGroup.ISSUE)
                .from(ViewEntity.Name.CHANGE_GROUP_CHANGE_CHANGE_ITEM)
                .whereEqual(ChangeItem.FIELD, "Key")
                .runWith(entityEngine)
                .visitWith(new ChangeItemVisitor(entityEngine));
    }

    private final static class ChangeItemVisitor implements Visitor<GenericValue>
    {
        private final EntityEngine entityEngine;

        private ChangeItemVisitor(final EntityEngine entityEngine)
        {
            this.entityEngine = entityEngine;
        }

        @Override
        public void visit(final GenericValue entity)
        {
            // Check if we already have a mapping for this key. This theoretically should not happen, except
            // bugs in older versions of JIRA meant that issue keys would sometimes get re-used :(
            final MovedIssueKey movedIssueKey = new MovedIssueKey(null, entity.getString(ChangeItem.OLDSTRING), entity.getLong(ChangeGroup.ISSUE));
            try
            {
                entityEngine.createValue(Entity.MOVED_ISSUE_KEY, movedIssueKey);
            }
            catch (DataAccessException ex)
            {
                // This can possibly fail for a duplicate entry if the same issue key is moved twice.
                // This can happen because old JIRA had a bug in the project counter where you could sometimes have a moved issue key get re-used :(
                MovedIssueKey existingValue = Select.from(Entity.MOVED_ISSUE_KEY)
                        .whereEqual(MovedIssueKey.OLD_ISSUE_KEY, movedIssueKey.getOldIssueKey())
                        .runWith(entityEngine)
                        .singleValue();
                if (existingValue == null)
                {
                    // nope - some other error
                    throw ex;
                }
                else
                {
                    log.warn("Found duplicate moved issue key information for issue " + movedIssueKey.getOldIssueKey() + ". Ignoring issue " + movedIssueKey.getIssueId());
                }
            }
        }
    }
}
