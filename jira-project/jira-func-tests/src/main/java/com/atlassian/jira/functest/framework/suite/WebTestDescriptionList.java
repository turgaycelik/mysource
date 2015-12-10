package com.atlassian.jira.functest.framework.suite;

import com.atlassian.jira.functest.framework.WebTestDescription;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Represents an ordered list of {@link com.atlassian.jira.functest.framework.WebTestDescription}s and provides
 * methods for advanced operations on this list.
 *
 * @since v4.4
 */
public final class WebTestDescriptionList
{
    private final List<WebTestDescription> descriptions;

    public WebTestDescriptionList(Iterable<WebTestDescription> descriptions)
    {
        this.descriptions = ImmutableList.copyOf(descriptions);
    }

    public WebTestDescriptionList(WebTestDescription... descriptions)
    {
        this(Lists.newArrayList(descriptions));
    }

    /**
     * The original list of descriptions.
     *
     * @return list of web test descriptions
     */
    public List<WebTestDescription> list()
    {
        return ImmutableList.copyOf(descriptions);
    }

    public List<WebTestDescription> singleTests()
    {
        return singleTests(descriptions);
    }

    private List<WebTestDescription> singleTests(Iterable<WebTestDescription> input)
    {
        ImmutableList.Builder<WebTestDescription> builder = ImmutableList.builder();
        for (WebTestDescription description : input)
        {
            if (description.isTest())
            {
                builder.add(description);
            }
            else
            {
                builder.addAll(singleTests(description.children()));
            }
        }
        return builder.build();
    }
}
