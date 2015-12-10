package com.atlassian.jira.crowd.embedded.ofbiz;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;

import org.ofbiz.core.entity.GenericValue;

class UserEntity
{
    static final String ENTITY = "User";

    static final String USER_ID = "id";
    static final String USER_NAME = "userName";
    static final String DIRECTORY_ID = "directoryId";
    static final String LOWER_USER_NAME = "lowerUserName";
    static final String ACTIVE = "active";
    static final String CREDENTIAL = "credential";
    static final String FIRST_NAME = "firstName";
    static final String LOWER_FIRST_NAME = "lowerFirstName";
    static final String LAST_NAME = "lastName";
    static final String LOWER_LAST_NAME = "lowerLastName";
    static final String DISPLAY_NAME = "displayName";
    static final String LOWER_DISPLAY_NAME = "lowerDisplayName";
    static final String EMAIL_ADDRESS = "emailAddress";
    static final String LOWER_EMAIL_ADDRESS = "lowerEmailAddress";
    static final String CREATED_DATE = "createdDate";
    static final String UPDATED_DATE = "updatedDate";
    static final String DELETED_EXTERNALLY = "deletedExternally";
    static final String EXTERNAL_ID = "externalId";

    /**
     * Map of all searchable fields along with their lowercase sibling field, if it is present.
     * <p>
     * Present and non-null means that the field is searched case-insensitively against the map value.
     * Present and null means that the field is not a string value.
     * Not present means that it isn't a field and it gets searched for in the attributes, instead.
     * </p>
     */
    static final Map<String,String> FIELD_NAME_TRANSLATION;
    static
    {
        final Map<String,String> builder = new HashMap<String,String>(16);

        // Our names for things
        builder.put(USER_NAME, LOWER_USER_NAME);
        builder.put(LOWER_USER_NAME, LOWER_USER_NAME);
        builder.put(EMAIL_ADDRESS, LOWER_EMAIL_ADDRESS);
        builder.put(LOWER_EMAIL_ADDRESS, LOWER_EMAIL_ADDRESS);
        builder.put(FIRST_NAME, LOWER_FIRST_NAME);
        builder.put(LOWER_FIRST_NAME, LOWER_FIRST_NAME);
        builder.put(LAST_NAME, LOWER_LAST_NAME);
        builder.put(LOWER_LAST_NAME, LOWER_LAST_NAME);
        builder.put(DISPLAY_NAME, LOWER_DISPLAY_NAME);
        builder.put(LOWER_DISPLAY_NAME, LOWER_DISPLAY_NAME);
        builder.put(CREATED_DATE, null);
        builder.put(UPDATED_DATE, null);
        builder.put(ACTIVE, null);

        // Crowd's names for things, where they don't match our own
        builder.put(UserTermKeys.USERNAME.getPropertyName(), LOWER_USER_NAME);
        builder.put(UserTermKeys.EMAIL.getPropertyName(), LOWER_EMAIL_ADDRESS);
        FIELD_NAME_TRANSLATION = Collections.unmodifiableMap(builder);
    }

    private UserEntity() {}

    static Map<String, Object> getData(final User user, final PasswordCredential credential)
    {
        PrimitiveMap.Builder data = getUserDetails(user);
        if (credential != null)
        {
            data.put(CREDENTIAL, credential.getCredential());
        }
        return data.build();
    }

    static Map<String, Object> getData(final User user)
    {
        return getUserDetails(user).build();
    }

    static Map<String, Object> getData(final User user, final PasswordCredential credential, final Timestamp updatedDate, final Timestamp createdDate)
    {
        PrimitiveMap.Builder data = getUserDetails(user);
        if (credential != null)
        {
            data.put(CREDENTIAL, credential.getCredential());
        }
        if (updatedDate != null)
        {
            data.put(UPDATED_DATE, updatedDate);
        }
        if (createdDate != null)
        {
            data.put(CREATED_DATE, createdDate);
        }
        return data.build();
    }

    private static PrimitiveMap.Builder getUserDetails(User user)
    {
        final PrimitiveMap.Builder data = PrimitiveMap.builder();
        data.put(USER_NAME, user.getName());
        data.put(DIRECTORY_ID, user.getDirectoryId());
        data.putCaseInsensitive(LOWER_USER_NAME, user.getName());
        data.put(ACTIVE, user.isActive());
        data.put(FIRST_NAME, user.getFirstName());
        data.putCaseInsensitive(LOWER_FIRST_NAME, user.getFirstName());
        data.put(LAST_NAME, user.getLastName());
        data.putCaseInsensitive(LOWER_LAST_NAME, user.getLastName());
        data.put(DISPLAY_NAME, user.getDisplayName());
        data.putCaseInsensitive(LOWER_DISPLAY_NAME, user.getDisplayName());
        data.put(EMAIL_ADDRESS, user.getEmailAddress());
        data.putCaseInsensitive(LOWER_EMAIL_ADDRESS, user.getEmailAddress());
        data.put(EXTERNAL_ID, user.getExternalId());
        return data;
    }

    static GenericValue setData(final User user, final GenericValue userGenericValue)
    {
        userGenericValue.setFields(getData(user));
        return userGenericValue;
    }

    /**
     * Return the name of the sibling lower case field of the supplied field.
     * Lower case fields return themselves as their own sibling.
     * @param fieldName Field name to search for sibling of.
     * @return name of lower case sibling.
     */
    static String getLowercaseFieldNameFor(final String fieldName)
    {
        return FIELD_NAME_TRANSLATION.get(fieldName);
    }

    /**
     * Returns true if the field name passed in is a first class field member of this entity.
     * That is it is a field rather than a secondary attribute, that may be stored elsewhere.
     * @param fieldName Field name to search
     * @return true if the field name passed in is a first class field member of this entity.
     */
    static boolean isSystemField(final String fieldName)
    {
        return FIELD_NAME_TRANSLATION.containsKey(fieldName);
    }
}
