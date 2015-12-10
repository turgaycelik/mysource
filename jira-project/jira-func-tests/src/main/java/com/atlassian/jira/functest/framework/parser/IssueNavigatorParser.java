package com.atlassian.jira.functest.framework.parser;

import com.atlassian.jira.functest.framework.navigator.NavigatorSearch;
import net.sourceforge.jwebunit.WebTester;

import java.util.Collection;

/**
 * Parse the Issue Navigator settings to return a.
 *
 * @since v3.13
 */

public interface IssueNavigatorParser
{
    /**
     * Parse out the issue navigator settings from the Navigator search page.
     *
     * @param tester the we client pointed to the navigation page.
     * @param conditions the conditions to parse out.
     *
     * @return the parsed out search.
     */
    NavigatorSearch parseSettings(WebTester tester, Collection /*<NavigatorCondition>*/ conditions);
}
