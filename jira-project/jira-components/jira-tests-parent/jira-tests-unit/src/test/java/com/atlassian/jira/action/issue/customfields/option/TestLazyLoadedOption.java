package com.atlassian.jira.action.issue.customfields.option;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.LazyLoadedOption;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;

import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestLazyLoadedOption
{
    private final GenericValue parentOption;
    private final OptionsManager mockOptionManager;
    private final Map<Long, GenericValue> options;
    private final MockGenericValue child1;
    private final MockGenericValue child2;

    public TestLazyLoadedOption()
    {
        parentOption = new MockGenericValue("CustomFieldOption", EasyMap.build("id", new Long(1), "value", "cars", "sequence", new Long(0)));
        child1 = new MockGenericValue("CustomFieldOption", EasyMap.build("id", new Long(2), "value", "ford", "parentoptionid", new Long(1),
            "sequence", new Long(0)));
        child2 = new MockGenericValue("CustomFieldOption", EasyMap.build("id", new Long(3), "parentoptionid", new Long(1), "value", "holden",
            "sequence", new Long(1)));
        options = new HashMap<Long, GenericValue>();
        options.put(new Long(1), parentOption);
        options.put(new Long(2), child1);
        options.put(new Long(3), child2);
        mockOptionManager = new MockOptionManager(options);
    }

    @Test
    public void testOptionsPopulatesBasicValues()
    {
        final Option option = new LazyLoadedOption(parentOption, mockOptionManager, null);

        assertEquals("cars", option.getValue());
        assertEquals(new Long(1), option.getOptionId());
        assertEquals(new Long(0), option.getSequence());

        option.setSequence(new Long(5));
        assertEquals(new Long(5), option.getSequence());

        assertNull(option.getParentOption());
    }

    @Test
    public void testChildPopulation()
    {
        final Option option = new LazyLoadedOption(parentOption, mockOptionManager, null);
        assertTrue(option.getChildOptions().size() == 2);
    }

    @Test
    public void testParentPopulation()
    {
        final Option option = new LazyLoadedOption(parentOption, mockOptionManager, null);

        assertNull(option.getParentOption());

        final Option child = new LazyLoadedOption(child1, mockOptionManager, null);

        assertNotNull(child.getParentOption());
        assertEquals(child.getParentOption(), option);
    }
}
