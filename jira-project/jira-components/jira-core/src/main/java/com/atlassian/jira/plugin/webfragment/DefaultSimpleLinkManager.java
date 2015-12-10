package com.atlassian.jira.plugin.webfragment;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.fugue.Option;
import com.atlassian.jira.plugin.webfragment.descriptors.SimpleLinkFactoryModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkImpl;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSectionImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.http.JiraUrl;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugin.web.api.DynamicWebInterfaceManager;
import com.atlassian.plugin.web.api.WebItem;
import com.atlassian.plugin.web.api.WebSection;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import static com.atlassian.fugue.Option.option;
import static com.google.common.collect.Iterables.any;
import static java.lang.Boolean.parseBoolean;

/**
 * Default implementation of the SimpleLinkManager This actually uses combines SimpleLinkFactory lists with {@link
 * com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor} links and {@link com.atlassian.plugin.web.descriptors.WebSectionModuleDescriptor}
 * sections, respecting weights.
 *
 * @since v4.0
 * @deprecated since 6.3 - use {@link com.atlassian.plugin.web.api.DynamicWebInterfaceManager} directly
 */
public class DefaultSimpleLinkManager implements SimpleLinkManager
{
    private static final Logger log = Logger.getLogger(DefaultSimpleLinkManager.class);

    private final SimpleLinkFactoryModuleDescriptors simpleLinkFactoryModuleDescriptors;
    private final JiraAuthenticationContext authenticationContext;
    private final WebResourceUrlProvider webResourceUrlProvider;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final DynamicWebInterfaceManager webInterfaceManager;

    public DefaultSimpleLinkManager(
            final DynamicWebInterfaceManager webInterfaceManager,
            final SimpleLinkFactoryModuleDescriptors simpleLinkFactoryModuleDescriptors,
            final JiraAuthenticationContext authenticationContext,
            final WebResourceUrlProvider webResourceUrlProvider,
            final VelocityRequestContextFactory velocityRequestContextFactory)
    {
        this.webInterfaceManager = webInterfaceManager;
        this.simpleLinkFactoryModuleDescriptors = simpleLinkFactoryModuleDescriptors;
        this.authenticationContext = authenticationContext;
        this.webResourceUrlProvider = webResourceUrlProvider;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
    }

