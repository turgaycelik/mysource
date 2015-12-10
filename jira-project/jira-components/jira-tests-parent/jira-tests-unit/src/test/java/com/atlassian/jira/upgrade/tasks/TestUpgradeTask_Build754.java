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
public class TestUpgradeTask_Build754
{
    private static final String ENTITY_NAME = "SearchRequest";
    private static final String FIELD_OWNER = "user";
    private static final String FIELD_AUTHOR = "author";

    @Mock
    private OfBizDelegator delegator;
    private UpgradeTask_Build754 uprgadeTask;

    @Before
    public void setUp() throws Exception
    {
        uprgadeTask = new UpgradeTask_Build754(delegator);
    }

    @Test
    public void checkCorrectBuildNumber()
    {
        assertThat(uprgadeTask.getBuildNumber(), equalTo("754"));
    }
    
    @Test
    public void doUpgrade()
    {
        GenericValue emptyUserName = mockGv("", "");
        GenericValue badGoodUser = mockGv("BadOwner", "gooduser");
        GenericValue goodBadUser = mockGv("goodowner", "BadUser");
        GenericValue badBadUser = mockGv("BadOwner", "BadUser");
        GenericValue goodGoodUser = mockGv("goodowner", "gooduser");

        stub(delegator.findAll(ENTITY_NAME)).toReturn(newArrayList(emptyUserName, badGoodUser, goodBadUser, badBadUser, goodGoodUser));

        uprgadeTask.doUpgrade(false);

        assertSearchRequest(emptyUserName, "", "");
        assertSearchRequest(badGoodUser, "badowner", "gooduser");
        assertSearchRequest(goodBadUser, "goodowner", "baduser");
        assertSearchRequest(badBadUser, "badowner", "baduser");
        assertSearchRequest(goodGoodUser, "goodowner", "gooduser");

        Mockito.verify(delegator).store(badGoodUser);
        Mockito.verify(delegator).store(goodBadUser);
        Mockito.verify(delegator).store(badBadUser);
        Mockito.verify(delegator, never()).store(emptyUserName);
        Mockito.verify(delegator, never()).store(goodGoodUser);
    }

    @Test
    public void isIndexTask()
    {
        assertThat(uprgadeTask.isReindexRequired(), is(true));
    }
    
    private void assertSearchRequest(GenericValue emptyUserName, String owner, String author)
    {
        assertThat(emptyUserName.getString(FIELD_OWNER), equalTo(owner));
        assertThat(emptyUserName.getString(FIELD_AUTHOR), equalTo(author));
    }

    private static GenericValue mockGv(String owner, String author)
    {
        Map<String, Object> fields = Maps.newLinkedHashMap();
        fields.put(FIELD_OWNER, owner);
        fields.put(FIELD_AUTHOR, author);

        return new MockGenericValue(ENTITY_NAME, fields);
    }
}
