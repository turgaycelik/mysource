package com.atlassian.jira.sharing;

import com.atlassian.jira.sharing.type.ShareType;

/**
 * Represents a JIRA share from the database.
 * 
 * @since v3.13
 */
public interface SharePermission
{
    /**
     * The identifier of the share. Can be null if the share has not been stored.
     * 
     * @return the identifier of the share.
     */
    Long getId();

    /**
     * The type of the share (e.g. group). Should never be null.
     * 
     * @return the type of the share.
     */
    ShareType.Name getType();

    /**
     * The first parameter of the permission. Its value can be null.
     * 
     * @return the value of the permissions first parameter.
     */
    String getParam1();

    /**
     * The second parameter of the permission. Its value can be null. This parameter cannot have a value when {#getParam1} returns null.
     * 
     * @return the value of the permissions second parameter.
     */
    String getParam2();
}
