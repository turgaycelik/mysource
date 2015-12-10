package com.atlassian.jira.issue.fields.config.manager;

import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.option.OptionSetManager;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.ComponentLocator;

import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * @since v4.0
 */
public class TestFieldConfigCleanupImpl extends MockControllerTestCase
{
    private OptionSetManager optionSetManager;
    private GenericConfigManager genericConfigManager;
    private ComponentLocator componentLocator;
    private OptionsManager optionsManager;

    @Before
    public void setUp() throws Exception
    {
        optionSetManager = mockController.getMock(OptionSetManager.class);
        genericConfigManager = mockController.getMock(GenericConfigManager.class);
        componentLocator = mockController.getMock(ComponentLocator.class);
        optionsManager = mockController.getMock(OptionsManager.class);
    }

    @Test
    public void testRemoveAdditionalData() throws Exception
    {
        final FieldConfig fieldConfig = mockController.getMock(FieldConfig.class);
        EasyMock.expect(fieldConfig.getId())
                .andStubReturn(123L);

        optionSetManager.removeOptionSet(fieldConfig);
        EasyMock.expectLastCall();
        genericConfigManager.remove("DefaultValue", "123");
        EasyMock.expectLastCall();
        optionsManager.removeCustomFieldConfigOptions(fieldConfig);
        EasyMock.expectLastCall();

        replay();
        
        final FieldConfigCleanupImpl cleanup = new FieldConfigCleanupImpl(optionSetManager, genericConfigManager, componentLocator)
        {
            @Override
            OptionsManager getOptionsManager()
            {
                return optionsManager;
            }
        };

        cleanup.removeAdditionalData(fieldConfig);
    }
}
