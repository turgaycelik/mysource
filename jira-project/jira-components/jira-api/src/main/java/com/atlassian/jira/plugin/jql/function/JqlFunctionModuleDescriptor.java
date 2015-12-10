package com.atlassian.jira.plugin.jql.function;

import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;

/**
 * A module descriptor for a JQL function handler that produces a {@link JqlFunction}.
 *
 * The following is an example XML descriptor for a JQL function:
 *
 * <pre>
 *
 * &lt;jql-function key="now-jql-function"
 *          i18n-name-key="jql.function.plugin.now.name"
 *          name="Now Function"
 *          class="com.atlassian.jira.plugin.jql.function.NowFunction"
 *          &gt;
 *      &lt;description key="jql.function.plugin.now.desc"&gt;Returns the current system time.&lt;/description&gt;
 *      &lt;fname&gt;now&lt;/fname&gt;
 *      &lt;list&gt;false&lt;/list&gt;
 * &lt;/jql-function&gt;
 *
 * </pre>
 *
 * The <tt>fname</tt> element specifies the name of the function. The <tt>list</tt> element specifies whether this
 * function returns a list of values or a single value. If omitted, the
 * default is <tt>false</tt>.
 *
 * @since v4.0
 */
public interface JqlFunctionModuleDescriptor extends JiraResourcedModuleDescriptor<JqlFunction>
{
    public String getFunctionName();

    public boolean isList();
}