    /**
     * This determines whether a location should be loaded lazily if possible. This loops through all sections for the
     * location and then retrieves all {@link com.atlassian.jira.plugin.webfragment.descriptors.SimpleLinkFactoryModuleDescriptor}
     * for those sections and sees whether any of the factories say they should be lazy.  If any say true, return true.
     *
     * @param location The location to check for
     * @param remoteUser The user that we are generating the list for
     * @param jiraHelper The context under which the list is being generated
     * @return true if any of the underlying factories for this location say they should be lazy, false otherwise
     */
    public boolean shouldLocationBeLazy(@Nonnull final String location, final User remoteUser, @Nonnull final JiraHelper jiraHelper)
    {
        final Iterable<WebSection> sections = webInterfaceManager.getDisplayableWebSections(location, makeContext(remoteUser, jiraHelper));
        final Iterable<SimpleLinkFactoryModuleDescriptor> linkFactories = simpleLinkFactoryModuleDescriptors.get();
        for (final WebSection section : sections)
        {
            if (parseBoolean(section.getParams().get("lazy")) || any(linkFactories, new SectionPredicate(location, section)))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * This determines whether a section should be loaded lazily if possible.
     * DO NOT USE: This method only checks simple
     * link factories and not the web-section. Nothing uses it now and it should not be used in future!
     *
     * @param section The section to check for
     * @return true if any of the underlying factories for this section say they should be lazy, false otherwise
     */
    public boolean shouldSectionBeLazy(final String section)
    {
        //DO NOT USE: This method only checks simple link factories and not the web-section. Nothing uses it now
        //and it should not be used in future!
        for (final SimpleLinkFactoryModuleDescriptor linkFactory : simpleLinkFactoryModuleDescriptors.get())
        {
            if (section.equals(linkFactory.getSection()))
            {
                if (linkFactory.shouldBeLazy())
                {
                    return true;
                }
            }
        }

        return false;
    }

    @Nonnull
    public List<SimpleLink> getLinksForSection(@Nonnull final String section, final User remoteUser, @Nonnull final JiraHelper jiraHelper)
    {
        return getLinksForSection(section, remoteUser, jiraHelper, false);
    }

    @Nonnull
    public List<SimpleLink> getLinksForSection(@Nonnull final String section, final User remoteUser, @Nonnull final JiraHelper jiraHelper, boolean addIconCachingPrefix)
    {
        //noinspection unchecked
        return getLinks(section, webInterfaceManager.getDisplayableWebItems(section, makeContext(remoteUser, jiraHelper)), remoteUser, jiraHelper, addIconCachingPrefix);
    }

    @Nonnull
    public List<SimpleLink> getLinksForSectionIgnoreConditions(@Nonnull final String section, final User remoteUser, @Nonnull final JiraHelper jiraHelper)
    {
        //noinspection unchecked
        return getLinks(section, webInterfaceManager.getWebItems(section, makeContext(remoteUser, jiraHelper)), remoteUser, jiraHelper, false);
    }

    private List<SimpleLink> getLinks(final String section, final Iterable<WebItem> items, final User user, final JiraHelper jiraHelper, boolean addIconCachingPrefix)
    {
        final List<SimpleLink> returnLinks = new ArrayList<SimpleLink>();

        final List<SimpleLinkFactoryModuleDescriptor> matchingFactories = new ArrayList<SimpleLinkFactoryModuleDescriptor>();
        for (final SimpleLinkFactoryModuleDescriptor linkFactory : simpleLinkFactoryModuleDescriptors.get())
        {
            if (section.equals(linkFactory.getSection()))
            {
                matchingFactories.add(linkFactory);
            }
        }

        final Iterator<SimpleLinkFactoryModuleDescriptor> factoryIterator = matchingFactories.iterator();
        final Iterator<WebItem> itemIterator = items.iterator();
        SimpleLinkFactoryModuleDescriptor factory = factoryIterator.hasNext() ? factoryIterator.next() : null;
        WebItem item = itemIterator.hasNext() ? itemIterator.next() : null;

        while ((factory != null) || (item != null))
        {
            if ((factory != null) && (item != null))
            {
                if (factory.getWeight() < item.getWeight())
                {
                    final SimpleLinkFactory factoryModule = factory.getModule();
                    if (factoryModule != null)
                    {
                        returnLinks.addAll(factoryModule.getLinks(user, jiraHelper.getContextParams()));
                    }
                    factory = factoryIterator.hasNext() ? factoryIterator.next() : null;
                }
                else
                {
                    final SimpleLink link = convertWebItemToSimpleLink(item, addIconCachingPrefix);
                    if (link != null)
                    {
                        returnLinks.add(link);
                    }
                    item = itemIterator.hasNext() ? itemIterator.next() : null;
                }
            }
            else if (factory == null)
            {
                final SimpleLink link = convertWebItemToSimpleLink(item, addIconCachingPrefix);
                if (link != null)
                {
                    returnLinks.add(link);
                }
                item = itemIterator.hasNext() ? itemIterator.next() : null;
            }
            else
            //item == null
            {
                final SimpleLinkFactory factoryModule = factory.getModule();
                if (factoryModule != null)
                {
                    returnLinks.addAll(factoryModule.getLinks(user, jiraHelper.getContextParams()));
                }
                factory = factoryIterator.hasNext() ? factoryIterator.next() : null;
            }
        }
        return returnLinks;
    }

    // Converts JiraWebItemModuleDescriptors to SimpleLinks
    private SimpleLink convertWebItemToSimpleLink(final WebItem item, boolean addIconCachingPrefix)
    {
        String iconUrl = item.getParams().get("iconUrl");
        if (StringUtils.isNotBlank(iconUrl) && isRelativeUrl(iconUrl))
        {
            if (addIconCachingPrefix)
            {
                final String staticResourcePrefix = webResourceUrlProvider.getStaticResourcePrefix(UrlMode.RELATIVE);
                iconUrl = staticResourcePrefix + iconUrl;
            }
            else
            {
                final String baseUrl = velocityRequestContextFactory.getJiraVelocityRequestContext().getBaseUrl();
                if (!StringUtils.startsWith(iconUrl, baseUrl))
                {
                    iconUrl = baseUrl + iconUrl;
                }
            }
        }

        final Option<String> id = option(item.getId());

        return new SimpleLinkImpl(id.getOrElse(item.getCompleteKey()), item.getLabel(), item.getTitle(), iconUrl, item.getStyleClass(),
                item.getParams(), item.getUrl(), item.getAccessKey(), item.getWeight());
    }

    @Nonnull
    public List<SimpleLinkSection> getSectionsForLocation(@Nonnull final String location, final User remoteUser, @Nonnull final JiraHelper jiraHelper)
    {
        final Iterable<WebSection> sections = webInterfaceManager.getDisplayableWebSections(location, makeContext(remoteUser, jiraHelper));

        final List<SimpleLinkSection> returnSections = new ArrayList<SimpleLinkSection>(Iterables.size(sections));
        for (final WebSection section : sections)
        {
            returnSections.add(convertWebSectionToSimpleLinkSection(section));
        }
        return returnSections;
    }

    @Nonnull
    public List<SimpleLinkSection> getNotEmptySectionsForLocation(@Nonnull String location, User remoteUser, @Nonnull JiraHelper jiraHelper)
    {
        List<SimpleLinkSection> allSections = getSectionsForLocation(location, remoteUser, jiraHelper);
        List<SimpleLinkSection> notEmptySections = new ArrayList<SimpleLinkSection>(allSections.size());
        for (SimpleLinkSection section : allSections)
        {
            final List<SimpleLink> directLinks = getLinksForSection(section.getId(), remoteUser, jiraHelper);

            if (directLinks.size() > 0)
            {
                notEmptySections.add(section);
                continue;  // trolling through all subsections could be intensive.
            }

            final List<SimpleLink> links = getLinksForSection(location + "/" + section.getId(), remoteUser, jiraHelper);

            if (links.size() > 0)
            {
                notEmptySections.add(section);
                continue;  // trolling through all subsections could be intensive.
            }

            final List<SimpleLinkSection> subSections = getNotEmptySectionsForLocation(section.getId(), remoteUser, jiraHelper);

            if (subSections.size() > 0)
            {
                notEmptySections.add(section);
            }
        }
        return notEmptySections;
    }

    // Converts JiraWebSectionModuleDescriptors to SimpleLinkSections
    private SimpleLinkSection convertWebSectionToSimpleLinkSection(final WebSection section)
    {
        return new SimpleLinkSectionImpl(section.getId(), section.getLabel(), section.getTitle(), null, section.getStyleClass(), section.getParams(), section.getWeight());
    }

    public SimpleLinkSection getSectionForURL(@Nonnull String topLevelSection, @Nonnull String URL, User remoteUser, JiraHelper jiraHelper)
    {
        return findWebSectionForURL(topLevelSection, URL, remoteUser, jiraHelper);
    }

    private SimpleLinkSection findWebSectionForURL(String currentLocation, String targetURL, User remoteUser, JiraHelper jiraHelper)
    {
        List<SimpleLinkSection> sections = getSectionsForLocation(currentLocation, remoteUser, jiraHelper);

        SimpleLinkSection sectionForURL = null;

        OuterLoop:
        for (SimpleLinkSection section : sections)
        {
            // check items
            String subSection = currentLocation + "/" + section.getId();
            List<SimpleLink> links = getLinksForSection(subSection, remoteUser, jiraHelper);
            for (SimpleLink link : links)
            {
                if (targetURL.endsWith(JiraUrl.extractActionFromURL(link.getUrl())))
                {
                    // yay!
                    sectionForURL = new SimpleLinkSectionImpl(subSection, (SimpleLinkSectionImpl) section);
                    break OuterLoop;
                }
            }

            // check subsections
            sectionForURL = findWebSectionForURL(section.getId(), targetURL, remoteUser, jiraHelper);
            if (sectionForURL != null)
            {
                break;
            }
        }

        return sectionForURL;
    }

    private boolean isRelativeUrl(String url)
    {
        try
        {
            return !(new URI(url).isAbsolute());
        }
        catch (URISyntaxException e)
        {
            // non valid URL? we assume that it's relative, so it can be prefixed
            return true;
        }
    }

    private Map<String, Object> makeContext(User remoteUser, JiraHelper jiraHelper)
    {
        final Map<String, Object> params = jiraHelper.getContextParams();
        params.put(JiraWebInterfaceManager.CONTEXT_KEY_USER, remoteUser);
        params.put(JiraWebInterfaceManager.CONTEXT_KEY_HELPER, jiraHelper);
        params.put(JiraWebInterfaceManager.CONTEXT_KEY_I18N, authenticationContext.getI18nHelper());

        return params;
    }

    // don't do String concatenation here
    private static class SectionPredicate implements Predicate<SimpleLinkFactoryModuleDescriptor>
    {
        private final String location;
        private final String key;

        private SectionPredicate(final String location, final WebSection webSection)
        {
            this.location = location;
            key = webSection.getId();
        }

        public boolean apply(final SimpleLinkFactoryModuleDescriptor linkFactory)
        {
            return linkFactory.getSection().equals(location + "/" + key) && linkFactory.shouldBeLazy();
        }

    }
}
