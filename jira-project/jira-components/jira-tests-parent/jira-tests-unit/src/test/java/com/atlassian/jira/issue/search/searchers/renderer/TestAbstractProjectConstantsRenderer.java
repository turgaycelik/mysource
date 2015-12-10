package com.atlassian.jira.issue.search.searchers.renderer;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.option.GroupTextOption;
import com.atlassian.jira.issue.fields.option.Option;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInput;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.issue.search.MockSearchContext;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.junit.Before;
import org.mockito.Mock;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public abstract class TestAbstractProjectConstantsRenderer<I extends SearchInput, O extends Options>
{
    final String urlParameter;

    @Mock
    ComponentManager componentManager;
    @Mock
    ProjectManager projectManager;
    @Mock
    FieldVisibilityManager fieldVisibilityManager;
    @Mock
    VelocityRequestContextFactory velocityRequestContextFactory;
    @Mock
    PermissionManager permissionManager;

    AbstractProjectConstantsRenderer<I, O> renderer;

    TestAbstractProjectConstantsRenderer(String urlParameter)
    {
        this.urlParameter = urlParameter;
    }

    @Before
    public void setUp()
    {
        MockComponentWorker componentWorker = new MockComponentWorker();
        componentWorker.registerMock(I18nHelper.class, new MockI18nHelper());
        componentWorker.registerMock(I18nHelper.BeanFactory.class, new MockI18nHelper().factory());
        ComponentAccessor.initialiseWorker(componentWorker);
    }

    @SuppressWarnings("unchecked")
    void test(SearchContext context, List<I> inputValues, List<String> expectedSelectedValues, List<Option> expectedValidOptions,
            List<Option> expectedInvalidOptions, List<String> expectedValidLabels, List<String> expectedInvalidLabels)
    {
        test(context, inputValues, expectedSelectedValues, expectedValidOptions, asList(""), expectedInvalidOptions, expectedValidLabels, expectedInvalidLabels);
    }

    @SuppressWarnings("unchecked")
    void test(SearchContext context, List<I> inputValues, List<String> expectedSelectedValues, List<Option> expectedValidOptions,
            List<String> validOptionGroupIds, List<Option> expectedInvalidOptions, List<String> expectedValidLabels, List<String> expectedInvalidLabels)
    {
        FieldValuesHolder fieldValuesHolder = new FieldValuesHolderImpl();
        if (inputValues != null)
        {
            fieldValuesHolder.put(urlParameter, inputValues.toArray());
        }

        // Edit
        Map<String, Object> results = Maps.newHashMap();

        renderer.addEditParameters(null, context, fieldValuesHolder, results);
        SelectedValues selectedValues = (SelectedValues) results.get("selectedValues");
        assertEquals(new SelectedValues(expectedSelectedValues), selectedValues);

        List<Option> resultValidOptions = getValidOptions(results, validOptionGroupIds);
        assertEquals(Sets.newHashSet(expectedValidOptions), Sets.newHashSet(resultValidOptions));

        List<Option> resultInvalidOptions = getInvalidOptions(results);
        if (expectedInvalidOptions == null)
        {
            assertNull(resultInvalidOptions);
        }
        else
        {
            assertEquals(Sets.newHashSet(expectedInvalidOptions), Sets.newHashSet(resultInvalidOptions));
        }

        // View
        renderer.addViewParameters(null, context, fieldValuesHolder, results);

        Collection<AbstractProjectConstantsRenderer.GenericProjectConstantsLabel> labels = (Collection<AbstractProjectConstantsRenderer.GenericProjectConstantsLabel>) results.get("selectedObjects");
        assertNotNull(labels);

        if (expectedSelectedValues == null)
        {
            assertTrue(labels.isEmpty());
        }
        else
        {
            assertEquals(expectedSelectedValues.size(), labels.size());

            for (AbstractProjectConstantsRenderer.GenericProjectConstantsLabel label : labels)
            {
                if (expectedValidLabels != null && expectedValidLabels.contains(label.getLabel()))
                {
                    assertTrue(label.isValid());
                }
                else if (expectedInvalidLabels != null && expectedInvalidLabels.contains(label.getLabel()))
                {
                    assertFalse(label.isValid());
                }
                else
                {
                    fail("Unexpected label " + label.getLabel());
                }
            }
        }
    }

    void testEmptyOptions(MockProject... projects)
    {
        test(new MockSearchContext(projects),
                null,
                null,
                Collections.<Option>emptyList(),
                null,
                Collections.<String>emptyList(),
                null);
    }

    static String createId(String value)
    {
        return "id:" + value;
    }

    @SuppressWarnings("unchecked")
    private List<Option> getValidOptions(Map<String, Object> results, Collection<String> groupIds)
    {
        List<Option> validOptions = Lists.newArrayList();

        mainLoop:
        for (GroupTextOption group : (List<GroupTextOption>)results.get("optionGroups"))
        {
            if (groupIds.contains(group.getId()))
            {
                List<Option> childOptions = group.getChildOptions();

                for (Option option : childOptions)
                {
                    for (String specialId : getSpecialOptionIds())
                    {
                        if (specialId.equals(option.getId()))
                        {
                            continue mainLoop;
                        }
                    }
                }

                for (Option option : childOptions)
                {
                    OptionWithValidity optionWithValidity = (OptionWithValidity) option;
                    assertTrue(optionWithValidity.isValid());
                }

                validOptions.addAll(childOptions);
            }
        }

        return validOptions;
    }

    abstract String[] getSpecialOptionIds();

    @SuppressWarnings("unchecked")
    private List<Option> getInvalidOptions(Map<String, Object> results)
    {
        for (GroupTextOption group : (List<GroupTextOption>)results.get("optionGroups"))
        {
            if ("invalid".equals(group.getId()))
            {
                List<Option> childOptions = group.getChildOptions();

                for (Option option : childOptions)
                {
                    OptionWithValidity optionWithValidity = (OptionWithValidity) option;
                    assertFalse(optionWithValidity.isValid());
                }

                return childOptions;
            }
        }
        return null;
    }
}
