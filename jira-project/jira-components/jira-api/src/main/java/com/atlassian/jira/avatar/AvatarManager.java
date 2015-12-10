package com.atlassian.jira.avatar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.annotation.Nonnull;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.dbc.Assertions;

import org.apache.commons.lang.StringUtils;

/**
 * Manager interface for {@link com.atlassian.jira.avatar.Avatar} domain objects.
 *
 * @since v4.0
 */
@PublicApi
public interface AvatarManager
{
    /**
     * Represents the different sizes of avatars that can be requested!
     * @deprecated in JIRA 6.0 -- use {@link Avatar.Size}.
     */
    public static enum ImageSize {
        NORMAL(Avatar.Size.NORMAL),
        SMALL(Avatar.Size.SMALL),
        MEDIUM(Avatar.Size.MEDIUM),
        LARGE(Avatar.Size.LARGE),
        XLARGE(Avatar.Size.XLARGE),
        XXLARGE(Avatar.Size.XXLARGE),
        XXXLARGE(Avatar.Size.XXXLARGE),
        // The order is important -- the @2x need to appear after the normal sizes, lest they be substituted incorrectly.
        RETINA_XXLARGE(Avatar.Size.RETINA_XXLARGE),
        RETINA_XXXLARGE(Avatar.Size.RETINA_XXXLARGE);

        private final Avatar.Size size;
        private final String filenameFlag;
        private final Selection originSelection;

        private final static ImageSize largest;
        private final static ImageSize defaultSize;

        static
        {
            ImageSize maxValue = SMALL;
            ImageSize defaultValue = LARGE;
            for (ImageSize imageSize : values())
            {
                if (imageSize.size.isDefault)
                {
                    defaultValue = imageSize;
                }
                if (imageSize.getPixels() > maxValue.getPixels())
                {
                    maxValue = imageSize;
                }
            }
            largest = maxValue;
            defaultSize = defaultValue;
        }

        ImageSize(Avatar.Size size)
        {
            this.size = size;
            this.filenameFlag = Assertions.notNull("filenameFlag", (size.isDefault ? "" : size.param + "_"));
            this.originSelection = new Selection(0, 0, getPixels(), getPixels());
        }

        public static ImageSize fromSize(Avatar.Size size)
        {
            for(ImageSize imageSize : values())
            {
                if (imageSize.getSize().equals(size)) return imageSize;
            }
            throw new IllegalArgumentException("There should be a matching ImageSize for every Avatar.Size, but there wasn't for '" + size + "'.");
        }

        public Avatar.Size getSize()
        {
            return size;
        }

        public int getPixels()
        {
            return size.getPixels();
        }

        public String getFilenameFlag()
        {
            return filenameFlag;
        }

        public Selection getOriginSelection()
        {
            return originSelection;
        }

        /**
         * In order to cater for future addition of larger sizes this method finds the largest image size.
         * @return The largest ImageSize
         * @see {@link com.atlassian.jira.avatar.Avatar.Size#largest()}, they need to be kept in sync.
         */
        public static ImageSize largest()
        {
            return largest;
        }

        /**
         * @return the default size for avatars.
         * @see {@link com.atlassian.jira.avatar.Avatar.Size#defaultSize()}, they need to be kept in sync.
         */
        public static ImageSize defaultSize()
        {
            return defaultSize;
        }

        /**
         * Returns an avatar image size matching the text provided.
         * If none can be found, returns {@link #defaultSize}.
         * @param text the images size. Will match "s", "small", "SMALL". Can also be an integer value (16, 24, etc.)
         * @return the image size enum matching the string provided
         */
        public static ImageSize fromString(String text)
        {
            if (StringUtils.isNotBlank(text))
            {
                int size = -1;
                try
                {
                    size = Integer.parseInt(text);
                }
                catch (NumberFormatException nfe)
                {
                    // It's not a number. That's okay!
                }

                for (ImageSize value : values())
                {
                    if( (size > 0 && size == value.getPixels())
                            || StringUtils.startsWithIgnoreCase(value.name(), text)
                            || StringUtils.startsWithIgnoreCase(value.getFilenameFlag(), text))
                    {
                        return value;
                    }
                }
            }
            //fallback to default size if none could be found.
            return defaultSize();
        }

