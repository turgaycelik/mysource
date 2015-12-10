package com.atlassian.jira.plugin.link.confluence.rest;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * Bean to represent a Confluence getSpaces result.
 *
 * @since v5.0
 */
@SuppressWarnings ( { "UnusedDeclaration", "FieldCanBeLocal" })
public class ConfluenceSpaceResponseBean
{
    @XmlElement
    private List<Space> spaces;

    public ConfluenceSpaceResponseBean(final List<Space> spaces)
    {
        this.spaces = spaces;
    }

    public static class Space
    {
        @XmlElement
        private String key;

        @XmlElement
        private String name;

        @XmlElement
        private String type;

        @XmlElement
        private String url;

        public Space(final String key, final String name, final String type, final String url)
        {
            this.key = key;
            this.name = name;
            this.type = type;
            this.url = url;
        }
    }
}
