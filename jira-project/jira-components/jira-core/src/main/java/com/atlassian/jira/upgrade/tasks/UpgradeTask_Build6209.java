package com.atlassian.jira.upgrade.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static electric.util.file.FileUtil.listFiles;
import static org.apache.commons.io.FileUtils.moveFile;
import static org.apache.commons.io.FilenameUtils.removeExtension;

/**
 * JRA-25705 Ensure no avatar file names interfere with our existing tagged avatar naming convention
 *
 * @since v6.1.6, 6.2-OD6
 */
public class UpgradeTask_Build6209 extends AbstractUpgradeTask
{
    private static final String AVATAR_ENTITY = "Avatar";
    private static final String FILE_NAME = "fileName";
    private static final String ID = "id";
    private static final String AVATAR_DIRECTORY = "data/avatars";
    private static final String OWNER = "owner";
    private static final String TAGGED_AVATAR_FILE_SUFFIX = "jrvtg.png";
    public static final String SYSTEM_AVATAR = "systemAvatar";
    public static final String PROJECT_AVATAR_TYPE = "project";
    private final OfBizDelegator ofBizDelegator;
    private final JiraHome jiraHome;
    private static final Logger log = Logger.getLogger(UpgradeTask_Build6209.class);

    public UpgradeTask_Build6209(OfBizDelegator ofBizDelegator, JiraHome jirahome)
    {
        super(false);
        this.ofBizDelegator = ofBizDelegator;
        this.jiraHome = jirahome;
    }

    @Override
    public String getBuildNumber()
    {
        return "6209";
    }

    @Override
    public String getShortDescription()
    {
        return "JRA-25705 Ensure no avatar file names interfere with our existing tagged avatar naming convention";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        ArrayList<Avatar> systemAvatars = new ArrayList<Avatar>();
        final ImmutableMap<String, String> condition = ImmutableMap.of(FILE_NAME, "%" + TAGGED_AVATAR_FILE_SUFFIX);
        for (GenericValue gv : ofBizDelegator.findByLike(AVATAR_ENTITY, condition))
        {
            try
            {
                renameAllVersions(gv);
            }
            catch (IOException e)
            {
                log.warn("An error occurred while upgrading avatar for " + gv.getString(OWNER)
                        + ". Please re-upload your avatar.");
            }
            catch (GenericEntityException e)
            {
                log.warn("An error occurred while upgrading avatar for " + gv.getString(OWNER)
                        + ". Please re-upload your avatar.");
            }
        }
    }

    private void renameAllVersions(GenericValue gv) throws IOException, GenericEntityException
    {
        int isSystem = gv.getInteger(SYSTEM_AVATAR);
        if (isSystem == 1)
        {
            return;
        }

        String avatarType = gv.getString("avatarType");
        if (PROJECT_AVATAR_TYPE.equals(avatarType))
        {
            return;
        }

        long id = gv.getLong(ID);

        String baseFileName = gv.getString(FILE_NAME);
        final String baseFileNameRegex = Pattern.quote(baseFileName) + "$";
        RegexFileFilter forThisAvatar = new RegexFileFilter(id + "_.*" + baseFileNameRegex, IOCase.INSENSITIVE);

        final String newBaseFileName = removeExtension(baseFileName) + "0.png";
        final File avatarBaseDirectory = getAvatarBaseDirectory();

        for (File file : listFiles(avatarBaseDirectory, true, forThisAvatar))
        {
            String newFileName = file.getName().replaceFirst(baseFileNameRegex, newBaseFileName);
            File newFile = new File(avatarBaseDirectory, newFileName);

            // The practice of prepending the id at the front of the avatar filename means a file of the same name
            // should never exist, but just in case...
            if (!newFile.exists())
            {
                moveFile(file, newFile);
            }
        }

        gv.setString(FILE_NAME, newBaseFileName);
        gv.store();

    }

    public File getAvatarBaseDirectory()
    {
        return new File(jiraHome.getHome(), AVATAR_DIRECTORY);
    }
}
