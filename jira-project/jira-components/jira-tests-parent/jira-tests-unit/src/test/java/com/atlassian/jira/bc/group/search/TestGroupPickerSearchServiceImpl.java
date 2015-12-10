package com.atlassian.jira.bc.group.search;

import java.util.List;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.util.UserManager;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith (ListeningMockitoRunner.class)
public class TestGroupPickerSearchServiceImpl
{
    @Mock
    private UserManager userManager;

    private Group group1;
    private Group group2;
    private Group group3;
    private Group group4;
    private Group group5;

    @Before
    public void setUp()
    {
        group1 = new MockGroup("1 group zzz");
        group2 = new MockGroup("2 group zzz");
        group3 = new MockGroup("3 group zzz");
        group4 = new MockGroup("aa group");
        group5 = new MockGroup("zzz");
        when(userManager.getGroups()).thenReturn(Lists.newArrayList(group1, group2, group3, group4, group5));
    }

    /**
     * Tests JRA-26981.  Exact matches should be returned first!
     */
    @Test
    public void testExactMatchesAreReturnedFirst()
    {
        final GroupPickerSearchService service = new GroupPickerSearchServiceImpl(userManager);

        List<Group> groups = service.findGroups("group");
        assertEquals(Lists.newArrayList(group1, group2, group3, group4), groups);

        groups = service.findGroups("");
        assertEquals(Lists.newArrayList(group1, group2, group3, group4, group5), groups);

        //group 5 comes later in the alphabet than group1,2 and 3 but because it's an exact match it should be
        //returned first
        groups = service.findGroups("zzz");
        assertEquals(Lists.newArrayList(group5, group1, group2, group3), groups);

        //case insensitve exact matches should also work (but will only return the exact match).
        groups = service.findGroups("zZz");
        assertEquals(Lists.newArrayList(group5), groups);
    }
}
