package com.atlassian.jira.functest.framework.suite;

import org.apache.commons.lang.StringUtils;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A {@link com.atlassian.jira.functest.framework.suite.WebTestSuite} that reads its configuration from system properties.
 *
 * @since v4.4
 */
@RunWith(WebTestSuiteRunner.class)
public class SystemPropertyBasedSuite implements WebTestSuite
{
    private static final String PACKAGE_PROPERTY_NAME = "atlassian.test.suite.package";
    private static final String INCLUDES_PROPERTY_NAME = "atlassian.test.suite.includes";
    private static final String EXCLUDES_PROPERTY_NAME = "atlassian.test.suite.excludes";

    private final String packageName;
    private final Set<Category> includes;
    private final Set<Category> excludes;

    public SystemPropertyBasedSuite()
    {
        packageName = notNull(System.getProperty(PACKAGE_PROPERTY_NAME)).trim();
        includes = toCategories(INCLUDES_PROPERTY_NAME);
        excludes = toCategories(EXCLUDES_PROPERTY_NAME);
    }

    @Override
    public String webTestPackage()
    {
        return packageName;
    }

    @Override
    public Set<Category> includes()
    {
        return includes;
    }

    @Override
    public Set<Category> excludes()
    {
        return excludes;
    }

    private Set<Category> toCategories(String propName)
    {
        EnumSet<Category> answer = EnumSet.noneOf(Category.class);
        for (String each : splitList(System.getProperty(propName)))
        {
            answer.add(Category.forString(each));
        }
        return answer;
    }

    private List<String> splitList(String list)
    {
        if (list == null)
        {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<String>();
        for (String each : Arrays.asList(list.split(",")))
        {
            final String trimmed = each.trim();
            if (StringUtils.isNotBlank(trimmed))
            {
                result.add(trimmed);
            }
        }
        return result;
    }
}
