package com.atlassian.jira.plugin.viewissue.issuelink.rest;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
/**
 * Bean to represent issue link types, which are {@link SimpleLink}s.
 *
 * @since v5.0
 */
@SuppressWarnings ( { "UnusedDeclaration", "FieldCanBeLocal" })
@XmlRootElement (name = "linkType")
// TODO this name might be confusing with relationship link types
public class LinkTypeBean
{
    @XmlElement
    private String id;

    @XmlElement
    private String label;

    @XmlElement
    private String url;

    @XmlElement
    private String focusedFieldName;

    public LinkTypeBean(final String id, final String label, final String url, String focusedFieldName)
    {
        this.id = id;
        this.label = label;
        this.url = url;
        this.focusedFieldName = focusedFieldName;
    }
}
