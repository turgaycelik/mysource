
package com.atlassian.jira.upgrade.tasks;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.atlassian.jira.entity.Delete;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.EntityListConsumer;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.entity.SelectQuery;
import com.atlassian.jira.entity.Update;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.security.roles.actor.UserRoleActorFactory;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.upgrade.tasks.util.FindMixedCaseUsernames;
import com.atlassian.jira.util.Visitor;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

// JRADEV-16740

/**
 * Upgrade task to convert username fields to lowercase.  This is
 * required as part of the preparation for rename user to work.
 *
 * @since v6.0
 */
public class UpgradeTask_Build6039 extends AbstractUpgradeTask
{
    // NOTE: 6096 reuses parts of this upgrade task!
    private final Logger log = Logger.getLogger(getClass());

    private static final String CF_TYPE_USER_PICKER = "com.atlassian.jira.plugin.system.customfieldtypes:userpicker";
    private static final String CF_TYPE_MULTI_USER_PICKER = "com.atlassian.jira.plugin.system.customfieldtypes:multiuserpicker";

    private final EntityEngine entityEngine;
    private final boolean debug;
    private boolean needReindex = false;

    public UpgradeTask_Build6039(EntityEngine entityEngine)
    {
        super(false);
        this.entityEngine = entityEngine;
        this.debug = log.isDebugEnabled();
    }

    @Override
    public String getBuildNumber()
    {
        return "6039";
    }

    @Override
    public boolean isReindexRequired()
    {
        // We need to reindex if we actually convert the case on anything that gets indexed,
        // like the assignee, reporter, change authors, and custom field values.  Strictly
        // speaking, this is pessimistic, as we may have already forced the old values to
        // lowercase in the past, but trying to establish which ones do and which ones
        // don't is time consuming and error prone.  At least we can avoid the reindex if
        // all of the usernames were already in lowercase.
        return needReindex;
    }

    @Override
    public String getShortDescription()
    {
        return "Convert username references to lowercase so that they can be used as keys";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        /** Note: ChangeGroup and ChangeItem were already handled in {@link UpgradeTask_Build6038}. */

        log.info("Updating username references in issue fields ...");
        updateSimpleColumn("Issue", "assignee", true);  // Assignee/Reporter get indexed
        updateSimpleColumn("Issue", "reporter", true);
        updateCustomFieldValuesForType(CF_TYPE_USER_PICKER);
        updateCustomFieldValuesForType(CF_TYPE_MULTI_USER_PICKER);

        log.info("Updating username references in comment authors ...");
        updateSimpleColumn("Action", "author", true);
        updateSimpleColumn("Action", "updateauthor", true);

        log.info("Updating username references in ownership relations (searches, favourites, etc.) ...");
        updateSimpleColumn("ColumnLayout", "username");
        updateSimpleColumn("Component", "lead");
        updateSimpleColumn("PortalPage", "username", true);
        updateSimpleColumn("Project", "lead");
        updateSimpleColumn("SearchRequest", "author" , true);
        updateSimpleColumn("SearchRequest", "user", true);
        updateSimpleColumn("FavouriteAssociations", "username");
        updateSimpleColumn("UserAssociation", "sourceName", true); // Voters/Watchers get indexed
        updateSimpleColumn("UserHistoryItem", "username");
        updateUserHistoryItemEntityId();

        // JRADEV-21357
        doWorklogUpgrade();

        // JRADEV-22456
        doUserProjectRoleActorUpgrade();
    }

    protected void doWorklogUpgrade() throws SQLException
    {
        log.info("Updating username references in worklogs ...");
        updateSimpleColumn("Worklog", "author");
        updateSimpleColumn("Worklog", "updateauthor");
    }

    protected void doUserProjectRoleActorUpgrade() throws SQLException
    {
        log.info("Updating username references in project roles ...");

        // Find distinct field values that need converting
        final SelectQuery<String> selectQuery = selectDistinctString("roletypeparameter").from("ProjectRoleActor")
                .whereEqual("roletype", UserRoleActorFactory.TYPE);
        final Map<String,String> usernameMap = toUsernameMap(selectQuery);
        if (usernameMap.isEmpty())
        {
            return;
        }

        needReindex = true;
        for (Map.Entry<String,String> entry : usernameMap.entrySet())
        {
            debugUserUpdate(entry);
            entityEngine.execute(Update
                    .into("ProjectRoleActor")
                    .set("roletypeparameter", entry.getValue())
                    .whereEqual("roletype", UserRoleActorFactory.TYPE)
                    .andEqual("roletypeparameter", entry.getKey()));
        }
    }



    private void debugUserUpdate(final Map.Entry<String,String> entry)
    {
        if (debug)
        {
            log.debug("    " + entry.getKey() + " -> " + entry.getValue());
        }
    }