        @Override
        public String toString()
        {
            return size.toString();
        }
    }

    public final String AVATAR_IMAGE_FORMAT = "PNG";

    public final AvatarFormat AVATAR_IMAGE_FORMAT_FULL = new AvatarFormat(AVATAR_IMAGE_FORMAT, "image/png");

    public static final String USER_AVATAR_ID_KEY = "user.avatar.id";

    /**
     * Retrieve the avatar with the given id.
     *
     * @param avatarId must not be null.
     * @return the Avatar if there is one or null if not.
     * @throws DataAccessException if there is a back-end database problem.
     */
    Avatar getById(Long avatarId) throws DataAccessException;

    /**
     * Retrieve the avatar with the given id,
     * ensuring the avatar file is tagged with metadata identifying the image came from JIRA.
     *
     * @param avatarId must not be null.
     * @return the Avatar if there is one or null if not.
     * @throws DataAccessException if there is a back-end database problem.
     */
    Avatar getByIdTagged(Long avatarId) throws DataAccessException;

    /**
     * Delete the avatar with the given id and the file on disk.
     *
     * @param avatarId must not be null.
     * @return true only if there was an avatar with the given id which was deleted.
     * @throws DataAccessException if there is a back-end database problem.
     */
    boolean delete(Long avatarId) throws DataAccessException;

    /**
     * Delete the avatar with the given id.
     *
     * @param avatarId must not be null.
     * @param alsoDeleteAvatarFile if false, the avatar file will be left on disk.
     * @return true only if there was an avatar with the given id which was deleted.
     * @throws DataAccessException if there is a back-end database problem.
     */
    boolean delete(Long avatarId, boolean alsoDeleteAvatarFile);

    /**
     * Saves the avatar as an updated version of the avatar with the same id that is already in the database.
     *
     * @param avatar must not be null.
     * @throws DataAccessException if there is a back-end database problem.
     */
    void update(Avatar avatar) throws DataAccessException;

    /**
     * Creates a database record for the given avatar. Use the return value as the persistent avatar, not the one you
     * passed in.
     *
     * @param avatar must not be null, must have a null id.
     * @return the created avatar which has an assigned id.
     * @throws DataAccessException if there is a back-end database problem.
     */
    @Nonnull
    Avatar create(Avatar avatar) throws DataAccessException;

    /**
     * Creates a database record for the given avatar and uses the content of the InputStream as the image. Use the
     * return value as the persistent avatar, not the one you passed in.
     *
     * @param avatar must not be null, must have a null id.
     * @param image the data of the original avatar image.
     * @param selection the cropping selection for the image or null to take whole image.
     * @return the created avatar which has an assigned id.
     * @throws DataAccessException if there is a back-end database problem.
     * @since v6.3
     */
    @Nonnull
    Avatar create(Avatar avatar, InputStream image, Selection selection) throws DataAccessException, IOException;

    /**
     * Creates a database record for the given avatar and uses the content of the InputStream as the image. Use the
     * return value as the persistent avatar, not the one you passed in.
     *
     * @param avatarType type of new avatar to create.
     * @param owningObjectId id of object (project/user/issuetype) to which this avatar is connected to.
     * @param imageDataProvider provider of image data
     * @return the created avatar which has an assigned id.
     *
     * @see com.atlassian.jira.avatar.CroppingAvatarImageDataProviderFactory
     * @throws DataAccessException if there is a back-end database problem.
     * @throws java.io.IOException if there are arrors when processing image data
     * @since v6.3
     */
    @Nonnull
    Avatar create(Avatar.Type avatarType, @Nonnull String owningObjectId, @Nonnull AvatarImageDataProvider imageDataProvider)
            throws IOException;

