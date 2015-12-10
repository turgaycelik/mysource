package com.atlassian.jira.upgrade.tasks;

import java.util.Collection;
import java.util.List;

import com.atlassian.crowd.directory.InternalDirectory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.jira.entity.GenericValueFunctions;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.util.Visitor;

import com.google.common.collect.Collections2;

import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

/**
 * Filling in missing external ids for users in internal directory
 *
 * @since v6.1
 */
public class UpgradeTask_Build6151 extends AbstractUpgradeTask
{
    static final String ENTITY = "User";
    static final String EXTERNAL_ID = "externalId";
    static final String USER_NAME = "userName";
    static final String DIRECTORY_ID = "directoryId";

    static class Directory
    {
        static final String ENTITY = "Directory";
        static final String DIRECTORY_ID = "id";
        static final String TYPE = "type";
    }


    public UpgradeTask_Build6151()
    {
        super(false);
    }

    @Override
    public String getBuildNumber()
    {
        return "6151";
    }

    @Override
    public String getShortDescription()
    {
        return "Set unique id for users in internal directory";
    }

    @Override
    public void doUpgrade(final boolean setupMode) throws Exception
    {
        final List<GenericValue> internalDirectories = Select.columns(Directory.DIRECTORY_ID).from(Directory.ENTITY).whereCondition(
                new EntityExpr(Directory.TYPE, EntityOperator.EQUALS, DirectoryType.INTERNAL.toString())).runWith(getEntityEngine()).asList();
        final Collection<Long> internalDirectoryIds = Collections2.transform(internalDirectories, GenericValueFunctions.getLong(Directory.DIRECTORY_ID));

        Select.from(ENTITY)
                .whereCondition(new EntityExpr(DIRECTORY_ID, EntityOperator.IN, internalDirectoryIds))
                .andEqual(EXTERNAL_ID, (String)null).runWith(getEntityEngine()).visitWith(new Visitor<GenericValue>()
        {
            @Override
            public void visit(final GenericValue element)
            {
                try
                {
                    element.set(EXTERNAL_ID, InternalDirectory.generateUniqueIdentifier());
                    element.store();
                }
                catch (GenericEntityException e)
                {
                    throw new RuntimeException(String.format("Unable to store User %s (directory %s)", element.getString(USER_NAME), element.getLong(DIRECTORY_ID)), e);
                }
            }
        });

        UpgradeTask_Build602.flushUserCaches();
    }
}
