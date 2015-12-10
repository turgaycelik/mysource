package com.atlassian.jira.rest.v2.issue.version;

import com.atlassian.jira.rest.v2.issue.Examples;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
* @since v4.4
*/
@SuppressWarnings ( { "UnusedDeclaration" })
@XmlRootElement (name="version")
public class VersionMoveBean
{
    /**
     * A version bean instance used for auto-generated documentation.
     */
    static final VersionMoveBean DOC_EXAMPLE;
    static final VersionMoveBean DOC_EXAMPLE2;
    static
    {
        VersionMoveBean version = new VersionMoveBean();
        version.after = Examples.restURI("version/10000");

        DOC_EXAMPLE = version;

        version = new VersionMoveBean();
        version.position = Position.Earlier;

        DOC_EXAMPLE2 = version;
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
    public VersionMoveBean() {}

    /**
     * Absolute positions that a version may be moved to
     */
    public enum Position
    {
        Earlier,
        Later,
        First,
        Last;
    }
}
