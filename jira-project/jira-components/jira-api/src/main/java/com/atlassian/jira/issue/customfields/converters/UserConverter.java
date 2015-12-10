package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.annotations.Internal;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Converts between User objects and Strings for storage and retrieval of Custom Field values.
 */
@Internal
public interface UserConverter
{
    /**
     * Get the String representation of the User.
     * @param user the User
     * @return the String representation of the User
     * @deprecated Use {@link #getHttpParameterValue(ApplicationUser)} or {@link #getDbString(ApplicationUser)} instead. Since v6.0.
     */
    public String getString(User user);

    /**
     * Get the String representation of the User to be passed to and from the presentation tier as an HTTP parameter.
     * @param user the User
     * @return the String representation of the User to be passed to and from the presentation tier as an HTTP parameter.
     * @since v6.0
     */
    public String getHttpParameterValue(ApplicationUser user);

    /**
     * Get the String representation of the User to be passed to and from the database tier.
     * @param user the User
     * @return the String representation of the User to be passed to and from the database tier.
     * @since v6.0
     */
    public String getDbString(ApplicationUser user);

    /**
     * Get the User Object from the user name.
     * This will return null if the stringValue is empty.
     * @param stringValue User name
     * @return A User or null if the input parameter is empty
     * @throws FieldValidationException if the input parameter is null
     * @deprecated Use {@link #getUserFromDbString(String)} instead. Since v6.0.
     */
    public User getUser(String stringValue) throws FieldValidationException;

    /**
     * Get the User Object from the user name even when the user is unknown.
     * This is usefull in places where the user needs to be shown, even though they may have disappeared remotely, say from LDAP.
     * This will return null if the stringValue is empty.
     * @param stringValue User name
     * @return A User or null if the input parameter is empty
     * @throws FieldValidationException if the input parameter is null
     * @since v4.4.5
     * @deprecated Use {@link #getUserFromDbString(String)} instead. Since v6.0.
     */
    public User getUserEvenWhenUnknown(String stringValue) throws FieldValidationException;

    /**
     * Get the User Object from its presentation string representation.
     * This will return null if the stringValue is empty.
     *
     * @param stringValue presentation string representation (username)
     * @return An ApplicationUser (or null if the input parameter is empty)
     * @throws FieldValidationException if no user exists with the given username
     * @since v6.0
     * @see {@link #getHttpParameterValue(ApplicationUser)}
     */
    public ApplicationUser getUserFromHttpParameterWithValidation(String stringValue) throws FieldValidationException;

    /**
     * Get the User Object from its database string representation.
     * If a null stringValue is passed, then a null User object is returned, but it is guaranteed to return a non-null
     * User in all other cases. This is usefull in places where the user needs to be shown, even though they may have
     * disappeared remotely, say from LDAP.
     * @param stringValue database string representation
     * @return A User or null if the input parameter is empty
     * @since v6.0
     * @see {@link #getDbString(ApplicationUser)}
     */
    public ApplicationUser getUserFromDbString(String stringValue);

    /**
     * Get the User Object from the user name.
     * This will return null if the stringValue is empty.
     * @param stringValue User name
     * @return A User or null if the input parameter is empty
     * @throws FieldValidationException if the input parameter is null
     * @deprecated Use {@link #getUser(String)} instead. Since v5.0.
     */
    public User getUserObject(String stringValue) throws FieldValidationException;

}
