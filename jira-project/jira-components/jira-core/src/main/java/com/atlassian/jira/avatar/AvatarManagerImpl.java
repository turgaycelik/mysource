package com.atlassian.jira.avatar;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;

import com.atlassian.core.util.thumbnail.Thumber;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.dbc.Assertions;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import static com.atlassian.jira.avatar.Avatar.Type.PROJECT;
import static com.atlassian.jira.avatar.Avatar.Type.USER;
import static com.atlassian.jira.avatar.AvatarServiceImpl.fromStaleUser;
import static com.atlassian.jira.config.properties.APKeys.JIRA_ANONYMOUS_USER_AVATAR_ID;
import static com.atlassian.jira.config.properties.APKeys.JIRA_DEFAULT_AVATAR_ID;
import static com.atlassian.jira.config.properties.APKeys.JIRA_DEFAULT_USER_AVATAR_ID;
import static com.atlassian.jira.user.util.Users.isAnonymous;

/**
 * Manager for Avatars.
 *
 * @since v4.0
 */
public class AvatarManagerImpl implements AvatarManager
{
    private static final Logger log = Logger.getLogger(AvatarManagerImpl.class);
    private static final String AVATAR_CLASSPATH_PREFIX = "/avatars/";

    private AvatarStore store;
    private JiraHome jiraHome;
    private ApplicationProperties applicationProperties;
    private final PermissionManager permissionManager;
    private ImageScaler scaler;
    private final AvatarTagger avatarTagger;
    private final AvatarImageDataStorage avatarImageDataStorage;

    public AvatarManagerImpl(AvatarStore store, JiraHome jiraHome, ApplicationProperties applicationProperties,
            final PermissionManager permissionManager, AvatarTagger avatarTagger)
    {
        this(store, jiraHome, applicationProperties, permissionManager, avatarTagger, new AvatarImageDataStorage(jiraHome));
    }

    public AvatarManagerImpl(AvatarStore store, JiraHome jiraHome, ApplicationProperties applicationProperties,
            final PermissionManager permissionManager, AvatarTagger avatarTagger, AvatarImageDataStorage avatarImageDataStorage)
    {
        this.store = store;
        this.jiraHome = jiraHome;
        this.applicationProperties = applicationProperties;
        this.permissionManager = permissionManager;
        this.avatarTagger = avatarTagger;
        this.scaler = new ImageScaler();

        this.avatarImageDataStorage = avatarImageDataStorage;
    }

    public Avatar getById(Long avatarId)
    {
        Assertions.notNull("avatarId", avatarId);
        return store.getById(avatarId);
    }

    public Avatar getByIdTagged(Long avatarId)
    {
        Assertions.notNull("avatarId", avatarId);
        return store.getByIdTagged(avatarId);
    }

    public boolean delete(Long avatarId)
    {
        return delete(avatarId, true);
    }

    public boolean delete(Long avatarId, boolean alsoDeleteAvatarFile)
    {
        Assertions.notNull("avatarId", avatarId);
        final Avatar avatar = store.getById(avatarId);
        if (avatar != null)
        {
            if (alsoDeleteAvatarFile)
            {
                for (ImageSize size : ImageSize.values())
                {
                    deleteFile(getAvatarFile(avatar, size.getFilenameFlag()));
                }
            }
            return store.delete(avatarId);
        }
        else
        {
            return false;
        }
    }

    private void deleteFile(File file)
    {
        if (!file.delete())
        {
            file.deleteOnExit();
        }
    }

    public void update(Avatar avatar)
    {
        Assertions.notNull("avatar", avatar);
        Assertions.notNull("avatar.id", avatar.getId());
        store.update(avatar);
    }

    public Avatar create(Avatar avatar)
    {
        Assertions.notNull("avatar", avatar);
        Assertions.stateTrue("avatar.id must be null", avatar.getId() == null);
        return store.create(avatar);
    }

    public Avatar create(final Avatar avatar, final InputStream imageData, final Selection croppingSelection)
            throws DataAccessException, IOException
    {
        Assertions.notNull("avatar", avatar);
        if (avatar.isSystemAvatar())
        {
            throw new IllegalArgumentException("System avatars cannot be created with custom image data");
        }
        Assertions.notNull("imageData", imageData);

        File avatarFile = null;
        try
        {
            final Avatar created = create(avatar);
            final File largeAvatarFile = avatarFile = processImage(created, imageData, croppingSelection, ImageSize.largest());
            for (ImageSize size : ImageSize.values())
            {
                if (ImageSize.largest().equals(size))
                {
                    //already generated this one.
                    continue;
                }
                avatarFile = processImage(created, FileUtils.openInputStream(largeAvatarFile), null, size);
            }
            return created;
        }
        catch (RuntimeException failedCreate)
        {
            handleCreationFailure(avatarFile);
            throw failedCreate;
        }
    }