    /**
     * Creates a database record for the given avatar and uses the content of the InputStream as the image. Use the
     * return value as the persistent avatar, not the one you passed in.
     *
     * @param fileName the name of the avatar
     * @param contentType MIME content type for the avatar
     * @param image the data of the original avatar image (it will be automatically closed)
     * @param selection the cropping selection for the image or null to take whole image
     * @return the created avatar which has an assigned id
     * @throws DataAccessException if there is a back-end database problem.
     */
    @Nonnull
    Avatar create(String fileName, String contentType, Project owner, InputStream image, Selection selection) throws DataAccessException, IOException;

    /**
     * Creates a database record for the given avatar and uses the content of the InputStream as the image. Use the
     * return value as the persistent avatar, not the one you passed in.
     *
     * @param fileName the name of the avatar
     * @param contentType MIME content type for the avatar
     * @param image the data of the original avatar image (it will be automatically closed)
     * @param selection the cropping selection for the image or null to take whole image
     * @return the created avatar which has an assigned id
     * @throws DataAccessException if there is a back-end database problem.
     */
    @Nonnull
    Avatar create(String fileName, String contentType, ApplicationUser owner, InputStream image, Selection selection) throws DataAccessException, IOException;

    /**
     * Provides a list of all system avatars.
     *
     * @param type The type of system avatars to return
     * @return the system avatars.
     * @throws DataAccessException if there is a back-end database problem.
     */
    @Nonnull
    List<Avatar> getAllSystemAvatars(Avatar.Type type) throws DataAccessException;

    /**
     * Provides a list of all avatars that are of the given type which have the given owner.
     *
     * @param type the desired type of the avatars to retrieve.
     * @param ownerId the id of the owner, matches the type (project id or user key).
     * @return all the avatars that have the given type and owner, never null.
     * @throws DataAccessException if there is a back-end database problem.
     */
    @Nonnull
    List<Avatar> getCustomAvatarsForOwner(Avatar.Type type, String ownerId) throws DataAccessException;

    /**
     * Tells whether the given avatar is owned by the given ownerId.
     *
     * @param avatar the avatar to check, must not be null.
     * @param ownerId the ownerId to check, must not be null.
     * @return true only if the given ownerId is the ownerId of the given avatar.
     * @deprecated To be removed. Since v6.0
     */
    boolean isAvatarOwner(Avatar avatar, String ownerId);

    /**
     * Provides read-only access to the data of the avatar image as an {@link java.io.InputStream} passed to the
     * provided dataProcessor. The InputStream is closed after the dataProcessor completes. The dataProcessor is
     * immediately invoked on the data for the avatar.
     *
     * @param avatar the avatar for which the data is to be processed.
     * @param size the size to return for this avatar
     * @param dataAccessor something to read the data.
     * @throws IOException if an IOException occurs in the dataProcessor or in acquiring the InputStream for the
     * avatar.
     */
    void readAvatarData(final Avatar avatar, final ImageSize size, final Consumer<InputStream> dataAccessor) throws IOException;

    /**
     * Returns the directory for storing avatars.
     *
     * @return the directory.
     */
    @Nonnull
    File getAvatarBaseDirectory();

    /**
     * Gets the default avatar for the given type.
     *
     * @param ofType the Avatar type.
     * @return the default Avatar.
     */
    Long getDefaultAvatarId(Avatar.Type ofType);

    /**
     * Gets the avatar id to use to represent an unknown or anonymous user
     * @return the avatar id for an anonymous user
     */
    Long getAnonymousAvatarId();

