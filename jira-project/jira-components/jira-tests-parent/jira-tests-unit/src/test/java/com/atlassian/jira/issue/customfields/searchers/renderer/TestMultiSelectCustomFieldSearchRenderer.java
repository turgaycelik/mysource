package com.atlassian.jira.issue.customfields.searchers.renderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.atlassian.jira.action.issue.customfields.option.MockOption;
import com.atlassian.jira.issue.customfields.option.Option;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestMultiSelectCustomFieldSearchRenderer
{
    @Test
    public void testOptionsComparatorSortsByOptionValue()
    {
        final Option firstOption = MockOption._getMockChild1Option();
        final Option secondOption = MockOption._getMockChild2Option();
        List<Option> optionSet = new ArrayList<Option>();
        optionSet.add(secondOption);
        optionSet.add(firstOption);

        Collections.sort(optionSet, MultiSelectCustomFieldSearchRenderer.OPTION_VALUE_COMPARATOR);
        assertEquals(firstOption, optionSet.get(0));
        assertEquals(secondOption, optionSet.get(1));
    }

    @Test
    public void testOptionsComparatorWorksWithNullOptions()
    {
        final Option o1 = null;
        final Option o2 = null;
        List<Option> optionSet = new ArrayList<Option>();
        optionSet.add(o2);
        optionSet.add(o1);

        Collections.sort(optionSet, MultiSelectCustomFieldSearchRenderer.OPTION_VALUE_COMPARATOR);
        assertEquals(o2, optionSet.get(0));
        assertEquals(o1, optionSet.get(1));
    }

    @Test
    public void testOptionsComparatorSendsNullOptionsToEndOfList()
    {
        final Option o1 = null;
        final Option o2 = MockOption._getMockChild1Option();;
        final Option o3 = null;
        List<Option> optionSet = new ArrayList<Option>();
        optionSet.add(o1);
        optionSet.add(o2);
        optionSet.add(o3);

        Collections.sort(optionSet, MultiSelectCustomFieldSearchRenderer.OPTION_VALUE_COMPARATOR);
        assertEquals(o2, optionSet.get(0));
    }
}