    @Override
    public Avatar create(Avatar.Type avatarType, String owningObjectId, AvatarImageDataProvider imageDataProvider)
            throws IOException
    {
        Assertions.notNull("avatarType", avatarType);
        Assertions.notNull("owningObjectId", owningObjectId);
        Assertions.notNull("imageDataProvider", imageDataProvider);

        Avatar avatarStub = new AvatarImpl(
                null,
                avatarImageDataStorage.getNextFilenameStub() + ".png",
                "image/png",
                avatarType,
                owningObjectId,
                false);

        final Avatar newAvatar = store.create(avatarStub);
        try
        {
            avatarImageDataStorage.storeAvatarFiles(newAvatar, imageDataProvider);
        }
        catch (Exception exc)
        {
            store.delete(newAvatar.getId());

            if (exc instanceof IOException)
            { throw (IOException) exc; }
            else if (exc instanceof RuntimeException)
            { throw (RuntimeException) exc; }
            else
            { throw new RuntimeException(exc); }
        }

        return newAvatar;
    }

    @Override
    public Avatar create(String fileName, String contentType, Project owner, InputStream imageData, Selection croppingSelection)
            throws DataAccessException, IOException
    {
        Assertions.notNull("fileName", fileName);
        Assertions.notNull("avatar", contentType);
        Assertions.notNull("owner", owner);
        Assertions.notNull("imageData", imageData);

        return create(AvatarImpl.createCustomAvatar(fileName, contentType, owner),
                imageData, croppingSelection);
    }

    @Override
    public Avatar create(String fileName, String contentType, ApplicationUser owner, InputStream imageData, Selection croppingSelection)
            throws DataAccessException, IOException
    {
        Assertions.notNull("fileName", fileName);
        Assertions.notNull("avatar", contentType);
        Assertions.notNull("owner", owner);
        Assertions.notNull("imageData", imageData);

        return create(AvatarImpl.createCustomAvatar(fileName, contentType, owner), imageData, croppingSelection);
    }

    private void handleCreationFailure(final File avatarFile)
    {
        try
        {
            if (avatarFile != null && avatarFile.exists() && !avatarFile.delete())
            {
                log.warn("Created avatar file '" + avatarFile
                        + "' but then failed to store to db. Failed to delete the file!");
            }
        }
        catch (RuntimeException failedDeleteFile)
        {
            log.warn("Created avatar file '" + avatarFile
                    + "' but then failed to store to db. Failed to delete the file!", failedDeleteFile);
        }
    }

    File processImage(final Avatar created, final InputStream imageData, final Selection croppingSelection, final ImageSize size)
            throws IOException
    {
        RenderedImage image = scaler.getSelectedImageData(new Thumber().getImage(imageData), croppingSelection, size.getPixels());
        File file = createAvatarFile(created, size.getFilenameFlag());
        ImageIO.write(image, AVATAR_IMAGE_FORMAT_FULL.getName(), file);
        avatarTagger.saveTaggedAvatar(image, AVATAR_IMAGE_FORMAT_FULL.getName(), file);
        return file;
    }

    File createAvatarFile(Avatar avatar, String flag) throws IOException
    {
        final File base = getAvatarBaseDirectory();
        createDirectoryIfAbsent(base);
        return new File(base, avatar.getId() + "_" + flag + avatar.getFileName());
    }

    public File getAvatarBaseDirectory()
    {
        return new File(jiraHome.getHome(), AvatarImageDataStorage.AVATAR_DIRECTORY);
    }

    private void createDirectoryIfAbsent(final File dir) throws IOException
    {
        if (!dir.exists() && !dir.mkdirs())
        {
            throw new IOException("Avatars directory is absent and I'm unable to create it. '" + dir.getAbsolutePath() + "'");
        }
        if (!dir.isDirectory())
        {
            throw new IllegalStateException("Avatars directory cannot be created due to an existing file. '" + dir.getAbsolutePath() + "'");
        }
    }

    public List<Avatar> getAllSystemAvatars(Avatar.Type type)
    {
        return Lists.newArrayList(Collections2.filter(store.getAllSystemAvatars(type), isUsableAvatarPredicate(type)));
    }

