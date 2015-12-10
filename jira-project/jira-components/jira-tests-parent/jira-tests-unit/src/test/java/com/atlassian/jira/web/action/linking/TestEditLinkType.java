package com.atlassian.jira.web.action.linking;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeImpl;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockHttp;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.web.action.MockRedirectSanitiser;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.RedirectSanitiser;
import com.atlassian.jira.web.action.admin.linking.EditLinkType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericValue;

import webwork.action.Action;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;

public class TestEditLinkType
{
    @Mock
    private IssueLinkTypeManager mockIssueLinkTypeManager;

    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext jiraAuthenticationContext;

    @AvailableInContainer
    private RedirectSanitiser redirectSanitiser = new MockRedirectSanitiser();

    @Mock
    private I18nHelper i18nHelper;

    @Rule
    public final TestRule initMock = MockitoMocksInContainer.forTest(this);
    @Rule
    public final MockHttp.DefaultMocks mockHttp = MockHttp.withDefaultMocks();

    @Before
    public void setUp() throws Exception
    {
        Mockito.when(jiraAuthenticationContext.getI18nHelper()).thenReturn(i18nHelper);
        Mockito.when(i18nHelper.getText(anyString(), anyObject())).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable
            {
                return Arrays.toString(invocation.getArguments());
            }
        });
        Mockito.when(i18nHelper.getText(anyString())).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable
            {
                return "textFor-" + invocation.getArguments()[0];
            }
        });
    }

    @Test
    public void testValidationFailsWithInvalidLink() throws Exception
    {
        final Long id = 10l;
        final String name = "existing link name";

        Mockito.when(mockIssueLinkTypeManager.getIssueLinkTypesByName(name)).thenReturn(Collections.<IssueLinkType>emptyList());
        Mockito.when(mockIssueLinkTypeManager.getIssueLinkType(id)).thenReturn(null);

        final EditLinkType editLinkType = new EditLinkType(mockIssueLinkTypeManager);
        editLinkType.setId(id);
        editLinkType.setName(name);
        editLinkType.setInward("inward desc");
        editLinkType.setOutward("outward desc");

        final String result = editLinkType.execute();
        Assert.assertEquals(Action.INPUT, result);

        final Collection<String> errors = editLinkType.getErrorMessages();
        Assert.assertThat(errors, IsIterableContainingInOrder.contains("[editlink.id.notfound, " + id + "]"));

    }

    @Test
    public void testValidationFailsWithNoOutwardNoInward() throws Exception
    {
        final Long id = 1l;
        final String name = "existing link name";
        Mockito.when(mockIssueLinkTypeManager.getIssueLinkType(id)).thenReturn(null);

        final EditLinkType editLinkType = new EditLinkType(mockIssueLinkTypeManager);
        editLinkType.setId(id);
        editLinkType.setName(name);

        final String result = editLinkType.execute();

        Assert.assertEquals(Action.INPUT, result);
        final Map<String, String> errors = editLinkType.getErrors();
        Assert.assertEquals(2, errors.size());
        Assert.assertEquals("textFor-editlink.outward.desc.notspecified", errors.get("outward"));
        Assert.assertEquals("textFor-editlink.inward.desc.notspecified", errors.get("inward"));
    }

    @Test
    public void testValidationFailsWithNoName() throws Exception
    {
        final GenericValue existingLinkType = new MockGenericValue("IssueLinkType", ImmutableMap.of("id", (long) 1, "linkname", "existing link name", "inward", "inward desc", "outward", "outward desc"));


        final Long id = 1l;

        Mockito.when(mockIssueLinkTypeManager.getIssueLinkType(id)).thenReturn(new IssueLinkTypeImpl(existingLinkType));

        final EditLinkType editLinkType = new EditLinkType(mockIssueLinkTypeManager);
        editLinkType.setId(id);
        editLinkType.setInward("inward desc");
        editLinkType.setOutward("outward desc");

        final String result = editLinkType.execute();
        Assert.assertEquals(Action.INPUT, result);

        final Map errors = editLinkType.getErrors();
        Assert.assertEquals(1, errors.size());
        Assert.assertEquals("textFor-editlink.name.notspecified", errors.get("name"));

    }

    @Test
    public void testValidationFailsWithSameLinkNameAndDifferentID() throws Exception
    {
        final GenericValue existingLinkType = new MockGenericValue("IssueLinkType", ImmutableMap.of("id", 1l, "linkname", "existing link name", "inward", "inward desc", "outward", "outward desc"));
        final GenericValue anotherExistingLinkType = new MockGenericValue("IssueLinkType", ImmutableMap.of("id", 2l, "linkname", "another existing link name", "inward", "inward desc", "outward", "outward desc"));

        final Long id = 1l;
        final String name = "another existing link name";

        Mockito.when(mockIssueLinkTypeManager.getIssueLinkTypesByName(name)).thenReturn(ImmutableList.<IssueLinkType>of(new IssueLinkTypeImpl(anotherExistingLinkType)));
        Mockito.when(mockIssueLinkTypeManager.getIssueLinkType(id)).thenReturn(new IssueLinkTypeImpl(existingLinkType));

        final EditLinkType editLinkType = new EditLinkType(mockIssueLinkTypeManager);
        editLinkType.setId(id);
        editLinkType.setName(name);
        editLinkType.setInward("inward desc");
        editLinkType.setOutward("outward desc");

        final String result = editLinkType.execute();
        Assert.assertEquals(Action.INPUT, result);

        final Map errors = editLinkType.getErrors();
        Assert.assertEquals(1, errors.size());
        Assert.assertEquals("textFor-editlink.name.alreadyexists", errors.get("name"));

    }

    @Test
    public void testValidationSucceedsWithNewLinkNameAndSameID() throws Exception
    {
        final GenericValue existingLinkType = new MockGenericValue("IssueLinkType", ImmutableMap.of("id", (long) 1, "linkname", "existing link name", "inward", "inward desc", "outward", "outward desc"));

        final Long id = (long) 1;
        final String name = "new link name";
        final String inwardDesc = "inward desc";
        final String outwardDesc = "outward desc";
        final IssueLinkType expectedIssueLinkType = new IssueLinkTypeImpl(existingLinkType);

        Mockito.when(mockIssueLinkTypeManager.getIssueLinkTypesByName(name)).thenReturn(Collections.<IssueLinkType>emptyList());
        Mockito.when(mockIssueLinkTypeManager.getIssueLinkType(id)).thenReturn(expectedIssueLinkType);


        final EditLinkType editLinkType = new EditLinkType(mockIssueLinkTypeManager);
        editLinkType.setId(id);
        editLinkType.setName(name);
        editLinkType.setInward(inwardDesc);
        editLinkType.setOutward(outwardDesc);

        final String result = editLinkType.execute();

        Assert.assertEquals(Action.NONE, result);
        Mockito.verify(mockIssueLinkTypeManager).updateIssueLinkType(expectedIssueLinkType, name, outwardDesc, inwardDesc);
    }


}
