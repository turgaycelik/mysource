package com.atlassian.jira.crowd.embedded.ofbiz;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.api.UserComparator;
import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.crowd.model.user.TimestampedUser;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang.BooleanUtils;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.crowd.embedded.ofbiz.UserEntity.ACTIVE;
import static com.atlassian.jira.crowd.embedded.ofbiz.UserEntity.CREATED_DATE;
import static com.atlassian.jira.crowd.embedded.ofbiz.UserEntity.DIRECTORY_ID;
import static com.atlassian.jira.crowd.embedded.ofbiz.UserEntity.DISPLAY_NAME;
import static com.atlassian.jira.crowd.embedded.ofbiz.UserEntity.EMAIL_ADDRESS;
import static com.atlassian.jira.crowd.embedded.ofbiz.UserEntity.EXTERNAL_ID;
import static com.atlassian.jira.crowd.embedded.ofbiz.UserEntity.FIRST_NAME;
import static com.atlassian.jira.crowd.embedded.ofbiz.UserEntity.LAST_NAME;
import static com.atlassian.jira.crowd.embedded.ofbiz.UserEntity.UPDATED_DATE;
import static com.atlassian.jira.crowd.embedded.ofbiz.UserEntity.USER_ID;
import static com.atlassian.jira.crowd.embedded.ofbiz.UserEntity.USER_NAME;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

class OfBizUser implements TimestampedUser, UserOrGroupStub, Serializable
{
    private static final long serialVersionUID = -3085079271696312159L;

    /**
     * The subset of {@link UserEntity} fields that this implementation expects to be available in
     * {@link #from(GenericValue)}
     * <p>
     * This is provided so that only the appropriate subset of fields need be
     * queried when it is known that all of the returned entities will be turned
     * into {@code OfBizUser} objects.
     * </p>
     */
    static final List<String> SUPPORTED_FIELDS = ImmutableList.<String>builder()
            .add(ACTIVE)
            .add(CREATED_DATE)
            .add(DIRECTORY_ID)
            .add(DISPLAY_NAME)
            .add(EMAIL_ADDRESS)
            .add(EXTERNAL_ID)
            .add(FIRST_NAME)
            .add(LAST_NAME)
            .add(UPDATED_DATE)
            .add(USER_ID)
            .add(USER_NAME)
            .build();

    static OfBizUser from(final GenericValue userGenericValue)
    {
        return new OfBizUser(userGenericValue);
    }

    private final long id;
    private final long directoryId;
    private final String name;
    private final boolean active;
    private final Date createdDate;
    private final Date updatedDate;
    private final String emailAddress;
    private final String firstName;
    private final String lastName;
    private final String displayName;
    private final String externalId;

    // Note: These do not require thread-safety guards because they are generated deterministically from immutable
    // data in the event that unsafe publishing results in seeing a stale null / 0 value.
    private String lowerUserName;
    private int hash;

    private OfBizUser(final GenericValue userGenericValue)
    {
        notNull("userGenericValue", userGenericValue);

        id = userGenericValue.getLong(USER_ID);
        directoryId = userGenericValue.getLong(DIRECTORY_ID);
        name = userGenericValue.getString(USER_NAME);
        active = BooleanUtils.toBoolean(userGenericValue.getInteger(ACTIVE));
        createdDate = userGenericValue.getTimestamp(CREATED_DATE);
        updatedDate = userGenericValue.getTimestamp(UPDATED_DATE);
        emailAddress = getEmailAddressFrom(userGenericValue);
        firstName = userGenericValue.getString(FIRST_NAME);
        lastName = userGenericValue.getString(LAST_NAME);
        displayName = userGenericValue.getString(DISPLAY_NAME);
        externalId = userGenericValue.getString(EXTERNAL_ID);
    }

    private String getEmailAddressFrom(final GenericValue userGenericValue)
    {
        // Some brain-dead databases like Oracle store blank values as 'null' so to cater for this we set the email
        // address to blank if the value returned from the database is 'null'
        final String email = userGenericValue.getString(EMAIL_ADDRESS);
        return (email != null) ? email : "";
    }

    public long getId()
    {
        return id;
    }

    public boolean isActive()
    {
        return active;
    }

    public long getDirectoryId()
    {
        return directoryId;
    }

    public String getName()
    {
        return name;
    }

    public String getLowerName()
    {
        String lower = this.lowerUserName;
        if (lower == null)
        {
            lower = IdentifierUtils.toLowerCase(name);
            this.lowerUserName = lower;
        }
        return lower;
    }

    public Date getCreatedDate()
    {
        return createdDate;
    }

    public Date getUpdatedDate()
    {
        return updatedDate;
    }

    public String getEmailAddress()
    {
        return emailAddress;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    @Override
    public String getExternalId()
    {
        return externalId;
    }

    @Override
    public String toString()
    {
        return name + ':' + directoryId;
    }



    // CWD-3905 - These implementations of equals, hashCode, and compareTo are designed to avoid the use of
    //     UserComparator wherever possible so that we don't call IdentifierUtils.toLowerCase() excessively.
    //     If CWD-3905 gets addressed, we can change these back to UserComparator calls.

    @Override
    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }
        if (o instanceof OfBizUser)
        {
            return equalsOfBizUser((OfBizUser) o);
        }
        return o instanceof User && equalsUser((User)o);
    }

    private boolean equalsOfBizUser(@Nonnull OfBizUser other)
    {
        // For OfBizUser, the hash code is cached and likely to mismatch faster than string length and initial letters,
        // so checking it first should usually be a win.
        return directoryId == other.directoryId &&
                hashCode() == other.hashCode() &&
                getLowerName().equals(other.getLowerName());
    }

    private boolean equalsUser(@Nonnull User other)
    {
        // For other user implementations, the hash code will generally be as expensive to calculate as
        // the lowercased username, so we may as well go to the lowercased username directly.
        return directoryId == other.getDirectoryId() &&
                getLowerName().equals(IdentifierUtils.toLowerCase(other.getName()));
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")  // see below
    @Override
    public int hashCode()
    {
        // The hash code is expensive to calculate because it is based on the username in lowercase,
        // so we store a local copy of it.  There is no need for thread-safety concerns because the
        // worst thing that can happen is for a thread to see a stale 0 value and recalculate it.
        int h = hash;
        if (h == 0)
        {
            h = UserComparator.hashCode(this);
            hash = h;
        }
        return h;
    }

    public int compareTo(final User other)
    {
        // Reproducing logic from UserComparator, but with using our cached lowercase username
        final int nameCompare = getLowerName().compareTo(getLowerName(other));
        if (nameCompare != 0)
        {
            return nameCompare;
        }
        final long directoryId1 = this.directoryId;
        final long directoryId2 = other.getDirectoryId();
        return (directoryId1 == directoryId2) ? 0 : ((directoryId1 < directoryId2) ? -1 : 1);
    }

    @Nonnull
    @SuppressWarnings("CastToConcreteClass")  // CWD-3905
    private static String getLowerName(@Nonnull User user)
    {
        if (user instanceof OfBizUser)
        {
            return ((OfBizUser)user).getLowerName();
        }
        return IdentifierUtils.toLowerCase(user.getName());
    }
}
