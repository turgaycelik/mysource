package com.atlassian.jira.plugin.link.confluence;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.application.confluence.ConfluenceApplicationType;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.util.UriMatcher.isBaseEqual;

/**
 * Represents the Applications Links pointing to Confluence instances from this JIRA instance.
 */
@Component
public class ConfluenceApplicationLinks
{
    private final ApplicationLinkService applicationLinkService;

    @Autowired
    public ConfluenceApplicationLinks(@ComponentImport final ApplicationLinkService applicationLinkService)
    {
        this.applicationLinkService = applicationLinkService;
    }

    /**
     * Returns all the application links that point to a Confluence instance.
     * @return The application links that point to a Confluence instance.
     */
    public Collection<ApplicationLink> getAppLinks()
    {
        return ImmutableList.copyOf(applicationLinkService.getApplicationLinks(ConfluenceApplicationType.class));
    }

    /**
     * Returns the {@link ApplicationLink} that corresponds to the given Confluence page uri.
     * @param pageUri The uri for the Confluence page.
     * @return The corresponding ApplicationLink for the given page uri, if there is any.
     */
    public Option<ApplicationLink> forPage(final URI pageUri)
    {
        List<ApplicationLink> candidateAppLinks = getCandidateAppLinksForPage(pageUri);
        if (candidateAppLinks.isEmpty())
        {
            return Option.none();
        }

        for (ApplicationLink matchingAppLink : candidateAppLinks)
        {
            if (matchingAppLink.isPrimary())
            {
                return Option.some(matchingAppLink);
            }
        }
        return Option.some(candidateAppLinks.get(0));
    }

    private List<ApplicationLink> getCandidateAppLinksForPage(final URI pageUri)
    {
        List<ApplicationLink> matchingAppLinks = new ArrayList<ApplicationLink>();
        for (final ApplicationLink appLink : getAppLinks())
        {
            if (isBaseEqual(appLink.getDisplayUrl(), pageUri) || isBaseEqual(appLink.getRpcUrl(), pageUri))
            {
                matchingAppLinks.add(appLink);
            }
        }
        return matchingAppLinks;
    }
}
