package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.EntityListConsumer;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.entity.Update;
import com.atlassian.jira.upgrade.UpgradeTask;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;

/**
 * Populate the new column in SearchRequest for lower-case name.
 *
 * @since v5.2
 */
public class UpgradeTask_Build802 implements UpgradeTask
{
    private final EntityEngine entityEngine;

    public UpgradeTask_Build802(EntityEngine entityEngine)
    {
        this.entityEngine = entityEngine;
    }

    @Override
    public String getBuildNumber()
    {
        return "802";
    }

    @Override
    public String getShortDescription()
    {
        return "Populate the new 'filternameLower' column in SearchRequest.";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        entityEngine.run(Select.columns("id", "name").from("SearchRequest")).consumeWith(new LowerCaseConsumer());
    }

    @Override
    public Collection<String> getErrors()
    {
        return Collections.emptyList();
    }

    @Override
    public boolean isReindexRequired()
    {
        return false;
    }

    private class LowerCaseConsumer implements EntityListConsumer<GenericValue,Object>
    {
        @Override
        public void consume(GenericValue entity)
        {
            Long id = entity.getLong("id");
            String name = entity.getString("name");
            // hopefully we won't need this ...
            if (name == null)
                name = "";
            entityEngine.execute(Update.into("SearchRequest").set("nameLower", name.toLowerCase()).whereEqual("id", id));
        }

        @Override
        public Object result()
        {
            return null;
        }
    }
}
