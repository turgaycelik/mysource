package com.atlassian.jira.avatar;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.util.InjectableComponent;

import javax.annotation.Nullable;
import java.util.NoSuchElementException;

/**
 * Service to manipulate avatars of different types.
 *
 * <p/>
 * The current implementation allows only {@link com.atlassian.jira.avatar.Avatar.Type#ISSUETYPE}
 * and {@link com.atlassian.jira.avatar.Avatar.Type#PROJECT}
 *
 * @since v6.3
 */
@ExperimentalApi
@InjectableComponent
public interface UniversalAvatarsService
{
    /**
     * Provides object dedicated to manipulate avatars of given type.
     * Returns null if there is no TypeAvatarService for such type.
     */
    @Nullable
    TypeAvatarService getAvatars(Avatar.Type type) throws NoSuchElementException;

    /**
     * Provides object dedicated to retrieve links (or image date) to avatars of given type.
     * Returns null if there is no AvatarImageResolver for such type.
     */
    @Nullable
    AvatarImageResolver getImages(Avatar.Type type) throws NoSuchElementException;
}
