package com.atlassian.jira.external.beans;

public class ExternalLink
{
    private String id;
    private String linkName;
    private String linkType;
    private String sourceId;
    private String destinationId;
    private String sequence;

    public String getId()
    {
        return id;
    }

    public void setId(final String id)
    {
        this.id = id;
    }

    /**
     * Returns the link type ID of this link.
     * Imports from non-JIRA systems would not have a link type ID that makes sense, and so would use the link name instead.
     *
     * @return the link type ID of this link.
     * @see #getLinkName() 
     */
    public String getLinkType()
    {
        return linkType;
    }

    public void setLinkType(final String linkType)
    {
        this.linkType = linkType;
    }

    /**
     * Returns the "link name" of this link's link type.
     * This is used for imports from non-JIRA systems where we just have a link name.
     * The import will later find or create an appropriate "link type" for this name.
     * An import from a JIRA backup would use LinkType instead.
     *
     * @return the "link name" of this link's link type.
     * @see #getLinkType()
     */
    public String getLinkName()
    {
        return linkName;
    }

    public void setLinkName(String linkName)
    {
        this.linkName = linkName;
    }

    public String getSourceId()
    {
        return sourceId;
    }

    public void setSourceId(String sourceId)
    {
        this.sourceId = sourceId;
    }

    public String getDestinationId()
    {
        return destinationId;
    }

    public void setDestinationId(String destinationId)
    {
        this.destinationId = destinationId;
    }

    public String getSequence()
    {
        return sequence;
    }

    public void setSequence(final String sequence)
    {
        this.sequence = sequence;
    }
}
