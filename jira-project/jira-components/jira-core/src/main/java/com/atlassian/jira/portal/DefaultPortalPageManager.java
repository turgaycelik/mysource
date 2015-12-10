package com.atlassian.jira.portal;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.gadgets.Vote;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.dashboard.permission.GadgetPermissionManager;
import com.atlassian.jira.portal.events.DashboardCreated;
import com.atlassian.jira.portal.events.DashboardDeleted;
import com.atlassian.jira.portal.events.DashboardUpdated;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.sharing.ShareManager;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntity.SharePermissions;
import com.atlassian.jira.sharing.SharedEntity.TypeDescriptor;
import com.atlassian.jira.sharing.index.SharedEntityIndexer;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.Resolver;
import com.atlassian.jira.util.collect.EnclosedIterable;
import com.atlassian.jira.util.collect.Transformed;
import com.atlassian.jira.util.dbc.Assertions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.not;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * The base class for PortalPageManager implementations
 *
 * @since v3.13
 */
public class DefaultPortalPageManager implements PortalPageManager
{
    private final ShareManager shareManager;
    private final PortalPageStore portalPageStore;
    private final PortletConfigurationManager portletConfigurationManager;
    private final SharedEntityIndexer indexer;
    private GadgetPermissionManager gadgetPermissionManager;
    private final EventPublisher eventPublisher;
    private final JiraAuthenticationContext authenticationContext;

    /**
     * A resolver that can set permissions
     */
    private final Resolver<PortalPage, PortalPage> permissionResolver = new Resolver<PortalPage, PortalPage>()
    {
        @Override
        public PortalPage get(final PortalPage portalPage)
        {
            return setRelatedState(portalPage);
        }
    };

    public DefaultPortalPageManager(final ShareManager shareManager, final PortalPageStore portalPageStore, final PortletConfigurationManager portletConfigurationManager, final SharedEntityIndexer indexer, EventPublisher eventPublisher, JiraAuthenticationContext authenticationContext)
    {
        this.shareManager = shareManager;
        this.portalPageStore = portalPageStore;
        this.portletConfigurationManager = portletConfigurationManager;
        this.indexer = indexer;
        this.eventPublisher = eventPublisher;
        this.authenticationContext = authenticationContext;
    }

    @Override
    public TypeDescriptor<PortalPage> getType()
    {
        return PortalPage.ENTITY_TYPE;
    }

    @Override
    public void adjustFavouriteCount(final SharedEntity entity, final int adjustmentValue)
    {
        notNull("entity", entity);
        Assertions.equals("PortalPage type", PortalPage.ENTITY_TYPE, entity.getEntityType());

        final PortalPage portalPage = portalPageStore.adjustFavouriteCount(entity, adjustmentValue);

        indexer.index(setRelatedState(portalPage)).await();
    }

    @Override
    public PortalPage getSharedEntity(final Long entityId)
    {
        notNull("entityId", entityId);
        return getPortalPageById(entityId);
    }

    @Override
    public PortalPage getSharedEntity(final User user, final Long entityId)
    {
        notNull("entityId", entityId);
        return getPortalPage(ApplicationUsers.from(user), entityId);
    }

    @Override
    public boolean hasPermissionToUse(final User user, final PortalPage portalPage)
    {
        notNull("portalPage", portalPage);
        return portalPage.isSystemDefaultPortalPage() || isSharedWith(portalPage, ApplicationUsers.from(user));
    }

    /*
     * =============================================== GET/ FIND ===============================================
     */

    @Override
    public EnclosedIterable<PortalPage> getAll()
    {
        return Transformed.enclosedIterable(portalPageStore.getAll(), permissionResolver);
    }

    @Override
    public EnclosedIterable<SharedEntity> getAllIndexableSharedEntities()
    {
        @SuppressWarnings("unchecked")
        final EnclosedIterable<SharedEntity> all = (EnclosedIterable) getAll();
        return all;
    }

    @Override
    public EnclosedIterable<PortalPage> get(final RetrievalDescriptor descriptor)
    {
        return Transformed.enclosedIterable(portalPageStore.get(descriptor), permissionResolver);
    }

    // NOTE: we don't care about the searching user here, as we don't need to sanitise or filter the results.
    @Override
    public EnclosedIterable<PortalPage> get(final User user, final RetrievalDescriptor ids)
    {
        return get(ids);
    }

    @Override
    public Collection<PortalPage> getAllOwnedPortalPages(final ApplicationUser owner)
    {
        notNull("owner", owner);
        notNull("owner.name", owner.getName());
        final Collection<PortalPage> portalPages = portalPageStore.getAllOwnedPortalPages(owner);
        if (portalPages == null)
        {
            return Collections.emptyList();
        }
        else
        {
            return setRelatedState(portalPages);
        }
    }

    @Override
    public Collection<PortalPage> getAllOwnedPortalPages(final User owner)
    {
        return getAllOwnedPortalPages(ApplicationUsers.from(owner));
    }

