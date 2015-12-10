package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarImpl;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.propertyset.CachingOfBizPropertyEntryStore;
import com.atlassian.jira.propertyset.OfBizPropertyEntryStore;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Updates user avatars to the latest crowdsourced designs
 *
 * @since v4.3
 */
public class UpgradeTask_Build608 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build608.class);

    private AvatarManager avatarManager;
    private ApplicationProperties applicationProperties;
    private final OfBizDelegator ofBizDelegator;
    private final UserPropertyManager userPropertyManager;

    public UpgradeTask_Build608(final AvatarManager avatarManager, final ApplicationProperties applicationProperties,
            final OfBizDelegator ofBizDelegator, final UserPropertyManager userPropertyManager)
    {
        super(false);
        this.avatarManager = avatarManager;
        this.applicationProperties = applicationProperties;
        this.ofBizDelegator = ofBizDelegator;
        this.userPropertyManager = userPropertyManager;
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        // first check if there are any existing system avatars. and get rid of them just in case!
        final List<Long> systemAvatarIds = new ArrayList<Long>();
        for (Avatar avatar : avatarManager.getAllSystemAvatars(Avatar.Type.USER))
        {
            systemAvatarIds.add(avatar.getId());
        }

        // for each system avatar file, add the system avatar
        for (int i = 1; i <= 22; i++)
        {
            createAvatar("Avatar-" + i + ".png");
        }
        final Avatar defaultAvatar = createAvatar("Avatar-default.png");
        applicationProperties.setString(APKeys.JIRA_DEFAULT_USER_AVATAR_ID, defaultAvatar.getId().toString());

        final Avatar anonymousAvatar = createAvatar("Avatar-unknown.png");
        applicationProperties.setString(APKeys.JIRA_ANONYMOUS_USER_AVATAR_ID, anonymousAvatar.getId().toString());

        //now go through all the user profiles in JIRA that were using defaultAvatar system avatar previously and
        //set them to be the new default avatar
        final EntityCondition entityCondition = new EntityConditionList(Arrays.asList(new EntityExpr("propertyKey", EntityOperator.EQUALS, AvatarManager.USER_AVATAR_ID_KEY),
                new EntityExpr("propertyValue", EntityOperator.IN, systemAvatarIds)), EntityOperator.AND);
        final List<GenericValue> propertiesToUpdate = ofBizDelegator.findByCondition("OSUserPropertySetNumberView", entityCondition, CollectionBuilder.list("id"));
        final List<Long> primaryKeys = new ArrayList<Long>();
        for (GenericValue genericValue : propertiesToUpdate)
        {
            primaryKeys.add(genericValue.getLong("id"));
        }
        
        ofBizDelegator.bulkUpdateByPrimaryKey("OSPropertyNumber", MapBuilder.singletonMap("value", defaultAvatar.getId()), primaryKeys);

        //finally delete the system avatars.  Doing this here rather than at the start guarantees that this is idempotent.
        for (Long systemAvatarId : systemAvatarIds)
        {
            avatarManager.delete(systemAvatarId);
        }

        //clear user property caches!
        OfBizPropertyEntryStore store = ComponentAccessor.getComponent(OfBizPropertyEntryStore.class);
        if (store instanceof CachingOfBizPropertyEntryStore)
        {
            ((CachingOfBizPropertyEntryStore)store).onClearCache(null);
        }
        else
        {
            final String name = (store != null) ? store.getClass().getName() : "null";
            log.error("Expected to find a CachingOfBizPropertyEntryStore, but got " + name);
        }
    }

    private Avatar createAvatar(final String fileName)
    {
        log.info("Creating system user avatar " + fileName);
        return avatarManager.create(AvatarImpl.createSystemAvatar(fileName, "image/png", Avatar.Type.USER));
    }

    @Override
    public String getShortDescription()
    {
        return "Updating system user avatars.";
    }

    @Override
    public String getBuildNumber()
    {
        return "608";
    }
}