package com.atlassian.jira.crowd.embedded.ofbiz;

import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupComparator;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.group.InternalDirectoryGroup;
import com.atlassian.jira.util.dbc.Assertions;

import org.apache.commons.lang.BooleanUtils;
import org.ofbiz.core.entity.GenericValue;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import static com.atlassian.jira.crowd.embedded.ofbiz.GroupEntity.ACTIVE;
import static com.atlassian.jira.crowd.embedded.ofbiz.GroupEntity.LOCAL;
import static com.atlassian.jira.crowd.embedded.ofbiz.GroupEntity.CREATED_DATE;
import static com.atlassian.jira.crowd.embedded.ofbiz.GroupEntity.DESCRIPTION;
import static com.atlassian.jira.crowd.embedded.ofbiz.GroupEntity.DIRECTORY_ID;
import static com.atlassian.jira.crowd.embedded.ofbiz.GroupEntity.ID;
import static com.atlassian.jira.crowd.embedded.ofbiz.GroupEntity.NAME;
import static com.atlassian.jira.crowd.embedded.ofbiz.GroupEntity.TYPE;
import static com.atlassian.jira.crowd.embedded.ofbiz.GroupEntity.UPDATED_DATE;

class OfBizGroup implements InternalDirectoryGroup, UserOrGroupStub, Serializable
{
    private static final long serialVersionUID = -3507906061639787167L;

    /**
     * The subset of {@link GroupEntity} fields that this implementation expects to be available in
     * {@link #from(GenericValue)}.
     * <p>
     * This is provided so that only the appropriate subset of fields need be
     * queried when it is known that all of the returned entities will be turned
     * into {@code OfBizGroup} objects.
     * </p>
     */
    static final List<String> SUPPORTED_FIELDS = ImmutableList.<String>builder()
            .add(ID)
            .add(DIRECTORY_ID)
            .add(NAME)
            .add(ACTIVE)
            .add(LOCAL)
            .add(CREATED_DATE)
            .add(UPDATED_DATE)
            .add(TYPE)
            .add(DESCRIPTION)
            .build();

    static OfBizGroup from(final GenericValue groupGenericValue)
    {
        return new OfBizGroup(Assertions.notNull(groupGenericValue));
    }

    private final long id;
    private final long directoryId;
    private final String name;
    private final boolean active;
    private final boolean local;
    private final Date createdDate;
    private final Date updatedDate;
    private final GroupType groupType;
    private final String description;

    private String lowerName;
    private int hash;

    @VisibleForTesting
    OfBizGroup(final long id, final long directoryId, final String name, final boolean active, final boolean local,
            final Date createdDate, final Date updatedDate, final GroupType groupType, final String description)
    {
        this.id = id;
        this.directoryId = directoryId;
        this.name = name;
        this.active = active;
        this.local = local;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.groupType = groupType;
        this.description = description;
    }

    private OfBizGroup(final GenericValue groupGenericValue)
    {
        this(
                groupGenericValue.getLong(ID),
                groupGenericValue.getLong(DIRECTORY_ID),
                groupGenericValue.getString(NAME),
                BooleanUtils.toBoolean(groupGenericValue.getInteger(ACTIVE)),
                BooleanUtils.toBoolean(groupGenericValue.getInteger(LOCAL)),
                groupGenericValue.getTimestamp(CREATED_DATE),
                groupGenericValue.getTimestamp(UPDATED_DATE),
                GroupType.valueOf(groupGenericValue.getString(TYPE)),
                groupGenericValue.getString(DESCRIPTION)
        );
    }

    public long getId()
    {
        return id;
    }

    public GroupType getType()
    {
        return groupType;
    }

    public boolean isActive()
    {
        return active;
    }

    public String getDescription()
    {
        return description;
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
        String lower = this.lowerName;
        if (lower == null)
        {
            lower = IdentifierUtils.toLowerCase(name);
            this.lowerName = lower;
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

    public boolean isLocal()
    {
        return local;
    }

    @Override
    public final boolean equals(Object o)
    {
        return o instanceof Group && GroupComparator.equal(this, (Group)o);
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")  // see below
    @Override
    public final int hashCode()
    {
        // The hash code is expensive to calculate because it is based on the group name in lowercase,
        // so we store a local copy of it.  There is no need for thread-safety concerns because the
        // worst thing that can happen is for a thread to see a stale 0 value and recalculate it.
        int h = hash;
        if (h == 0)
        {
            h = GroupComparator.hashCode(this);
            hash = h;
        }
        return hash;
    }

    public int compareTo(@Nonnull Group other)
    {
        return GroupComparator.compareTo(this, other);
    }

    @Override
    public String toString()
    {
        return "Group[" + name + ':' + directoryId + ']';
    }
}