    private void updateSimpleColumn(final String entityName, final String fieldName) throws SQLException
    {
        updateSimpleColumn(entityName, fieldName, false);
    }

    private void updateSimpleColumn(final String entityName, final String fieldName, final boolean triggersReindex)
            throws SQLException
    {
        if (debug)
        {
            log.debug("  updateSimpleColumn(" + entityName + '.' + fieldName + ')');
        }

        // Find distinct field values that need converting
        final SelectQuery<String> selectQuery = selectDistinctString(fieldName).from(entityName);
        final Map<String,String> usernameMap = toUsernameMap(selectQuery);
        if (usernameMap.isEmpty())
        {
            return;
        }

        if (triggersReindex)
        {
            needReindex = true;
        }
        for (Map.Entry<String,String> entry : usernameMap.entrySet())
        {
            debugUserUpdate(entry);
            updateNamesToLowercase(entityName, fieldName, entry.getKey(), entry.getValue());
        }
    }

    private void updateNamesToLowercase(String entityName, String fieldName, String mixedName, String loweredName)
    {
        try
        {
            entityEngine.execute(Update
                    .into(entityName)
                    .set(fieldName, loweredName)
                    .whereEqual(fieldName, mixedName));
        }
        catch (DataAccessException dae)
        {
            // The exception is very likely due to violation of a uniqueness constraint.
            // Check which, if any, of the constrained tables caused the exception, then deal with the colliding rows
            // one-at-a-time

            if ("UserHistoryItem".equals(entityName))
            {
                // Loss of user history is no big deal, so just blow the offending rows away.
                entityEngine.delete(Delete.from(entityName).whereEqual(fieldName, mixedName));
            }
            else if ("UserAssociation".equals(entityName))
            {
                Select.WhereContext<GenericValue> allRowsForUser =
                        Select.columns("sourceName", "sinkNodeId", "sinkNodeEntity", "associationType")
                                .from(entityName)
                                .whereEqual("sourceName", mixedName);
                entityEngine.run(allRowsForUser).visitWith(new AssociationVisitor(entityEngine, loweredName));
            }
            else if ("FavouriteAssociations".equals(entityName))
            {
                Select.WhereContext<GenericValue> allRowsForUser =
                        Select.columns("id", "username", "entityType", "entityId")
                                .from(entityName)
                                .whereEqual("username", mixedName);
                entityEngine.run(allRowsForUser).visitWith(new FavouriteVisitor(entityEngine, loweredName));
            }
            else
            {
                // The above tables are the only candidates for a constraint-related exception in this upgrade task.
                // Therefore, this exception must be caused by something else.
                throw dae;
            }
        }
    }

    private static class AssociationVisitor implements Visitor<GenericValue>
    {
        private final EntityEngine entityEngine;
        private String loweredName;

        public AssociationVisitor(EntityEngine entityEngine, String loweredName)
        {
            this.entityEngine = entityEngine;
            this.loweredName = loweredName;
        }

        @Override
        public void visit(GenericValue element)
        {
            if (loweredName.equals(element.getString("sourceName")))
            {
                return;
            }

            if (lowercasedVersionExists(element))
            {
                deleteThisRow(element);
            }
            else
            {
                lowercaseThisRow(element);
            }
        }

        // The primary key spans 4 columns in this table, so ugly queries are here abstracted out.
        private boolean lowercasedVersionExists(GenericValue element)
        {
            Select.WhereContext lowercasedVersionQuery = Select.columns("sinkNodeId").from("UserAssociation")
                    .whereEqual("sourceName", loweredName)
                    .andEqual("sinkNodeId", element.getLong("sinkNodeId"))
                    .andEqual("sinkNodeEntity", element.getString("sinkNodeEntity"))
                    .andEqual("associationType", element.getString("associationType"));
            return !entityEngine.run(lowercasedVersionQuery).asList().isEmpty();
        }

        private void deleteThisRow(GenericValue element)
        {
            Delete.DeleteWhereContext delete = Delete.from("UserAssociation")
                    .whereEqual("sourceName", element.getString("sourceName"))
                    .andEqual("sinkNodeId", element.getLong("sinkNodeId"))
                    .andEqual("sinkNodeEntity", element.getString("sinkNodeEntity"))
                    .andEqual("associationType", element.getString("associationType"));
            entityEngine.delete(delete);
        }

        private void lowercaseThisRow(GenericValue element)
        {
            Update.WhereContext lowercase = Update.into("UserAssociation")
                    .set("sourceName", loweredName)
                    .whereEqual("sourceName", element.getString("sourceName"))
                    .andEqual("sinkNodeId", element.getLong("sinkNodeId"))
                    .andEqual("sinkNodeEntity", element.getString("sinkNodeEntity"))
                    .andEqual("associationType", element.getString("associationType"));
            entityEngine.execute(lowercase);
        }
    }

