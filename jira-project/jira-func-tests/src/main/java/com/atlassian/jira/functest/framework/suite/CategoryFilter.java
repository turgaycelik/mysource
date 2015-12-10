package com.atlassian.jira.functest.framework.suite;

import org.hamcrest.StringDescription;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 * <p/>
 * A Junit4 filter that filters given set of tests by the included and excluded web test categories.
 *
 * <p/>
 * NOTE: shamelessly ripped off from the {@link org.junit.experimental.categories.Categories} code. Sorry for that Kent!
 *
 * @since v4.4
 */
public class CategoryFilter extends Filter
{
    private final Set<Category> included;
    private final Set<Category> excluded;

    public CategoryFilter(Set<Category> includedCategories, Set<Category> excludedCategories)
    {
        included = includedCategories.size() > 0 ? EnumSet.copyOf(includedCategories) : EnumSet.noneOf(Category.class);
        excluded = excludedCategories.size() > 0 ? EnumSet.copyOf(excludedCategories) : EnumSet.noneOf(Category.class);
    }

    @Override
    public String describe()
    {
        return new StringDescription().appendText("Included categories ").appendValue(included)
                .appendText("\nExcluded categories").appendValue(excluded).toString();
    }

    @Override
    public boolean shouldRun(Description description)
    {
        if (hasCorrectCategoryAnnotation(description))
        {
            return true;
        }
        for (Description each : description.getChildren())
        {
            if (shouldRun(each))
            {
                return true;
            }
        }
        return false;
    }

    private boolean hasCorrectCategoryAnnotation(Description description)
    {
        Set<Category> categories = categories(description);
        if (categories.isEmpty())
        {
            return included == null;
        }
        for (Category each : categories)
        {
            if (excluded != null && excluded.contains(each))
            {
                return false;
            }
        }
        for (Category each : categories)
        {
            if (included == null || included.contains(each))
            {
                return true;
            }
        }
        return false;
    }

    private Set<Category> categories(Description description)
    {
        EnumSet<Category> answer = EnumSet.noneOf(Category.class);
        answer.addAll(directCategories(description));
        if (description.isTest())
        {
            answer.addAll(directCategories(parentDescription(description)));
        }
        return answer;
    }

    private Description parentDescription(Description description)
    {
        return Description.createSuiteDescription(description.getTestClass());
    }

    private Set<Category> directCategories(Description description)
    {
        WebTest annotation = description.getAnnotation(WebTest.class);
        if (annotation == null || annotation.value().length == 0)
        {
            return EnumSet.noneOf(Category.class);
        }
        return EnumSet.copyOf(Arrays.asList(annotation.value()));
    }
}
