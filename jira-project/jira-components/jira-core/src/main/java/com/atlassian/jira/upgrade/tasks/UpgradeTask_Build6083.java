package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarImpl;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds new system project avatars, hides old ones.
 *
 * @since v6.0
 */
public class UpgradeTask_Build6083 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build6083.class);

    private final AvatarManager avatarManager;
    static final String AVATAR_ENTITY = "Avatar";
    static final String SYSTEM_AVATAR_FIELD = "systemAvatar";
    static final Integer IS_SYSTEM = 1;
    static final Integer NOT_SYSTEM = 0;

    private static final List<String> demoteFromSystemProjectAvatars = Lists.newArrayList(
        "codegeist.png",
        "jm_black.png",
        "jm_brown.png",
        "jm_orange.png",
        "jm_red.png",
        "jm_white.png",
        "jm_yellow.png",
        "monster.png"
    );
    private static final List<String> newSystemProjectAvatars = Lists.newArrayList(
            "cloud.png",
            "config.png",
            "disc.png",
            "finance.png",
            "hand.png",
            "new_monster.png",
            "power.png",
            "refresh.png",
            "servicedesk.png",
            "settings.png",
            "storm.png",
            "travel.png"
    );

    public UpgradeTask_Build6083(AvatarManager avatarManager)
    {
        super(false);
        this.avatarManager = avatarManager;
    }

    @Override
    public String getBuildNumber()
    {
        return "6083";
    }

    @Override
    public String getShortDescription()
    {
        return "Adding a few new system project avatars for user selection.";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        // Get the list of all the existing system avatars.
        List<Avatar> existingSystemProjectAvatars = avatarManager.getAllSystemAvatars(Avatar.Type.PROJECT);
        List<String> avatarsToCreate = new ArrayList<String>(newSystemProjectAvatars);

        // Demote some of the existing project avatars from system avatar status.
        for (Avatar avatar : existingSystemProjectAvatars)
        {
/**
 * WARNING: Setting a system avatar back to an unowned avatar has some fairly gnarly consequences, like
 * JIRA refusing to serve the image asset whatsoever. Beware! Best to simply blacklist some avatars you don't want to see.
 * @see {@link Avatar#demotedSystemProjectAvatars}.
 */
//            if (avatar.isSystemAvatar() && demoteFromSystemProjectAvatars.contains(avatar.getFileName()))
//            {
//                setSystemDefaultForAvatar(avatar, false);
//            }

            // Keep track of the new avatars that don't exist in the DB yet
            if (avatarsToCreate.contains(avatar.getFileName()))
            {
                avatarsToCreate.remove(avatar.getFileName());
            }
        }

        // Now create some new system project avatars.
        for (String filename : avatarsToCreate)
        {
            createAvatar(filename);
        }
    }

    private void setSystemDefaultForAvatar(Avatar avatar, boolean isSystemAvatar)
    {
        try
        {
            log.info(String.format("Changing '%s' system avatar status to " + isSystemAvatar, avatar.getFileName()));
            GenericValue gv = getOfBizDelegator().findById(AVATAR_ENTITY, avatar.getId());
            gv.set(SYSTEM_AVATAR_FIELD, isSystemAvatar ? IS_SYSTEM : NOT_SYSTEM);
            gv.store();
        }
        catch (GenericEntityException e)
        {
            log.error(String.format("Error updating '%s' system avatar status", avatar.getFileName()), e);
        }
    }

    private Avatar createAvatar(final String fileName)
    {
        log.info("Creating system project avatar " + fileName);
        return avatarManager.create(AvatarImpl.createSystemAvatar(fileName, "image/png", Avatar.Type.PROJECT));
    }

}
