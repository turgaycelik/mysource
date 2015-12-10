package com.atlassian.jira.plugin.link.confluence.rest;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * Bean to represent Confluence search results.
 *
 * @since v5.0
 */
@SuppressWarnings ( { "UnusedDeclaration", "FieldCanBeLocal" })
public class ConfluenceSearchResponseBean
{
    @XmlElement
    private List<Result> result;

    public ConfluenceSearchResponseBean(final List<Result> result)
    {
        this.result = result;
    }

    public static class Result
    {
        @XmlElement
        private String id;

        @XmlElement
        private String type;

        @XmlElement
        private String title;

        @XmlElement
        private String excerpt;

        @XmlElement
        private String url;

        public Result(final String id, final String type, final String title, final String excerpt, final String url)
        {
            this.id = id;
            this.type = type;
            this.title = title;
            this.excerpt = excerpt;
            this.url = url;
        }
    }
}
