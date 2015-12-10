package com.atlassian.jira.pageobjects.config.junit4.rule;

import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.webdriver.testing.rule.SessionCleanupRule;
import com.atlassian.webdriver.testing.rule.WindowSizeRule;
import org.junit.rules.RuleChain;

/**
 * <p/>
 * Default set of rules that can be used in plugin tests. Those should work regardless of whether the func-test plugin
 * is installed, or not.
 *
 * <p/>
 * The set includes (in order):
 * <ul>
 *     <li><tt>WindowSizeRule</tt></li>
 *     TODO
 * </ul>
 *
 * @since 5.1
 */
public final class WebTestRules
{
    private WebTestRules()
    {
        throw new AssertionError("Don't instantiate me");
    }

    public static RuleChain forProduct(TestedProduct<?> product)
    {
        return RuleChainBuilder.forProduct(product)
                .around(WindowSizeRule.class)
                .around(SessionCleanupRule.class)
                .around(RestoreDataMethodRule.class)
                .around(WebSudoRule.class)
                .around(DirtyWarningTerminatorRule.class)
                .build();
    }
}
