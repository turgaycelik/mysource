package com.atlassian.jira.functest.config;

/**
 * Class that can be used to parse and manipulate the SequenceValueItem from the passed JIRA xml.
 *
 * @since v4.0
 */
public interface ConfigSequence
{
    Long getNextId(String elementType);
    boolean save();
}
