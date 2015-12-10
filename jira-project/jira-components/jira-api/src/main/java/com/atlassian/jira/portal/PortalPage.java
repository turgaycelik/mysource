package com.atlassian.jira.portal;

import com.atlassian.annotations.PublicApi;
import com.atlassian.gadgets.dashboard.Layout;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.user.ApplicationUser;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * This class represents a Dashboard page in JIRA.  It includes dashboard meta data (name, description) as well as
 * information about favouriting and share permissions.
 * <p/>
 * It no longer has a link to the PortletConfigurations (Gadgets) displayed on a dashboard.  To get this information one
 * should consult the {@link com.atlassian.jira.bc.portal.PortalPageService} directly.
 * <p/>
 * Use the supplied {@link com.atlassian.jira.portal.PortalPage.Builder} class to construct new instances of a
 * PortalPage.  This class is used to represent both System and User owned dashboards.  The only restriction is that
 * system dashboards may not have an owner.
 *
 * @since 4.1
 */
@PublicApi
public final class PortalPage implements SharedEntity
{
    /**
     * This is the {@link SharedEntity} type, eg "PortalPage"
     */
    public static final TypeDescriptor<PortalPage> ENTITY_TYPE = TypeDescriptor.Factory.get().create("PortalPage");

    private final Long id;
    private final String name;
    private final String description;
    private final Long favouriteCount;
    private final Layout layout;
    private final Long version;
    private final boolean systemDashboard;
    private final SharePermissions sharePermissions;
    private final ApplicationUser owner;

    private PortalPage(final Long id, final String name, final String description, final ApplicationUser owner, final Long favouriteCount,
            final Layout layout, final Long version, final SharePermissions sharePermissions, final boolean isSystemDashboard)
    {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.description = description;
        this.favouriteCount = favouriteCount;
        this.version = version;
        this.systemDashboard = isSystemDashboard;
        this.layout = layout;
        this.sharePermissions = sharePermissions;
    }

    public Long getId()
    {
        return id;
    }

    public Long getVersion()
    {
        return version;
    }

    public boolean isSystemDefaultPortalPage()
    {
        return systemDashboard;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    @Override
    public ApplicationUser getOwner()
    {
        return owner;
    }

    @Override
    public String getOwnerUserName()
    {
        return owner == null ? null : owner.getUsername();
    }

    public Layout getLayout()
    {
        return layout;
    }

    public Long getFavouriteCount()
    {
        return favouriteCount;
    }

    @SuppressWarnings ("unchecked")
    public final TypeDescriptor<PortalPage> getEntityType()
    {
        return ENTITY_TYPE;
    }

    public SharePermissions getPermissions()
    {
        return sharePermissions;
    }

    public static Builder portalPage(PortalPage page)
    {
        return new Builder().portalPage(page);
    }

    public static Builder id(Long id)
    {
        return new Builder().id(id);
    }

    public static Builder name(String name)
    {
        return new Builder().name(name);
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (obj == this)
        {
            return true;
        }
        if (obj.getClass() != getClass())
        {
            return false;
        }

        final PortalPage rhs = (PortalPage) obj;
        return new EqualsBuilder().
                append(id, rhs.id).
                append(name, rhs.name).
                append(description, rhs.description).
                append(favouriteCount, rhs.favouriteCount).
                append(sharePermissions, rhs.sharePermissions).
                append(layout, rhs.layout).
                append(getOwnerKey(), rhs.getOwnerKey()).
                append(version, rhs.version).
                append(systemDashboard, rhs.systemDashboard).
                isEquals();
    }

    private String getOwnerKey()
    {
        return owner == null ? "" : owner.getKey();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(13, 33).
                append(id).
                append(name).
                append(description).
                append(favouriteCount).
                append(sharePermissions).
                append(layout).
                append(getOwnerKey()).
                append(version).
                append(systemDashboard).
                hashCode();
    }

    public static class Builder
    {
        private boolean isSystemDashboard = false;
        private Long id;
        private String description;
        private String name;
        private Long favouriteCount = 0L;
        private SharePermissions permissions = SharePermissions.PRIVATE;
        private Layout layout = Layout.AA;
        private ApplicationUser owner;
        private Long version = 0L;

        public Builder portalPage(PortalPage page)
        {
            id = page.getId();
            name = page.getName();
            description = page.getDescription();
            isSystemDashboard = page.isSystemDefaultPortalPage();
            favouriteCount = page.getFavouriteCount();
            permissions = page.getPermissions();
            layout = page.getLayout();
            owner = page.getOwner();
            version = page.getVersion();
            return this;
        }

        public Builder id(Long id)
        {
            notNull("id", id);
            this.id = id;
            return this;
        }

        public Builder name(String name)
        {
            this.name = name;
            return this;
        }

        public Builder description(String description)
        {
            this.description = description;
            return this;
        }

        public Builder owner(ApplicationUser owner)
        {
            this.owner = owner;
            return this;
        }

        /** @deprecated Use {@link #owner(com.atlassian.jira.user.ApplicationUser)} instead. Since v6.0. */
        public Builder owner(String username)
        {
            return this.owner(ComponentAccessor.getUserManager().getUserByName(username));
        }

        public Builder permissions(SharePermissions permissions)
        {
            this.permissions = permissions;
            return this;
        }

        public Builder systemDashboard()
        {
            if (owner != null)
            {
                throw new IllegalStateException("System Dashboard cannot have an owner!");
            }
            this.isSystemDashboard = true;
            return this;
        }

        public Builder favouriteCount(Long favouriteCount)
        {
            this.favouriteCount = favouriteCount == null ? 0L : favouriteCount;
            return this;
        }

        public Builder layout(Layout layout)
        {
            this.layout = layout;
            return this;
        }

        public Builder version(Long version)
        {
            this.version = version;
            return this;
        }

        public PortalPage build()
        {
            if (isSystemDashboard && owner != null)
            {
                throw new IllegalStateException("System Dashboard cannot have an owner!");
            }
            return new PortalPage(id, name, description, owner, favouriteCount, layout, version, permissions, isSystemDashboard);
        }
    }
}
