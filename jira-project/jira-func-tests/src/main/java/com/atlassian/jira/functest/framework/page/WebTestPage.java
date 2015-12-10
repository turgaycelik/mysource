package com.atlassian.jira.functest.framework.page;

import com.atlassian.jira.functest.framework.FuncTestHelperFactory;

/**
 * @since v6.0
 */
public interface WebTestPage
{
    String baseUrl();

    void setContext(FuncTestHelperFactory funcTestHelperFactory);
}
