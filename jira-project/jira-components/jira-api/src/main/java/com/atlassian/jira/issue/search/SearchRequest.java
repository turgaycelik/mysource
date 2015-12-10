package com.atlassian.jira.issue.search;

import com.atlassian.annotations.PublicApi; import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.user.ApplicationUser;
import javax.annotation.Nonnull;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.order.OrderByImpl;
import org.apache.commons.lang.StringUtils;

import static com.atlassian.jira.user.ApplicationUsers.getKeyFor;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A shareable representation of a search for issues. Officially known as a "Filter" or "Saved Filter".
 *
 * This class binds the {@link com.atlassian.query.Query}, which is used to perform the actual search, and
 * the saved information (such as name, description), and any permissions that may be associated with the saved search
 * together.
 */
@PublicApi
public class SearchRequest implements SharedEntity
{
    public static final TypeDescriptor<SearchRequest> ENTITY_TYPE = TypeDescriptor.Factory.get().create("SearchRequest");

    private Long id;
    private String name;
    private String description;
    private Long favouriteCount;
    private String ownerKey;
    private Query query;
    private boolean modified = false;
    private boolean loaded = false;
    // Whether to use the Search requests specific columns
    private boolean useColumns;
    // Calculated properties
    private SharePermissions sharePermissions = SharePermissions.PRIVATE; // default to private

    /**
     * A no-arg constructor that will build a SearchRequest with an empty {@link com.atlassian.query.Query}, this
     * will be a search that will find all issues with the default system sorting . You can then use the setter methods
     * to set the attributes you wish this SearchRequest to contain.
     */
    public SearchRequest()
    {
        this.query = new QueryImpl(null, new OrderByImpl(), null);
        setModified(false);
        setUseColumns(true);
    }

    /**
     * Creates a SearchRequest with the specified {@link com.atlassian.query.Query} and no other attributes.
     * This can be used to create a programtic SearchRequest that can be used to perform a search but is not ready to
     * be saved.
     *
     * @param query provides the details of the search that will be performed with this SearchRequest.
     */
    public SearchRequest(final Query query)
    {
        this.query = query;
        setModified(false);
        setUseColumns(true);
    }

    /**
     * Used to create a SearchRequest that copies all the information from the old search request. This includes
     * the name, description, author, id, favCount and the SearchQuery.
     *
     * @param oldRequest defines all the attributes that this SearchRequest will contain.
     */
    public SearchRequest(final SearchRequest oldRequest)
    {
        this(oldRequest.getQuery(), oldRequest.getOwner(), oldRequest.getName(), oldRequest.getDescription(), oldRequest.getId(), oldRequest.getFavouriteCount());
        setUseColumns(oldRequest.useColumns());
        setPermissions(oldRequest.getPermissions());
        setModified(oldRequest.isModified());
    }

    /**
     * Build a SearchRequest with the provided attributes, this can be used if you want to create a SearchRequest that
     * can be persisted.
     *
     * @param query defines what this SearchRequest will search for.
     * @param owner the owner, user who initially create the request.
     * @param name the name associated with this SearchRequest, can be set even if this is not persistent yet.
     * @param description the description associated with this SearchRequest, can be set even if this is not persistent yet.
     * filter.
     */
    public SearchRequest(final Query query, final ApplicationUser owner, final String name, final String description)
    {
        this();
        this.ownerKey = getKeyFor(owner);
        this.name = name;
        this.description = description;
        this.query = query;
    }

    /**
     * Build a SearchRequest with the provided attributes.
     *
     * @param query defines what this SearchRequest will search for.
     * @param owner the owner, user who initially create the request.
     * @param name the name associated with this SearchRequest, can be set even if this is not persistent yet.
     * @param description the description associated with this SearchRequest, can be set even if this is not persistent yet.
     * @param id the persistent id of the SearchRequest, null if the SearchRequest is not persistent.
     * @param favouriteCount the number of users that have this filter as a favortie, will only be set if this is a persistent
     * filter.
     */
    public SearchRequest(final Query query, final ApplicationUser owner, final String name, final String description, final Long id, long favouriteCount)
    {
        this();
        this.ownerKey = getKeyFor(owner);
        this.name = name;
        this.description = description;
        this.query = query;
        this.id = id;
        this.favouriteCount = favouriteCount;
    }
    /**
     * @deprecated Use {@link #SearchRequest(com.atlassian.query.Query, com.atlassian.jira.user.ApplicationUser, String, String)} instead. Since v6.0.
     *
     * Build a SearchRequest with the provided attributes, this can be used if you want to create a SearchRequest that
     * can be persisted.
     *
     * @param query defines what this SearchRequest will search for.
     * @param ownerUserName the owner, user who initially create the request.
     * @param name the name associated with this SearchRequest, can be set even if this is not persistent yet.
     * @param description the description associated with this SearchRequest, can be set even if this is not persistent yet.
     * filter.
     */
    public SearchRequest(final Query query, final String ownerUserName, final String name, final String description)
    {
        this(query, ComponentAccessor.getUserManager().getUserByName(ownerUserName), name, description);
    }