    private Predicate<? super Avatar> isUsableAvatarPredicate(final Avatar.Type type)
    {
        return new Predicate<Avatar>()
        {
            @Override
            public boolean apply(@Nullable final Avatar input)
            {
                if (null == input) { return false; }
                if (Avatar.Type.PROJECT.equals(type))
                { return !Avatar.demotedSystemProjectAvatars.contains(input.getFileName()); }
                return true;
            }
        };
    }

    public List<Avatar> getCustomAvatarsForOwner(final Avatar.Type type, final String ownerId)
    {
        return store.getCustomAvatarsForOwner(type, ownerId);
    }

    public boolean isAvatarOwner(final Avatar avatar, final String ownerId)
    {
        Assertions.notNull("avatar", avatar);
        Assertions.notNull("owner", ownerId);
        return getCustomAvatarsForOwner(avatar.getAvatarType(), ownerId).contains(avatar);
    }

    void processAvatarData(final Avatar avatar, final Consumer<InputStream> dataAccessor, ImageSize size)
            throws IOException
    {
        final InputStream data;
        if (avatar.isSystemAvatar())
        {
            // load from classpath
            String path = AVATAR_CLASSPATH_PREFIX + size.getFilenameFlag() + avatar.getFileName();
            data = getClasspathStream(path);
            if (data == null)
            {
                log.error("System Avatar not found at the following resource path: " + path);
                throw new IOException("File not found");
            }
        }
        else
        {
            final File file = getOrGenerateAvatarFile(avatar, size);
            data = new FileInputStream(file);
        }
        try
        {
            dataAccessor.consume(data);
        }
        finally
        {
            data.close();
        }
    }

    private File getOrGenerateAvatarFile(final Avatar avatar, final ImageSize size) throws IOException
    {
        final File file = getAvatarFile(avatar, size.getFilenameFlag());
        //if this file doesn't exist and we requested something other than the largest size lets
        //try to generate a smaller image.
        if (!file.exists())
        {
            File largeFile = getOrGenerateLargerAvatarFile(avatar, size);
            //generate a smaller image file for the avatar requested and return that!
            if (largeFile != null && largeFile.exists())
            {
                return processImage(avatar, FileUtils.openInputStream(largeFile), null, size);
            }
        }
        return file;
    }

    /**
     * If we don't have a large file to downscale images from, we'll create a new larger file to do so from.
     *
     * @param avatar the avatar to find the largest file we have on hand for.
     * @param sizeToGet the size we're either hoping to find an asset for, or generate an image at.
     * @return either a new avatar image based on the largest file we have to scale from, or the largest file we have on
     * hand, or null.
     */
    private File getOrGenerateLargerAvatarFile(final Avatar avatar, final ImageSize sizeToGet)
    {
        File largestFileOnHand = null;
        File newFile = null;
        ImageSize availableSize = null;

        for (Avatar.Size size : Avatar.Size.inPixelOrder())
        {
            ImageSize imageSize = ImageSize.fromSize(size);
            File avatarFile = getAvatarFile(avatar, imageSize.getFilenameFlag());
            if (avatarFile.exists())
            {
                largestFileOnHand = avatarFile;
                availableSize = imageSize;
            }
            if (null != availableSize && availableSize.getPixels() > sizeToGet.getPixels())
            {
                break;
            }
        }

        try
        {
            // Upscale or downscale the largest file we have on hand (assuming there is one) to the largest size we need.
            if (largestFileOnHand != null)
            {
                newFile = processImage(avatar, FileUtils.openInputStream(largestFileOnHand), null, sizeToGet);
            }
        }
        catch (IOException ioe)
        {
            log.error(String.format("Failed to generate new image for '%s' from image '%s'", sizeToGet, largestFileOnHand), ioe);
        }

        return (newFile != null) ? newFile : ((largestFileOnHand != null) ? largestFileOnHand : null);
    }

    InputStream getClasspathStream(final String path)
    {
        return AvatarManagerImpl.class.getResourceAsStream(path);
    }

    File getAvatarFile(final Avatar avatar, final String sizeFlag)
    {
        final File base = getAvatarBaseDirectory();
        return new File(base, avatar.getId() + "_" + sizeFlag + avatar.getFileName());
    }

    public void readAvatarData(final Avatar avatar, ImageSize size, final Consumer<InputStream> dataAccessor)
            throws IOException
    {
        processAvatarData(avatar, dataAccessor, size);
    }


