package com.atlassian.jira.upgrade.tasks;

import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

/**
 * JRA-26194: usernames in AvatarStore should be stored lower case only. Update the storage.
 *
 * @since v5.1
 */
public class UpgradeTask_Build755 extends AbstractUpgradeTask
{
    private static final Logger LOG = Logger.getLogger(UpgradeTask_Build755.class);

    static final String AVATAR_ENTITY = "Avatar";
    static final String AVATAR_TYPE = "avatarType";
    static final String OWNER = "owner";
    static final String SYSTEM_AVATAR = "systemAvatar";

    static final Integer NOT_SYSTEM = 0;

    private final OfBizDelegator delegator;

    public UpgradeTask_Build755(OfBizDelegator delegator)
    {
        super(false);
        this.delegator = delegator;
    }

    @Override
    public String getBuildNumber()
    {
        return "755";
    }

    @Override
    public String getShortDescription()
    {
        return "JRA-26194: usernames in AvatarStore should be stored lower case only. Update the storage.";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        final List<GenericValue> avatars = delegator.findByAnd(AVATAR_ENTITY, FieldMap.build(SYSTEM_AVATAR, NOT_SYSTEM, AVATAR_TYPE, Avatar.Type.USER.getName()));
        if (avatars == null)
        {
            return;
        }

        LOG.info(String.format("Analysing %d Avatars...", avatars.size()));
        for (GenericValue gv : avatars)
        {
            final String owner = gv.getString(OWNER);
            if (owner != null)
            {
                final String lowercase_owner = IdentifierUtils.toLowerCase(owner);
                if (!owner.equals(lowercase_owner))
                {
                    gv.setString(OWNER, lowercase_owner);
                    try
                    {
                        gv.store();
                    }
                    catch (GenericEntityException e)
                    {
                        throw new DataAccessException(e);
                    }
                }
            }
        }
    }
}
