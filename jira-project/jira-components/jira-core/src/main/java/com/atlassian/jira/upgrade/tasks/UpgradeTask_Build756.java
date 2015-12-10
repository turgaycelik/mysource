package com.atlassian.jira.upgrade.tasks;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.PrimitiveMap;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.user.OfbizExternalEntityStore;
import com.atlassian.jira.user.util.UserManager;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JRA-26194: usernames in UserPropertyManager should be stored lower case only. Update the storage.
 *
 * @since v5.1
 */
public class UpgradeTask_Build756 extends AbstractUpgradeTask
{
    private static final Logger LOG = Logger.getLogger(UpgradeTask_Build756.class);

    private static final String ENTITY_TYPE = OfbizExternalEntityStore.class.getName();
    private static final String NAME = "name";
    private final OfBizDelegator ofBizDelegator;
    private final UserManager userManager;

    public UpgradeTask_Build756(OfBizDelegator ofBizDelegator, UserManager userManager)
    {
        super(false);
        this.ofBizDelegator = ofBizDelegator;
        this.userManager = userManager;
    }

    @Override
    public String getBuildNumber()
    {
        return "756";
    }

    @Override
    public String getShortDescription()
    {
        return "JRA-26194: usernames in UserPropertyManager should be stored lower case only. Update the storage.";
    }

    @Override
    public void doUpgrade(boolean setupMode)
    {
        final Map<String, Object> externalEntityParameters = new PrimitiveMap.Builder().add("type", ENTITY_TYPE).toMap();
        final List<GenericValue> entities = ofBizDelegator.findByAnd(OfbizExternalEntityStore.ENTITY_NAME_EXTERNAL_ENTITY,
                externalEntityParameters);
        if (entities != null)
        {
            LOG.info(String.format("Analysing %d External Entities...", entities.size()));
            Map<String, GenericValue> nameMap = new HashMap<String, GenericValue>(entities.size());
            List<GenericValue> rowsToDelete = new ArrayList<GenericValue>();

            // Search for duplicates and decide how to deal with them
            for (GenericValue gv : entities)
            {
                final String name = gv.getString(NAME);
                final String lowerName = IdentifierUtils.toLowerCase(name);
                if (!nameMap.containsKey(lowerName))
                {
                    // No duplicate
                    nameMap.put(lowerName, gv);
                }
                else
                {
                    // Duplicate found, we need to pick a winner: see which one is the actual user in embedded crowd
                    final User user = userManager.getUser(name);
                    if (user != null && name.equals(user.getName()))
                    {
                        // This row has the preffered username
                        rowsToDelete.add(nameMap.get(lowerName));
                        nameMap.put(lowerName, gv);
                    }
                    else
                    {
                        // The previous row can be kept
                        rowsToDelete.add(gv);
                    }
                }
            }

            // Delete duplicate names
            for (GenericValue gv : rowsToDelete)
            {
                ofBizDelegator.removeValue(gv);
            }

            // Update rows to lowercase
            for (Map.Entry<String, GenericValue> nameGVEntry : nameMap.entrySet())
            {
                String lowername = nameGVEntry.getKey();
                GenericValue gv = nameGVEntry.getValue();

                // Do we even need to update this row?
                if (!gv.getString(NAME).equals(lowername))
                {
                    // Update to lower-case
                    gv.setString(NAME, lowername);
                    ofBizDelegator.store(gv);
                }
            }
        }
    }
}
