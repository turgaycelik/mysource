package com.atlassian.jira.upgrade.tasks;

import java.util.List;
import java.util.Map;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockComponentContainer;
import com.atlassian.jira.ofbiz.OfBizDelegator;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericValue;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test the upgrade of dark feature being stored as text rather then 255
 *
 * @since v6.0
 */

@RunWith (MockitoJUnitRunner.class)
public class TestUpgradeTask_Build6133
{


    @Rule
    public MockComponentContainer mockComponentContainer = new MockComponentContainer(this);

    @Mock
    @AvailableInContainer
    OfBizDelegator mockOfBizDelegator;

    @Mock
    @AvailableInContainer
    ApplicationProperties mockApplicationProperties;

    private static final Map<String, String> PLUGIN_STATE_MAP = ImmutableSortedMap.of("jira.plugin.state-.plugin1", "true",
            "jira.plugin.state-.plugin2", "false",
            "jira.plugin.state-.plugin3", "true",
            "jira.plugin.state-.plugin4", "false",
            "jira.plugin.state-.plugin5", "true");


    private static final String PLUGIN_STATE_ENTITY = "PluginState";

    private final List<GenericValue> mockGenericValues = Lists.newArrayList();

    @Before
    public void setupMocks()
    {
        when(mockApplicationProperties.getStringsWithPrefix(APKeys.GLOBAL_PLUGIN_STATE_PREFIX)).thenReturn(PLUGIN_STATE_MAP.keySet());
        for (int i = 0; i < PLUGIN_STATE_MAP.size(); i++)
        {
            mockGenericValues.add(mock(GenericValue.class));
        }
        when(mockOfBizDelegator.makeValue(PLUGIN_STATE_ENTITY)).thenReturn(mockGenericValues.get(0),
                mockGenericValues.subList(1, PLUGIN_STATE_MAP.size()).toArray(new GenericValue[0]) );
        when(mockApplicationProperties.getStringsWithPrefix("jira.plugin.state-")).thenReturn(PLUGIN_STATE_MAP.keySet());
        when(mockApplicationProperties.getString(anyString())).thenAnswer(new Answer<String>()
        {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable
            {
                return PLUGIN_STATE_MAP.get(invocation.getArguments()[0]);
            }
        });
    }


    @Test
    public void testUpgradeCreatesValues() throws Exception
    {
        UpgradeTask_Build6133 upgradeTask = new UpgradeTask_Build6133();
        upgradeTask.doUpgrade(true);
        int count = 0;
        List<GenericValue> gvs = Lists.newArrayList();
        for (String key : PLUGIN_STATE_MAP.keySet())
        {
            verify(mockGenericValues.get(count)).set("key", "plugin"+(count+1));
            verify(mockGenericValues.get(count)).set("enabled", PLUGIN_STATE_MAP.get(key));
            gvs.add(mockGenericValues.get(count));
            count++;
        }
        verify(mockOfBizDelegator).storeAll(gvs);
    }

    @Test
    public void testUpgradeRemovesOldKeys() throws Exception
    {
        UpgradeTask_Build6133 upgradeTask = new UpgradeTask_Build6133();
        upgradeTask.doUpgrade(true);

        for (String key : PLUGIN_STATE_MAP.keySet())
        {
            verify(mockApplicationProperties).setString(key, null);
        }
    }


}
