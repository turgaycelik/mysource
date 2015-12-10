package com.atlassian.jira.avatar;

import java.util.List;

import com.atlassian.jira.exception.DataAccessException;

/**
 * Persistent storage mechanism for {@link AvatarImpl}.
 *
 * @since v4.0
 */
public interface AvatarStore
{
    /**
     * Retrieves the Avatar by id.
     *
     * @param avatarId the avatar's id, must not be null.
     * @return the avatar with the given id or null if it doesn't exist.
     * @throws DataAccessException if there is a back-end storage problem.
     */
    public Avatar getById(Long avatarId) throws DataAccessException;

    /**
     * Retrieves the Avatar by id,
     * ensuring the avatar file is tagged with metadata identifying the image came from JIRA.
     *
     * @param avatarId the avatar's id, must not be null.
     * @return the avatar with the given id or null if it doesn't exist.
     * @throws DataAccessException if there is a back-end storage problem.
     */
    public Avatar getByIdTagged(Long avatarId);

    /**
     * Permanently removes the avatar from the system.
     *
     * @param avatarId the avatar's id, must not be null.
     * @return the avatar with the given id or null if it doesn't exist.
     * @throws DataAccessException if there is a back-end storage problem.
     */
    public boolean delete(Long avatarId) throws DataAccessException;

    /**
     * Updates an avatar's properties to match those in the given avatar. The avatar
     * too change is identified by the id of the given avatar.
     *
     * @param avatar the avatar to update, must not be null.
     * @throws DataAccessException if there is a back-end storage problem.
     */
    public void update(Avatar avatar) throws DataAccessException;

    /**
     * Creates an avatar with the properties of the given avatar.
     *
     * @param avatar the to create, must not be null, must have a null id.
     * @return the avatar with the given id or null if it doesn't exist.
     * @throws DataAccessException if there is a back-end storage problem.
     */
    public Avatar create(Avatar avatar) throws DataAccessException;

    /**
     * Provides a list of all system avatars.
     *
     * @return the system avatars, never null.
     * @throws DataAccessException if there is a back-end database problem.
     * @param type the types of avatar to retrieve
     */
    public List<Avatar> getAllSystemAvatars(final Avatar.Type type) throws DataAccessException;

    /**
     * Provides a list of all avatars that are of the given type which have the given owner.
     *
     * @param type    the desired type of the avatars to retrieve.
     * @param ownerId the id of the owner, matches the type (project id or user key).
     * @return all the avatars that have the given type and owner, never null.
     * @throws DataAccessException if there is a back-end database problem.
     */
    public List<Avatar> getCustomAvatarsForOwner(Avatar.Type type, String ownerId) throws DataAccessException;
}
