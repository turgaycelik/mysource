package com.atlassian.jira.plugin.link.confluence.rest;

import com.atlassian.applinks.api.ApplicationLink;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean to represent a list of Confluence Application Links.
 *
 * @since v5.0
 */
@SuppressWarnings ( { "UnusedDeclaration", "FieldCanBeLocal" })
public class ConfluenceApplicationLinksBean
{
    @XmlElement
    private List<ConfluenceApplicationLinkBean> applicationLinks;

    public ConfluenceApplicationLinksBean(final Iterable<ApplicationLink> appLinks)
    {
        applicationLinks = new ArrayList<ConfluenceApplicationLinkBean>();
        for (final ApplicationLink appLink : appLinks)
        {
            applicationLinks.add(new ConfluenceApplicationLinkBean(appLink.getId().get(), appLink.getName()));
        }
    }

    public static class ConfluenceApplicationLinkBean
    {
        @XmlElement
        private String id;

        @XmlElement
        private String name;

        public ConfluenceApplicationLinkBean(final String id, final String name)
        {
            this.id = id;
            this.name = name;
        }
    }
}
