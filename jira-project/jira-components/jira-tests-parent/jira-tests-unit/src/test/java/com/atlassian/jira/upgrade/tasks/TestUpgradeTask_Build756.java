package com.atlassian.jira.upgrade.tasks;

import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.MockUserManager;

import org.ofbiz.core.entity.GenericValue;

import junit.framework.TestCase;

public class TestUpgradeTask_Build756 extends TestCase
{
    MockOfBizDelegator mockOfBizDelegator = new MockOfBizDelegator(getInitialValues(), getExpectedValues());

    public void testUpgrade() throws Exception
    {
        MockUserManager mockUserManager = new MockUserManager();
        mockUserManager.addUser(new MockUser("ROBIN"));

        UpgradeTask_Build756 upgradeTask_build756 = new UpgradeTask_Build756(mockOfBizDelegator, mockUserManager);
        upgradeTask_build756.doUpgrade(false);

        mockOfBizDelegator.verify();
    }

    private List<GenericValue> getInitialValues()
    {
        return Arrays.asList(
                externalEntityGV(10, "robin"),
                externalEntityGV(11, "ROBIN"),
                externalEntityGV(12, "Robbie"),
                externalEntityGV(13, "ROBBIE"),
                externalEntityGV(14, "Jan"),
                externalEntityGV(15, "fred")
        );
    }

    private List<? extends GenericValue> getExpectedValues()
    {
        return Arrays.asList(
                // ROBIN is preffered name, so ID should be 11
                externalEntityGV(11, "robin"),
                // No preferred name, so we take the first one we find: robbie 12
                externalEntityGV(12, "robbie"),
                externalEntityGV(14, "jan"),
                externalEntityGV(15, "fred")
        );
    }

    private GenericValue externalEntityGV(long id, String name)
    {
        return new MockGenericValue("ExternalEntity", FieldMap.build("id", id).add("name", name).add("type", "com.atlassian.jira.user.OfbizExternalEntityStore"));
    }
}
