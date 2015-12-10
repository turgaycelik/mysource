package com.atlassian.jira.plugin.headernav.customcontentlinks.admin;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

@SuppressWarnings ({ "UnusedDeclaration" })
@XmlRootElement (name = "version")
public class MoveBean
{

    @XmlElement
    public URI after;

    @XmlElement
    public Position position;


    //Needed so that JAXB works.
    public MoveBean()
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