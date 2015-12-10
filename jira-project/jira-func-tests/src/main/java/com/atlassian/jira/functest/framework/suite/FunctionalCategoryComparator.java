package com.atlassian.jira.functest.framework.suite;

import java.util.Comparator;

/**
 * Compares tests using their functional categories. Useful for keeping consistent ordering across the whole suite.
 *
 * @since v4.3
 */
public class FunctionalCategoryComparator implements Comparator<Class<?>>
{

    public static final FunctionalCategoryComparator INSTANCE = new FunctionalCategoryComparator();

    @Override
    public int compare(Class<?> test1, Class<?> test2)
    {
        final Category category1 = getCategory(test1);
        final Category category2 = getCategory(test2);
        if (category1 == null && category2 == null)
        {
            return compareByName(test1,test2);
        }
        if (category1 == null)
        {
            // non-functional test classes come last
            return 1;
        }
        if (category2 == null)
        {
            // non-functional test classes come last
            return -1;
        }
        // let enum ordinal decide
        int result = category1.compareTo(category2);
        return result != 0 ? result : compareByName(test1, test2);
    }

    private Category getCategory(Class<?> test1)
    {
        WebTest webTest = test1.getAnnotation(WebTest.class);
        if (webTest == null)
        {
            return null;
        }
        for (Category category : Category.fromAnnotation(webTest))
        {
            if (category.isFunctional())
            {
                return category;
            }
        }
        return null;
    }

    private int compareByName(Class<?> test1, Class<?> test2)
    {
        return test1.getSimpleName().compareTo(test2.getSimpleName());
    }
}
