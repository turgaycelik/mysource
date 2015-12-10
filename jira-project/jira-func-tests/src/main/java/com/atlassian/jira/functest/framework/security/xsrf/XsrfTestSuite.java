package com.atlassian.jira.functest.framework.security.xsrf;

import com.atlassian.jira.functest.framework.FuncTestHelperFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * XsrfCheck test suite class that runs multiple checks in sequence.
 *
 * @since v4.1
 */
public class XsrfTestSuite
{
    private List<XsrfCheck> checks = new ArrayList<XsrfCheck>();

    public XsrfTestSuite(Collection<XsrfCheck> checks)
    {
        if (checks == null || checks.isEmpty())
        {
            throw new IllegalArgumentException("Please pass in at least one XsrfCheck");
        }
        this.checks.addAll(checks);
    }

    public XsrfTestSuite(XsrfCheck... checks)
    {
        if (checks == null || checks.length == 0)
        {
            throw new IllegalArgumentException("Please pass in at least one XsrfCheck");
        }
        this.checks.addAll(Arrays.asList(checks));
    }

    public void run(final FuncTestHelperFactory funcTestHelperFactory) throws Exception
    {
        for (XsrfCheck check : checks)
        {
            check.init(funcTestHelperFactory);
            check.run();
        }
    }

    public void run(final FuncTestHelperFactory funcTestHelperFactory, String xsrfError) throws Exception
    {
        for (XsrfCheck check : checks)
        {
            check.init(funcTestHelperFactory);
            check.run(xsrfError);
        }
    }
}