    /**
     * Determines if the remoteUser provided has permission to view avatars of a certain type for the owner provided.
     * For project avatars, this method checks that the user is either and admin, project admin or has browse permission
     * for the owner project.  For user avatars, the method checks that the remoteUser has use permission for JIRA or
     * the remoteUser and avatar owner are the same person
     *
     * @param remoteUser The remote user trying to view an avatar
     * @param type The type of avatar
     * @param ownerId The owner id of the avatar being viewed (project id or user key)
     * @return true if the remote user has permission to view avatars owned by the owner provided.
     * @deprecated Use {@link #hasPermissionToView(com.atlassian.jira.user.ApplicationUser, ApplicationUser)} or {@link #hasPermissionToView(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.project.Project)}. Since v6.0
     */
    boolean hasPermissionToView(final User remoteUser, final Avatar.Type type, final String ownerId);

    /**
     * Determines if the remoteUser provided has permission to view avatars of a certain type for the owner provided.
     * For project avatars, this method checks that the user is either and admin, project admin or has browse permission
     * for the owner project.  For user avatars, the method checks that the remoteUser has use permission for JIRA or
     * the remoteUser and avatar owner are the same person
     *
     * @param remoteUser The remote user trying to view an avatar
     * @param owner The owner of the avatar being viewed
     * @return true if the remote user has permission to view avatars owned by the owner provided.
     * @since v6.0
     */
    boolean hasPermissionToView(final ApplicationUser remoteUser, final ApplicationUser owner);

    /**
     * Determines if the remoteUser provided has permission to view avatars of a certain type for the owner provided.
     * For project avatars, this method checks that the user is either and admin, project admin or has browse permission
     * for the owner project.  For user avatars, the method checks that the remoteUser has use permission for JIRA or
     * the remoteUser and avatar owner are the same person
     *
     * @param remoteUser The remote user trying to view an avatar
     * @param owner The owner of the avatar being viewed
     * @return true if the remote user has permission to view avatars owned by the owner provided.
     * @since v6.0
     */
    boolean hasPermissionToView(final ApplicationUser remoteUser, final Project owner);

    /**
     * Determines if the remoteUser provided has permission to edit avatars of a certain type for the owner provided.
     * For project avatars, this method checks that the user is either and admin or project admin for the owner project.
     * For user avatars, the method checks that the remoteUser has admin permissions for JIRA or the remoteUser and
     * avatar owner are the same person.  If external user management is enabled this method returns false
     *
     * @param remoteUser The remote user trying to edit an avatar
     * @param type The type of avatar
     * @param ownerId The owner id of the avatar being edited (project id or user key)
     * @return true if the remote user has permission to edit avatars owned by the owner provided.
     * @deprecated Use {@link #hasPermissionToEdit(ApplicationUser, ApplicationUser)} or {@link #hasPermissionToEdit(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.project.Project)}. Since v6.0
     */
    boolean hasPermissionToEdit(final User remoteUser, final Avatar.Type type, final String ownerId);

    /**
     * Determines if the remoteUser provided has permission to edit avatars of a certain type for the owner provided.
     * For project avatars, this method checks that the user is either and admin or project admin for the owner project.
     * For user avatars, the method checks that the remoteUser has admin permissions for JIRA or the remoteUser and
     * avatar owner are the same person.  If external user management is enabled this method returns false
     *
     * @param remoteUser The remote user trying to edit an avatar
     * @param owner The owner of the avatar being edited
     * @return true if the remote user has permission to edit avatars owned by the owner provided.
     * @since v6.0
     */
    boolean hasPermissionToEdit(final ApplicationUser remoteUser, final ApplicationUser owner);

    /**
     * Determines if the remoteUser provided has permission to edit avatars of a certain type for the owner provided.
     * For project avatars, this method checks that the user is either and admin or project admin for the owner project.
     * For user avatars, the method checks that the remoteUser has admin permissions for JIRA or the remoteUser and
     * avatar owner are the same person.  If external user management is enabled this method returns false
     *
     * @param remoteUser The remote user trying to edit an avatar
     * @param owner The owner of the avatar being edited
     * @return true if the remote user has permission to edit avatars owned by the owner provided.
     * @since v6.0
     */
    boolean hasPermissionToEdit(final ApplicationUser remoteUser, final Project owner);
}
