package com.atlassian.jira.external.beans;

/**
 * Represents an IssueLinkType object.
 * <p>
 * These look like the following in XML:
 * <pre>
 *    &lt;IssueLinkType id="10000" linkname="Duplicate" inward="is duplicated by" outward="duplicates"/&gt;
 *    &lt;IssueLinkType id="10001" linkname="jira_subtask_link" inward="jira_subtask_inward" outward="jira_subtask_outward" style="jira_subtask"/&gt;
 * </pre>
 *
 * @since v3.13
 */
public class ExternalIssueLinkType
{
    private String id;
    private String linkname;
    private String style;

    public String getId()
    {
        return id;
    }

    public void setId(final String id)
    {
        this.id = id;
    }

    public String getLinkname()
    {
        return linkname;
    }

    public void setLinkname(final String linkname)
    {
        this.linkname = linkname;
    }

    public String getStyle()
    {
        return style;
    }

    public void setStyle(final String style)
    {
        this.style = style;
    }
}
