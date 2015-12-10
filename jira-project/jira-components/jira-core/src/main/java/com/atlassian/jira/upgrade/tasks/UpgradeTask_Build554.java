package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarImpl;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.apache.log4j.Logger;

/**
 * Adds user project avatars.
 *
 * @since v4.2
 */
public class UpgradeTask_Build554 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build554.class);

    private AvatarManager avatarManager;
    private ApplicationProperties applicationProperties;

    public UpgradeTask_Build554(AvatarManager avatarManager, ApplicationProperties applicationProperties)
    {
        super(false);
        this.avatarManager = avatarManager;
        this.applicationProperties = applicationProperties;
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        // first check if there are any existing system avatars. and get rid of them just in case!
        for (Avatar avatar : avatarManager.getAllSystemAvatars(Avatar.Type.USER))
        {
            log.info("Deleting existing system avatar");
            avatarManager.delete(avatar.getId());
        }

        // for each system avatar file, add the system avatar
        createAvatar("angel.png");
        createAvatar("businessman.png");
        createAvatar("businessman2.png");
        createAvatar("devil.png");
        createAvatar("doctor.png");
        createAvatar("dude1.png");
        createAvatar("dude2.png");
        createAvatar("dude3.png");
        createAvatar("dude4.png");
        createAvatar("dude5.png");
        createAvatar("ghost.png");
        createAvatar("security_agent.png");
        createAvatar("user1.png");
        createAvatar("user2.png");
        createAvatar("user3.png");
        createAvatar("user_headphones.png");
        Avatar a = createAvatar("userprofile_silhouette.png");

        applicationProperties.setString(APKeys.JIRA_DEFAULT_USER_AVATAR_ID, a.getId().toString());
    }

    private Avatar createAvatar(final String fileName)
    {
        log.info("Creating system user avatar " + fileName);
        return avatarManager.create(AvatarImpl.createSystemAvatar(fileName, "image/png", Avatar.Type.USER));
    }

    @Override
    public String getShortDescription()
    {
        return "Creates system user avatars.";
    }

    @Override
    public String getBuildNumber()
    {
        return "554";
    }
}