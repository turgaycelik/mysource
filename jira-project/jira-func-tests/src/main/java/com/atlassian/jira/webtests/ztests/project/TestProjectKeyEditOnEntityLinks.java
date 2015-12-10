package com.atlassian.jira.webtests.ztests.project;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.beans.EntityRefBean;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

/**
 * @since v6.1
 */
@WebTest ({ Category.FUNC_TEST, Category.APP_LINKS, Category.PROJECTS })
public class TestProjectKeyEditOnEntityLinks extends FuncTestCase
{

    public void testCanSearchProjectByHistoricalKeys()
    {
        administration.restoreData("TestProjectKeyEditOnEntityLinks.xml");

        final long projectId = backdoor.project().getProjectId("ABC");
        backdoor.project().editProjectKey(projectId, "TST");

        final ArrayList<EntityRefBean> entities = backdoor.project().getEntityLinks(projectId).entities;

        assertThat(entities.size(), equalTo(3));
        final Matcher<Iterable<EntityRefBean>> hasItems = hasItems(entity("TT", "Jira TEST", "JiraApplicationType"),
                entity("CR-TEST", "Crucible TEST", "FishEyeCrucibleApplicationType"),
                entity("TEST", "FishEye TEST", "FishEyeCrucibleApplicationType"));
        assertThat(entities, hasItems);
    }

    private Matcher<EntityRefBean> entity(final String key, final String name, final String type)
    {
        return new BaseMatcher<EntityRefBean>()
        {
            @Override
            public boolean matches(final Object item)
            {
                if(!(item instanceof EntityRefBean))
                {
                    return false;
                }
                EntityRefBean entity = (EntityRefBean) item;

                return entity.key.equals(key) && entity.name.equals(name) && entity.type.applicationTypeClassName.contains(type);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText(String.format("entity with key: %s, name: %s, type: %s", key, name, type));
            }
        };
    }

}
