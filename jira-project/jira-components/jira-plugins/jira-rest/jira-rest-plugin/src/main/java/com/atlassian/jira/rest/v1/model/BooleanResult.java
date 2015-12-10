package com.atlassian.jira.rest.v1.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB bean to return a boolean result.
 *
 * @since v4.0
 */
@XmlRootElement
public class BooleanResult
{
    @XmlElement
    private boolean value;

    @SuppressWarnings({"UnusedDeclaration", "unused"})
    private BooleanResult() {}

    public BooleanResult(final boolean theBoolean)
    {
        this.value = theBoolean;
    }

    public boolean isValue()
    {
        return value;
    }
}