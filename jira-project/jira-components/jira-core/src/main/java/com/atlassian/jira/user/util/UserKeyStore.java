package com.atlassian.jira.user.util;

import com.atlassian.annotations.Internal;

/**
 * @since v6.0
 */
@Internal
public interface UserKeyStore
{
    String getUsernameForKey(String key);

    String getKeyForUsername(String username);

    @Internal
    void renameUser(String oldUsername, String newUsername);

    @Internal
    String ensureUniqueKeyForNewUser(String username);

    @Internal
    Long getIdForUserKey(String name);

    @Internal
    String removeByKey(String key);
}