    @Override
    public PortalPage getPortalPageByName(final ApplicationUser owner, final String pageName)
    {
        notNull("owner", owner);
        notNull("owner,name", owner.getName());
        notNull("pageName", pageName);

        final PortalPage portalPage = portalPageStore.getPortalPageByOwnerAndName(owner, pageName);
        return setRelatedState(portalPage);
    }

    @Override
    public PortalPage getPortalPageByName(final User owner, final String pageName)
    {
        return getPortalPageByName(ApplicationUsers.from(owner), pageName);
    }

    @Override
    public PortalPage getSystemDefaultPortalPage()
    {
        final PortalPage systemDefaultPortalPage = portalPageStore.getSystemDefaultPortalPage();
        return setRelatedState(systemDefaultPortalPage);
    }

    @Override
    public PortalPage getPortalPage(final ApplicationUser user, final Long id)
    {
        notNull("id", id);

        final PortalPage portalPage = portalPageStore.getPortalPage(id);
        if (portalPage == null)
        {
            return null;
        }
        // the System Default Dashboard is a special page and can be
        // shown to anyone including the Anonymous (null) user
        if (!hasPermissionToUse(ApplicationUsers.toDirectoryUser(user), portalPage))
        {
            return null;
        }
        return setRelatedState(portalPage);
    }

    @Override
    public PortalPage getPortalPage(final User user, final Long id)
    {
        return getPortalPage(ApplicationUsers.from(user), id);
    }

    @Override
    public PortalPage getPortalPageById(final Long portalPageId)
    {
        notNull("portalPageId", portalPageId);

        final PortalPage portalPage = portalPageStore.getPortalPage(portalPageId);
        return setRelatedState(portalPage);
    }

    /*
     * =============================================== CRUD ===============================================
     */
    @Override
    public PortalPage create(final PortalPage portalPage)
    {
        assertCreate(portalPage);

        final PortalPage createdPortalPage = PortalPage.portalPage(portalPageStore.create(portalPage)).permissions(portalPage.getPermissions()).build();

        shareManager.updateSharePermissions(createdPortalPage);
        indexer.index(createdPortalPage).await();
        eventPublisher.publish(new DashboardCreated(createdPortalPage, authenticationContext.getLoggedInUser()));
        return createdPortalPage;
    }

    @Override
    public PortalPage createBasedOnClone(final ApplicationUser pageOwner, final PortalPage portalPage, final PortalPage clonePortalPage)
    {
        assertCreate(portalPage);
        notNull("clonePortalPage", clonePortalPage);

        //need to set the layout here since it determines the number of columns for the new portal page.  (JRA-16991)
        final PortalPage portalPageToCreate = PortalPage.portalPage(portalPage).layout(clonePortalPage.getLayout()).build();

        final PortalPage newPortalPage = create(portalPageToCreate);
        clonePortletsFromOnePageToAnother(pageOwner, clonePortalPage, newPortalPage);
        eventPublisher.publish(new DashboardCreated(newPortalPage, authenticationContext.getLoggedInUser()));
        return newPortalPage;
    }

    @Override
    public PortalPage createBasedOnClone(final User pageOwner, final PortalPage portalPage, final PortalPage clonePortalPage)
    {
        return createBasedOnClone(ApplicationUsers.from(pageOwner), portalPage, clonePortalPage);
    }

    @Override
    public PortalPage update(final PortalPage portalPage)
    {
        assertCreate(portalPage);
        notNull("portalPage.id", portalPage.getId());

        final PortalPage newPortalPage = PortalPage.portalPage(portalPageStore.update(portalPage)).permissions(portalPage.getPermissions()).build();
        shareManager.updateSharePermissions(newPortalPage);
        indexer.index(newPortalPage).await();
        eventPublisher.publish(new DashboardUpdated(portalPage, newPortalPage, authenticationContext.getLoggedInUser()));
        return newPortalPage;
    }

    @Override
    public void delete(final Long portalPageId)
    {
        notNull("portalPageId", portalPageId);
        //
        // delete all PortletConfigs associated with the page first. The underlying portletConfigurationManager/Store
        // also cleans associated propertySets
        //
        final List<PortletConfiguration> portlectConfigurations = portletConfigurationManager.getByPortalPage(portalPageId);
        for (final PortletConfiguration portletConfiguration : portlectConfigurations)
        {
            portletConfigurationManager.delete(portletConfiguration);
        }

        final SharedEntity identifier = new SharedEntity.Identifier(portalPageId, PortalPage.ENTITY_TYPE, (ApplicationUser) null);

        portalPageStore.delete(portalPageId);
        shareManager.deletePermissions(identifier);
        indexer.deIndex(identifier).await();
        eventPublisher.publish(new DashboardDeleted(identifier, authenticationContext.getLoggedInUser()));
    }

