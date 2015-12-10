package com.atlassian.jira.user.util;

import org.ofbiz.core.entity.GenericEntityException;

/**
 * Util interface providing helper methods for updating global user preferences (such as e-mail Mimetype, etc)
 */
public interface GlobalUserPreferencesUtil
{
    /**
     * Returns a count of all user entries that will have their mail settings changed.
     * This respects external user managment (ie we'll do a lookup in the external entities table)
     *
     * @param mimetype either 'text' or 'html'
     * @return A count of the total number of users affected.
     */
    long getTotalUpdateUserCountMailMimeType(String mimetype);

    /**
     * This is used to update all users mimetype mail preference.
     *
     * @param mimetype The new mimetype to use
     * @throws org.ofbiz.core.entity.GenericEntityException
     *
     */
    void updateUserMailMimetypePreference(String mimetype) throws GenericEntityException;
}
