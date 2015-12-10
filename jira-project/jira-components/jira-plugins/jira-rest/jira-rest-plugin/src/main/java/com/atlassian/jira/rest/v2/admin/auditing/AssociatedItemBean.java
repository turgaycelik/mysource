package com.atlassian.jira.rest.v2.admin.auditing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.auditing.AssociatedItem;

import com.google.common.base.Function;

import org.codehaus.jackson.annotate.JsonAutoDetect;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

/**
 * @since 6.3
 */
@SuppressWarnings("unused")
@JsonAutoDetect
public class AssociatedItemBean
{

    private String id;
    private String name;
    private String typeName;
    private String parentId;
    private String parentName;

    public AssociatedItemBean() {
    }

    public AssociatedItemBean(final AssociatedItem item) {
        id = item.getObjectId();
        name = item.getObjectName();
        typeName = item.getObjectType().name();
        parentId = item.getParentId();
        parentName = item.getParentName();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public String getTypeName()
    {
        return typeName;
    }

    public String getParentId()
    {
        return parentId;
    }

    public String getParentName()
    {
        return parentName;
    }

    public AssociatedItem toAssociatedItem()
    {
        notBlank("name", name);
        notBlank("typeName", typeName);

        final AssociatedItem.Type type = AssociatedItem.Type.valueOf(typeName);

        return new AssociatedItem() {
            @Nonnull
            @Override
            public String getObjectName() {
                return defaultIfEmpty(name, null);
            }

            @Nullable
            @Override
            public String getObjectId() {
                return defaultIfEmpty(id, null);
            }

            @Nullable
            @Override
            public String getParentName() {
                return defaultIfEmpty(parentName, null);
            }

            @Nullable
            @Override
            public String getParentId() {
                return defaultIfEmpty(parentId, null);
            }

            @Nonnull
            @Override
            public Type getObjectType() {
                return type;
            }
        };
    }

    public static Function<AssociatedItemBean, AssociatedItem> mapToAssociatedItem()
    {
        return new Function<AssociatedItemBean, AssociatedItem>() {
            @Override
            public AssociatedItem apply(@Nullable AssociatedItemBean input) {
                return input != null ? input.toAssociatedItem() : null;
            }
        };
    };
}