    @Override
    public void saveLegacyPortletConfiguration(final PortletConfiguration portletConfiguration)
    {
        notNull("portletConfiguration", portletConfiguration);

        //check the PC exists!
        final Long id = portletConfiguration.getId();
        final PortletConfiguration pc = portletConfigurationManager.getByPortletId(id);
        if (pc != null)
        {
            portletConfigurationManager.store(portletConfiguration);
        }
        else
        {
            throw new IllegalStateException("Trying to update portletConfiguration that doesn't exist with id '" + id + "'.");
        }
    }

    @Override
    public SharedEntitySearchResult<PortalPage> search(final SharedEntitySearchParameters searchParameters, final ApplicationUser user, final int pagePosition, final int pageWidth)
    {
        notNull("searchParameters", searchParameters);
        not("pagePosition < 0", pagePosition < 0);
        not("pageWidth <= 0", pageWidth <= 0);

        return indexer.getSearcher(PortalPage.ENTITY_TYPE).search(searchParameters, user, pagePosition, pageWidth);
    }

    @Override
    public SharedEntitySearchResult<PortalPage> search(final SharedEntitySearchParameters searchParameters, final User user, final int pagePosition, final int pageWidth)
    {
        return search(searchParameters,  ApplicationUsers.from(user), pagePosition, pageWidth);
    }

    @Override
    public List<List<PortletConfiguration>> getPortletConfigurations(final Long portalPageId)
    {
        final List<List<PortletConfiguration>> columns = new ArrayList<List<PortletConfiguration>>();
        final List<PortletConfiguration> portletConfigurations = portletConfigurationManager.getByPortalPage(portalPageId);
        final PortalPage portalPage = getPortalPageById(portalPageId);
        if ((portalPage != null) && !portletConfigurations.isEmpty())
        {
            initColumns(portalPage.getLayout().getNumberOfColumns(), columns);
            for (final PortletConfiguration portletConfiguration : portletConfigurations)
            {
                final int column = portletConfiguration.getColumn();
                columns.get(column).add(portletConfiguration);
            }

            //Once all portlet configs are inserted, sort each column then make it an unmodifieable list.
            for (int i = 0; i < columns.size(); i++)
            {
                final List<PortletConfiguration> column = columns.get(i);
                Collections.sort(column);
                columns.set(i, Collections.<PortletConfiguration> unmodifiableList(column));
            }
        }
        return Collections.unmodifiableList(columns);
    }

    private void initColumns(final int numberOfColumns, final List<List<PortletConfiguration>> columns)
    {
        for (int i = 0; i < numberOfColumns; i++)
        {
            columns.add(new ArrayList<PortletConfiguration>());
        }
    }

    private void clonePortletsFromOnePageToAnother(final ApplicationUser owner, final PortalPage clonePortalPage, final PortalPage targetPortalPage)
    {
        notNull("owner", owner);
        notNull("clonePortalPage", clonePortalPage);
        notNull("targetPortalPage", targetPortalPage);

        final List<PortletConfiguration> pcsToClone = portletConfigurationManager.getByPortalPage(clonePortalPage.getId());
        for (final PortletConfiguration pc : pcsToClone)
        {
            final String key = getGadgetPermissionManager().extractModuleKey(pc.getGadgetURI().toASCIIString());
            if ((key == null) || getGadgetPermissionManager().voteOn(key, ApplicationUsers.toDirectoryUser(owner)).equals(Vote.ALLOW))
            {
                portletConfigurationManager.addGadget(targetPortalPage.getId(), pc.getColumn(), pc.getRow(), pc.getGadgetURI(), pc.getColor(),
                        pc.getUserPrefs());
            }
        }
    }

    // Gotten like this to avoid cyclic dep
    GadgetPermissionManager getGadgetPermissionManager()
    {
        if (gadgetPermissionManager == null)
        {
            gadgetPermissionManager = ComponentAccessor.getComponentOfType(GadgetPermissionManager.class);
        }
        return gadgetPermissionManager;
    }

    private boolean isSharedWith(final PortalPage entity, final ApplicationUser user)
    {
        return shareManager.isSharedWith(user, entity);
    }

    private Collection<PortalPage> setRelatedState(final Collection<PortalPage> portalPages)
    {
        final Collection<PortalPage> ret = new ArrayList<PortalPage>(portalPages.size());
        for (final PortalPage portalPage : portalPages)
        {
            ret.add(setRelatedState(portalPage));
        }
        return ret;
    }

    private PortalPage setRelatedState(final PortalPage portalPage)
    {
        if (portalPage != null)
        {
            final PortalPage.Builder builder = PortalPage.portalPage(portalPage);
            //the system default dashboard should always have the global permission
            if (portalPage.isSystemDefaultPortalPage())
            {
                builder.permissions(SharePermissions.GLOBAL);
            }
            else
            {
                builder.permissions(shareManager.getSharePermissions(portalPage));
            }
            return builder.build();
        }
        return null;
    }

    private void assertCreate(final PortalPage portalPage)
    {
        notNull("portalPage", portalPage);
        if (!portalPage.isSystemDefaultPortalPage())
        {
            notNull("portalPage.owner", portalPage.getOwner());
        }
        notNull("portalPage.pageName", portalPage.getName());
    }
}
