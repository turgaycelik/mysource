package com.atlassian.jira.rest.v2.issue;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlElement;
import java.net.URI;

@SuppressWarnings ({ "UnusedDeclaration" })
public class MoveFieldBean
{
    final static MoveFieldBean DOC_EXAMPLE;

    static {
        DOC_EXAMPLE = new MoveFieldBean();
        DOC_EXAMPLE.position = MoveFieldBean.Position.First;
    }

    @XmlElement
    public URI after;

    @XmlElement
    public Position position;

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    //Needed so that JAXB works.
    public MoveFieldBean()
    {}

    /**
     * Absolute positions that a version may be moved to
     */
    public enum Position
    {
        Earlier,
        Later,
        First,
        Last
    }

}