package com.atlassian.jira.upgrade.tasks;

import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.entity.SelectQuery;
import com.atlassian.jira.entity.Update;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.user.ApplicationUserEntity;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

/**
 * Store user properties under ApplicationUser entity for rename user.
 *
 * @since v6.0
 */
public class UpgradeTask_Build6041 extends AbstractUpgradeTask
{
    private final EntityEngine entityEngine;

    public UpgradeTask_Build6041(EntityEngine entityEngine)
    {
        super(false);
        this.entityEngine = entityEngine;
    }

    @Override
    public String getBuildNumber()
    {
        return "6041";
    }

    @Override
    public String getShortDescription()
    {
        return "Store user properties under ApplicationUser entity for rename user.";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        // Find all values in the ExternalEntity table
        final SelectQuery<GenericValue> selectQuery = Select.from(Entity.Name.EXTERNAL_ENTITY).whereEqual("type", "com.atlassian.jira.user.OfbizExternalEntityStore");
        final List<GenericValue> externalEntityUsers = entityEngine.run(selectQuery).asList();

        // Convert the External Entity property to an Application User property
        for (GenericValue externalEntityUser : externalEntityUsers)
        {
            Long oldId = externalEntityUser.getLong("id");
            String username = externalEntityUser.getString("name");
            // this should already be lower case, but lets be cautious
            username = IdentifierUtils.toLowerCase(username);
            ApplicationUserEntity user = Select.from(Entity.APPLICATION_USER).whereEqual("lowerUserName", username).runWith(entityEngine).singleValue();
            // user may be deleted
            if (user != null)
            {
                Long newId = user.getId();
                // Change OSProperty Entries stored for this user.
                Update.into(Entity.Name.OS_PROPERTY_ENTRY)
                        .set("entityName", Entity.APPLICATION_USER.getEntityName())
                        .set("entityId", newId)
                        .whereEqual("entityName", "ExternalEntity")
                        .andEqual("entityId", oldId)
                        .execute(entityEngine);
            }
        }
    }
}
