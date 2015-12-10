package com.atlassian.jira.webtests.ztests.upgrade.tasks;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.ServicesControl;
import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;

import java.util.List;

import static com.atlassian.jira.functest.framework.suite.Category.FUNC_TEST;
import static com.atlassian.jira.functest.framework.suite.Category.UPGRADE_TASKS;
import static org.hamcrest.CoreMatchers.hasItem;

/**
 * Ensure Audit log cleaning service is created
 *
 * @since v6.3
 */
@WebTest ({ FUNC_TEST, UPGRADE_TASKS })
public class TestUpgradeTask6327 extends FuncTestCase
{

    public void testAuditLogCleanerAdded()
    {
        administration.restoreData("TestUpgradeTask6327.xml");
        final List<ServicesControl.ServiceBean> services = backdoor.services().getServices();
        final Matcher<Iterable<? super ServicesControl.ServiceBean>> hasItemService = hasItem(service("Audit log cleaning service"));
        Assert.assertThat(services, hasItemService);

    }

    private BaseMatcher<ServicesControl.ServiceBean> service(final String name)
    {
        return new BaseMatcher<ServicesControl.ServiceBean>()
        {
            @Override
            public boolean matches(final Object item)
            {
                return item instanceof ServicesControl.ServiceBean
                        && ((ServicesControl.ServiceBean) item).name.equals(name);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("Service named:").appendValue(name);
            }
        };
    }
}
