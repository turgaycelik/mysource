package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.FieldMap;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestUpgradeTask_Build572
{
    @Mock ApplicationProperties applicationProperties;

    MockOfBizDelegator ofBizDelegator = new MockOfBizDelegator();

    @After
    public void tearDown()
    {
        applicationProperties = null;
        ofBizDelegator = null;
    }

    @Test
    public void testGetBuildNumber() throws Exception
    {
        assertEquals("572", fixture().getBuildNumber());
    }

    @Test
    public void testDoUpgrade() throws Exception
    {
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_CLONE_LINKTYPE_NAME)).thenReturn("Cloners");

        fixture().doUpgrade(true);

        ofBizDelegator.verifyAll(
                link(1000L, "blocks", "is blocked by", "Blocks"),
                link(1001L, "clones", "is cloned by", "Cloners"),
                link(1002L, "duplicates", "is duplicated by", "Duplicate"),
                link(1003L, "relates to", "relates to", "Relates") );
        verify(applicationProperties).setOption(APKeys.JIRA_OPTION_ISSUELINKING, true);
    }

    UpgradeTask_Build572 fixture()
    {
        return new UpgradeTask_Build572(ofBizDelegator, applicationProperties);
    }

    static GenericValue link(Long id, String outward, String inward, String linkname)
    {
        return new MockGenericValue("IssueLinkType", FieldMap.build(
                "id", id,
                "outward", outward,
                "inward", inward,
                "style", null,
                "linkname", linkname));
    }
}
