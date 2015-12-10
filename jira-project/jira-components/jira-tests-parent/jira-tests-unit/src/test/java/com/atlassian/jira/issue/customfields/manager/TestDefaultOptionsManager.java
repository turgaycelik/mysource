package com.atlassian.jira.issue.customfields.manager;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.jira.action.issue.customfields.option.MockOption;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.config.FieldConfigImpl;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.CollectionReorderer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericValue;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDefaultOptionsManager
{
    @Mock private OfBizDelegator ofBizDelegator;
    @Mock private CollectionReorderer<Option> collectionReorderer;
    @Mock private FieldConfigManager fieldConfigManager;

    @After
    public void tearDown()
    {
        ofBizDelegator = null;
        collectionReorderer = null;
        fieldConfigManager = null;
    }

    @Test
    public void testGetAllOptions() throws Exception
    {
        final Option option = new MockOption(null, null, null, null, null, 10L);
        when(ofBizDelegator.findAll("CustomFieldOption", null)).thenReturn(ImmutableList.<GenericValue>of());

        final AtomicBoolean called = new AtomicBoolean(false);
        final DefaultOptionsManager optionsManager = new DefaultOptionsManager(ofBizDelegator, collectionReorderer, fieldConfigManager)
        {
            @Override
            List<Option> convertGVsToOptions(final List<GenericValue> optionGvs)
            {
                called.set(true);
                return ImmutableList.of(option);
            }
        };

        final List<Option> result = optionsManager.getAllOptions();
        assertThat("called", called.get(), is(true));
        assertThat(result, contains(option));
    }

    @Test
    public void testFindByOptionValue() throws Exception
    {
        final Option option1 = new MockOption(null, null, null, "value", null, 10L);
        final DefaultOptionsManager manager = new DefaultOptionsManager(ofBizDelegator, collectionReorderer, fieldConfigManager)
        {
            @Override
            public List<Option> getAllOptions()
            {
                return ImmutableList.of(option1, new MockOption(null, null, null, "DiffValue", null, 10L));
            }
        };

        final List<Option> result = manager.findByOptionValue("VALUE");
        assertThat(result, contains(option1));
    }

    @Test
    public void testRemoveCustomFieldConfigOptions() throws Exception
    {
        when(ofBizDelegator.removeByAnd("CustomFieldOption", ImmutableMap.of("customfieldconfig", 1L))).thenReturn(1);

        final DefaultOptionsManager manager = new DefaultOptionsManager(ofBizDelegator, collectionReorderer, fieldConfigManager);
        final FieldConfigImpl fieldConfig = new FieldConfigImpl(1L, "", "", null, "");

        manager.removeCustomFieldConfigOptions(fieldConfig);
    }
}
