package com.atlassian.jira.functest.framework;

import com.atlassian.jira.functest.framework.parser.Header;
import com.atlassian.jira.functest.framework.parser.IssueNavigatorParser;
import com.atlassian.jira.functest.framework.parser.IssueParser;
import com.atlassian.jira.functest.framework.parser.SystemInfoParser;
import com.atlassian.jira.functest.framework.parser.filter.FilterParser;

/**
 * Bucket for page/domain parsers
 *
 * @since v3.13
 */
public interface Parser
{

    /**
     * Get the issue related parser
     *
     * @return The issue related parser 
     */
    IssueParser issue();

    /**
     * Get the parser for filters
     *
     * @return the parser for filters
     */
    FilterParser filter();

    /**
     * Get the parser that can read the JIRA System Info
     *
     * @return the parser of System Info
     */
    SystemInfoParser systemInfo();

    /**
     * Get a parser for the issue navigator.
     *
     * @return an object that can parse the settings of a search in the issue navigator.
     */
    IssueNavigatorParser navigatorParser();

    /**
     * Return an object that can parse the header.
     *
     * @return an object to parse the header.
     */
    Header header();
}