    public Long getDefaultAvatarId(Avatar.Type ofType)
    {
        Assertions.stateTrue("Can only handle Project & User avatars at this time.",
                Avatar.Type.PROJECT.equals(ofType) || Avatar.Type.USER.equals(ofType) ||
                        Avatar.Type.ISSUETYPE.equals(ofType));

        String defaultAvatarId = null;
        if (Avatar.Type.PROJECT.equals(ofType))
        {
            defaultAvatarId = applicationProperties.getString(JIRA_DEFAULT_AVATAR_ID);
        }
        else if (Avatar.Type.USER.equals(ofType))
        {
            defaultAvatarId = applicationProperties.getString(JIRA_DEFAULT_USER_AVATAR_ID);
        }
        else if (Avatar.Type.ISSUETYPE.equals(ofType))
        {
            defaultAvatarId = applicationProperties.getString(APKeys.JIRA_DEFAULT_ISSUETYPE_AVATAR_ID);
        }
        return defaultAvatarId != null ? Long.valueOf(defaultAvatarId) : null;
    }

    @Override
    public Long getAnonymousAvatarId()
    {
        final String avatarId = applicationProperties.getString(JIRA_ANONYMOUS_USER_AVATAR_ID);
        return avatarId != null ? Long.valueOf(avatarId) : null;
    }

    @Override
    public boolean hasPermissionToView(final User remoteUser, final Avatar.Type type, final String ownerId)
    {
        if (ownerId == null)
        {
            return type != Avatar.Type.PROJECT;
        }
        try
        {
            return type == Avatar.Type.PROJECT
                    ? hasPermissionToView(fromStaleUser(remoteUser), getProjectManager().getProjectObj(Long.parseLong(ownerId)))
                    : hasPermissionToView(fromStaleUser(remoteUser), ApplicationUsers.byKey(ownerId));
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    @Override
    public boolean hasPermissionToView(ApplicationUser remoteUser, Project project)
    {
        //can't edit non-existent project!
        if (project == null)
        {
            return false;
        }
        final boolean isAdmin = permissionManager.hasPermission(Permissions.ADMINISTER, remoteUser);
        final boolean isProjectAdmin = permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, remoteUser);
        final boolean hasBrowseProject = permissionManager.hasPermission(Permissions.BROWSE, project, remoteUser);
        return isAdmin || isProjectAdmin || hasBrowseProject;
    }

    @Override
    public boolean hasPermissionToView(ApplicationUser remoteUser, ApplicationUser user)
    {
        final boolean remoteUserIsSame = user == null ? true : remoteUser != null && user.equals(remoteUser);
        return remoteUserIsSame || permissionManager.hasPermission(Permissions.USE, remoteUser);
    }

    @Override
    public boolean hasPermissionToEdit(final User remoteUser, final Avatar.Type type, final String ownerId)
    {
        if (ownerId == null)
        {
            return false;
        }
        try
        {
            if (type == PROJECT)
            {
                return hasPermissionToEdit(fromStaleUser(remoteUser), getProjectManager().getProjectObj(Long.parseLong(ownerId)));
            }
            else if (type == USER)
            {
                return hasPermissionToEdit(fromStaleUser(remoteUser), ApplicationUsers.byKey(ownerId));
            }
            else {
                // check for other types are removed form this manager into AvatarAccessPolicy
                return true;
            }
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    @Override
    public boolean hasPermissionToEdit(ApplicationUser remoteUser, ApplicationUser owner)
    {
        //only logged in users can modify someone's avatar image!
        if (isAnonymous(remoteUser) || owner == null)
        {
            return false;
        }
        final boolean isAdmin = permissionManager.hasPermission(Permissions.ADMINISTER, remoteUser);
        final boolean isOwner = remoteUser.getKey().equals(owner.getKey());
        return (isAdmin || isOwner);
    }

    @Override
    public boolean hasPermissionToEdit(ApplicationUser remoteUser, Project owner)
    {
        final boolean isAdmin = permissionManager.hasPermission(Permissions.ADMINISTER, remoteUser);
        //can't edit non-existent project!
        if (owner == null)
        {
            return false;
        }
        final boolean isProjectAdmin = permissionManager.hasPermission(Permissions.PROJECT_ADMIN, owner, remoteUser);
        return isAdmin || isProjectAdmin;
    }

    ProjectManager getProjectManager()
    {
        return ComponentAccessor.getProjectManager();
    }
}