    /**
     * @deprecated Use {@link #SearchRequest(com.atlassian.query.Query, com.atlassian.jira.user.ApplicationUser, String, String, Long, long)} instead. Since v6.0.
     *
     * Build a SearchRequest with the provided attributes.
     *
     * @param query defines what this SearchRequest will search for.
     * @param ownerUserName the owner, user who initially create the request.
     * @param name the name associated with this SearchRequest, can be set even if this is not persistent yet.
     * @param description the description associated with this SearchRequest, can be set even if this is not persistent yet.
     * @param id the persistent id of the SearchRequest, null if the SearchRequest is not persistent.
     * @param favouriteCount the number of users that have this filter as a favortie, will only be set if this is a persistent
     * filter.
     */
    public SearchRequest(final Query query, final String ownerUserName, final String name, final String description, final Long id, long favouriteCount)
    {
        this(query, ComponentAccessor.getUserManager().getUserByName(ownerUserName), name, description, id, favouriteCount);
    }

    /**
     * Gets the SearchQuery that defines the search that will be performed for this SearchRequest.
     *
     * @return the SearchQuery that defines the search that will be performed for this SearchRequest, not null.
     */
    @Nonnull
    public Query getQuery()
    {
        return query;
    }

    public void setQuery(final Query query)
    {
        notNull("query", query);
        setModified(true);
        this.query = query;
    }

    @Override
    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        setModified(true);
        this.name = name;
    }

    @Override
    public Long getId()
    {
        return id;
    }

    @Override
    public SharePermissions getPermissions()
    {
        return sharePermissions;
    }

    public void setPermissions(final SharePermissions sharePermissions)
    {
        notNull("permissions", sharePermissions);
        this.sharePermissions = sharePermissions;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public ApplicationUser getOwner()
    {
        return ComponentAccessor.getUserManager().getUserByKeyEvenWhenUnknown(ownerKey);
    }

    @Override
    public String getOwnerUserName()
    {
        return ComponentAccessor.getUserKeyService().getUsernameForKey(ownerKey);
    }

    public void setDescription(final String description)
    {
        setModified(true);
        this.description = description;
    }

    protected void setFavouriteCount(final Long favouriteCount)
    {
        setModified(true);
        this.favouriteCount = favouriteCount;
    }

    @Override
    public Long getFavouriteCount()
    {
        if (favouriteCount == null)
        {
            favouriteCount = 0L;
        }
        return favouriteCount;
    }

    /**
     * Set the owner of the SearchRequest.
     *
     * @param owner the user who is the search requests owner.
     */
    public void setOwner(final ApplicationUser owner)
    {
        setModified(true);
        // SearchRequest is cached, so just hang onto the key and re-lookup the user each time in case username has changed.
        this.ownerKey = getKeyFor(owner);
    }

    /**
     * @deprecated Use {@link #setOwner(com.atlassian.jira.user.ApplicationUser)} instead. Since v6.0.
     * Set the owner of the SearchRequest.
     *
     * @param ownerUserName the name of the user who is the search requests owner.
     */
    public void setOwnerUserName(final String ownerUserName)
    {
        if (ownerUserName == null)
        {
            setOwner(null);
        }
        else
        {
            setOwner(ComponentAccessor.getUserManager().getUserByName(ownerUserName));
        }
    }

    public boolean isModified()
    {
        return modified;
    }

    public void setModified(final boolean modified)
    {
        this.modified = modified;
    }

    public boolean isLoaded()
    {
        return this.id != null;
    }

    /**
     * @return true if this SearchRequest should be displayed using the saved column layout, false otherwise
     */
    public boolean useColumns()
    {
        return useColumns;
    }

    public void setUseColumns(final boolean useColumns)
    {
        this.useColumns = useColumns;
    }

    // /CLOVER:OFF
    @Override
    public String toString()
    {
        final StringBuilder buff = new StringBuilder();
        buff.append("Search Request: name: ");
        buff.append(getName());
        buff.append("\n");

        if (query != null && !StringUtils.isBlank(query.toString()))
        {
            buff.append("query = ").append(query.toString());
        }

        return buff.toString();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final SearchRequest request = (SearchRequest) o;

        if (loaded != request.loaded)
        {
            return false;
        }
        if (description != null ? !description.equals(request.description) : request.description != null)
        {
            return false;
        }
        if (id != null ? !id.equals(request.id) : request.id != null)
        {
            return false;
        }
        if (name != null ? !name.equals(request.name) : request.name != null)
        {
            return false;
        }
        if (ownerKey != null ? !ownerKey.equals(request.ownerKey) : request.ownerKey != null)
        {
            return false;
        }
        if (!query.equals(request.query))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (ownerKey != null ? ownerKey.hashCode() : 0);
        result = 31 * result + query.hashCode();
        result = 31 * result + (loaded ? 1 : 0);
        return result;
    }

    @SuppressWarnings("unchecked")
    public final TypeDescriptor<SearchRequest> getEntityType()
    {
        return SearchRequest.ENTITY_TYPE;
    }
}
