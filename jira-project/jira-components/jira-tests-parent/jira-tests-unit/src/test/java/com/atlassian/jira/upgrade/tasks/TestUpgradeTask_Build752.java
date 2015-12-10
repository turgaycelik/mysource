package com.atlassian.jira.upgrade.tasks;

import java.util.Map;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;

import com.google.common.collect.Maps;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericValue;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.stub;

/**
 * @since v5.0.3
 */
@RunWith(MockitoJUnitRunner.class)
public class TestUpgradeTask_Build752
{
    private static final String ENTITY_NAME = "PortalPage";
    private static final String FIELD_USERNAME = "username";

    @Mock
    private OfBizDelegator delegator;
    private UpgradeTask_Build752 build727;

    @Before
    public void setUp() throws Exception
    {
        build727 = new UpgradeTask_Build752(delegator);
    }

    @Test
    public void checkCorrectBuildNumber()
    {
        build727 = new UpgradeTask_Build752(delegator);
        assertThat(build727.getBuildNumber(), equalTo("752"));
    }
    
    @Test
    public void doUpgrade()
    {
        GenericValue emptyUserName = mockGv("");
        GenericValue badUser = mockGv("BadUser");
        GenericValue goodUser = mockGv("gooduser");
        
        stub(delegator.findAll(ENTITY_NAME)).toReturn(newArrayList(emptyUserName, badUser, goodUser));

        build727.doUpgrade(false);

        assertThat(emptyUserName.getString(FIELD_USERNAME), equalTo(""));
        assertThat(badUser.getString(FIELD_USERNAME), equalTo("baduser"));
        assertThat(goodUser.getString(FIELD_USERNAME), equalTo("gooduser"));

        Mockito.verify(delegator).store(badUser);
        Mockito.verify(delegator, never()).store(goodUser);
        Mockito.verify(delegator, never()).store(emptyUserName);
    }

    @Test
    public void isNotIndexTask()
    {
        assertThat(build727.isReindexRequired(), is(false));
    }

    private static GenericValue mockGv(String username)
    {
        Map<String, Object> fields = Maps.newLinkedHashMap();
        fields.put(FIELD_USERNAME, username);
        
        return new MockGenericValue(ENTITY_NAME, fields);
    }
}
