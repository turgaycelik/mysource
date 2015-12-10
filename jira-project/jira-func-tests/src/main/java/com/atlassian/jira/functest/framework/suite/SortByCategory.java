package com.atlassian.jira.functest.framework.suite;

import com.google.common.collect.Lists;
import org.junit.runner.Description;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.stateTrue;

/**
 * Transform that sorts tests by category.
 *
 * @since v4.4
 */
public class SortByCategory implements SuiteTransform
{
    public static final SortByCategory INSTANCE = new SortByCategory();

    @Override
    public Iterable<Description> apply(@Nullable Iterable<Description> input)
    {
        List<Description> answer = Lists.newArrayList(input);
        Collections.sort(answer, CategoryComparator.INSTANCE);
        return answer;
    }


    private static class CategoryComparator implements Comparator<Description>
    {
        private static final CategoryComparator INSTANCE = new CategoryComparator();


        @Override
        public int compare(Description first, Description second)
        {
            final Category category1 = getCategory(first);
            final Category category2 = getCategory(second);
            if (category1 == null && category2 == null)
            {
                return compareByName(first,second);
            }
            if (category1 == null)
            {
                // non-functional tests come last
                return 1;
            }
            if (category2 == null)
            {
                // non-functional tests come last
                return -1;
            }
            // let enum ordinal decide
            int result = category1.compareTo(category2);
            return result != 0 ? result : compareByName(first, second);
        }

        private Category getCategory(Description description)
        {
            WebTest webTest = description.getAnnotation(WebTest.class);
            if (webTest == null)
            {
                if (description.isTest())
                {
                    return getCategory(Description.createSuiteDescription(description.getTestClass()));
                }
                else
                {
                    return null;
                }
            }
            stateTrue("@WebTest for " + description + " does not have any categories specified", webTest.value().length > 0);
            for (Category category : Category.fromAnnotation(webTest))
            {
                if (category.isFunctional())
                {
                    return category;
                }
            }
            return null;
        }

        private int compareByName(Description one, Description two)
        {
            String className1 = one.getClassName();
            String className2 = two.getClassName();
            if (className1 == null && className2 == null)
            {
                // 'suites' not bound to a class - compare by display name
                return one.getDisplayName().compareTo(two.getDisplayName());
            }
            if (className1 == null)
            {
                // non-class (higher-level) suites come first
                return -1;
            }
            if (className2 == null)
            {
                // non-class (higher-level) suites come first
                return 1;
            }
            int result = className1.compareTo(className2);
            return result != 0 ? result : compareByMethodName(one, two);
        }

        private int compareByMethodName(Description one, Description two)
        {
            final String method1 = one.getMethodName();
            final String method2 = two.getMethodName();
            if (method1 == null && method2 == null)
            {
                // two test class suites with the same name
                return 0;
            }
            if (method1 == null)
            {
                // class suites come first before single tests
                return -1;
            }
            if (method2 == null)
            {
                // class suites come first before single tests
                return 1;
            }
            return method1.compareTo(method2);
        }


    }
}
