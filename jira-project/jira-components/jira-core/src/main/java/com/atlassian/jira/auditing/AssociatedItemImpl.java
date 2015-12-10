package com.atlassian.jira.auditing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Function;

import org.ofbiz.core.entity.GenericValue;

/**
 *
 * @since v6.2
 */
public class AssociatedItemImpl implements AssociatedItem
{
    public static final String OBJECT_TYPE = "objectType";
    public static final String OBJECT_ID = "objectId";
    public static final String OBJECT_NAME = "objectName";
    public static final String OBJECT_PARENT_ID = "objectParentId";
    public static final String OBJECT_PARENT_NAME = "objectParentName";

    protected final GenericValue gv;

    public AssociatedItemImpl(final GenericValue gv)
    {
        this.gv = gv;
    }

    public static Function<? super GenericValue, AssociatedItem> from()
    {
        return new Function<GenericValue, AssociatedItem>()
        {
            @Override
            public AssociatedItem apply(final GenericValue input)
            {
                return new AssociatedItemImpl(input);
            }
        };
    }

    @Nonnull
    @Override
    public String getObjectName()
    {
        return gv.getString(AssociatedItemImpl.OBJECT_NAME);
    }

    @Nullable
    @Override
    public String getObjectId()
    {
        return gv.getString(AssociatedItemImpl.OBJECT_ID);
    }

    @Nullable
    @Override
    public String getParentName()
    {
        return gv.getString(AssociatedItemImpl.OBJECT_PARENT_NAME);
    }

    @Nullable
    @Override
    public String getParentId()
    {
        return gv.getString(AssociatedItemImpl.OBJECT_PARENT_ID);
    }

    @Nonnull
    @Override
    public Type getObjectType()
    {
        return Type.valueOf(gv.getString(AssociatedItemImpl.OBJECT_TYPE));
    }
}