    private static class FavouriteVisitor implements Visitor<GenericValue>
    {
        private final EntityEngine entityEngine;
        private String loweredName;

        public FavouriteVisitor (EntityEngine entityEngine, String loweredName)
        {
            this.entityEngine = entityEngine;
            this.loweredName = loweredName;
        }

        @Override
        public void visit(GenericValue element)
        {
            if (loweredName.equals(element.getString("username")))
            {
                return;
            }
            final Select.WhereContext lowercasedVersionExistsQuery =
                    Select.columns("id").from("FavouriteAssociations")
                            .whereEqual("username", loweredName)
                            .andEqual("entityType", element.getString("entityType"))
                            .andEqual("entityId", element.getLong("entityId"));

            if (entityEngine.run(lowercasedVersionExistsQuery).asList().isEmpty())
            {
                entityEngine.execute(Update.into("FavouriteAssociations").set("username", loweredName).whereIdEquals(element.getLong("id")));
            }
            else
            {
                entityEngine.delete(Delete.from("FavouriteAssociations").whereIdEquals(element.getLong("id")));
            }
        }
    }

    private void updateUserHistoryItemEntityId() throws SQLException
    {
        if (debug)
        {
            log.debug("  updateUserHistoryItemEntityId");
        }

        // Find distinct field values that need converting
        final SelectQuery<String> selectQuery = selectDistinctString("entityId")
                        .from("UserHistoryItem")
                        .whereEqual("type", "UsedUser");
        for (Map.Entry<String,String> entry : toUsernameMap(selectQuery).entrySet())
        {
            debugUserUpdate(entry);
            try
            {
                entityEngine.execute(Update
                        .into("UserHistoryItem")
                        .set("entityId", entry.getValue())
                        .whereEqual("type", "UsedUser")
                        .andEqual("entityId", entry.getKey()));
            }
            catch (DataAccessException dae)
            {
                // Very likely due to violation of a uniqueness constraint; proceed under this assumption

                // Loss of user history is no big deal, so just blow the offending rows away.
                entityEngine.delete(Delete.from("UserHistoryItem").whereEqual("entityId", entry.getKey()));
            }
        }
    }

    private void updateCustomFieldValuesForType(final String customFieldTypeKey) throws SQLException
    {
        if (debug)
        {
            log.debug("  Updating custom field values of type '" + customFieldTypeKey + '\'');
        }
        final SelectQuery<GenericValue> selectQuery = Select.columns("id")
                .from("CustomField")
                .whereEqual("customfieldtypekey", customFieldTypeKey);
        final Collection<Long> ids = entityEngine.run(selectQuery).consumeWith(new IdCollector());
        for (Long customFieldId : ids)
        {
            updateCustomField(customFieldId);
        }
    }

    private Select.SelectSingleColumnContext<String> selectDistinctString(String fieldName) throws SQLException
    {
        // MSSQL's case-insensitive collation means we can't use a distinct clause for names differing in case
        return isMSSQL() ? Select.stringColumn(fieldName) : Select.distinctString(fieldName);
    }

    private void updateCustomField(final Long customFieldId) throws SQLException
    {
        if (debug)
        {
            log.debug("   updateCustomField(" + customFieldId + ')');
        }

        final SelectQuery<String> selectQuery = selectDistinctString("stringvalue")
                .from("CustomFieldValue")
                .whereEqual("customfield", customFieldId);
        final Map<String,String> usernameMap = toUsernameMap(selectQuery);
        if (usernameMap.isEmpty())
        {
            return;
        }

        // Custom field values get indexed
        needReindex = true;
        for (Map.Entry<String,String> entry : usernameMap.entrySet())
        {
            debugUserUpdate(entry);
            entityEngine.execute( Update.into("CustomFieldValue")
                    .set("stringvalue", entry.getValue())
                    .whereEqual("customfield", customFieldId)
                    .andEqual("stringvalue", entry.getKey()) );
        }
    }

    protected Map<String,String> toUsernameMap(SelectQuery<String> selectQuery)
    {
        return entityEngine.run(selectQuery).consumeWith(FindMixedCaseUsernames.fromStrings());
    }

    private class IdCollector implements EntityListConsumer<GenericValue, Collection<Long>>
    {
        private final Set<Long> ids = new HashSet<Long>();

        @Override
        public void consume(GenericValue entity)
        {
            ids.add(entity.getLong("id"));
        }

        @Override
        public Collection<Long> result()
        {
            return ids;
        }
    }
}
