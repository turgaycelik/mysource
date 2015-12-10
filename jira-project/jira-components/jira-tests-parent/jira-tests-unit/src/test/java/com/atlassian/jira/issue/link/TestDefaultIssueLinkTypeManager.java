package com.atlassian.jira.issue.link;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TestDefaultIssueLinkTypeManager
{
    private DefaultIssueLinkTypeManager issueLinkTypeManager = new DefaultIssueLinkTypeManager(
            new MockOfBizDelegator(null, null), new MemoryCacheManager());
    private LinkTypeToNameTransformer linkTypeToNameTransformer = new LinkTypeToNameTransformer();
    private LinkTypeToInwardTransformer linkTypeToInwardTransformer = new LinkTypeToInwardTransformer();
    private LinkTypeToOutwardTransformer linkTypeToOutwardTransformer = new LinkTypeToOutwardTransformer();

    private MockOfBizDelegator ofBizDelegator;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldThrowExceptionOnCreateIssueLinkTypeForNullName() throws GenericEntityException
    {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("name should not be null!");

        issueLinkTypeManager.createIssueLinkType(null, "test outward", "test inward", "test style");
    }

    @Test
    public void shouldThrowExceptionOnCreateIssueLinkTypeForNullOutward() throws GenericEntityException
    {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("outward should not be null!");

        issueLinkTypeManager.createIssueLinkType("test name", null, "test inward", "test style");
    }

    @Test
    public void shouldThrowExceptionOnCreateIssueLinkTypeForNullInward() throws GenericEntityException
    {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("inward should not be null!");

        issueLinkTypeManager.createIssueLinkType("test name", "test outward", null, "test style");
    }

    @Test
    public void shouldCreateIssueLinkTypeForValidData()
    {
        final Map<String, ? extends Serializable> expectedFields = ImmutableMap.of("linkname", "test name",
                "id", Long.valueOf(1000), "outward", "test outward", "inward", "test inward", "style", "test style");

        final GenericValue genericValue = new MockGenericValue(OfBizDelegator.ISSUE_LINK_TYPE, expectedFields);
        setupDatabase(null, ImmutableList.of(genericValue));

        issueLinkTypeManager.createIssueLinkType("test name", "test outward", "test inward", "test style");
        ofBizDelegator.verifyAll();
    }

    @Test
    public void shouldGetTestIssueLinkTypeByIdWhenExists()
    {
        setupDatabase(buildDefaultGenericValue(), null);
        final IssueLinkType issueLinkType = issueLinkTypeManager.getIssueLinkType(Long.valueOf(11));

        assertNotNull("issueLinkType must not be null", issueLinkType);
        assertEquals("test-link", issueLinkType.getName());
    }

    @Test
    @Ignore("there exist consumers that rely on the old behaviour of ignoring the excludeSystemLinks param")
    public void shouldReturnNullForSystemLinkWhenExcluded()
    {
        final Map<String, ? extends Serializable> expectedFields = ImmutableMap.of("linkname", "test-link",
                "id", Long.valueOf(44), "outward", "test outward", "inward", "test inward", "style", "jira_system-link");

        final GenericValue genericValue = new MockGenericValue(OfBizDelegator.ISSUE_LINK_TYPE, expectedFields);
        setupDatabase(ImmutableList.of(genericValue), null);

        final IssueLinkType issueLinkType = issueLinkTypeManager.getIssueLinkType(Long.valueOf(44), true);
        assertNull("issueLinkType must be null", issueLinkType);
    }

    @Test
    public void shouldReturnSystemLinkWhenNotExcluded()
    {
        final Map<String, ? extends Serializable> expectedFields = ImmutableMap.of("linkname", "test-link",
                "id", Long.valueOf(45), "outward", "test outward", "inward", "test inward", "style", "jira_system-link");

        final GenericValue genericValue = new MockGenericValue(OfBizDelegator.ISSUE_LINK_TYPE, expectedFields);
        setupDatabase(ImmutableList.of(genericValue), null);

        final IssueLinkType issueLinkType = issueLinkTypeManager.getIssueLinkType(Long.valueOf(45), false);
        assertNotNull("issueLinkType must not be null", issueLinkType);
        assertEquals("test-link", issueLinkType.getName());
    }

    @Test
    public void shouldReturnEmptyListWhenOnlySystemIssueTypesExist()
    {
        final Map<String, ? extends Serializable> expectedFields = ImmutableMap.of("linkname", "test-link",
                "id", Long.valueOf(46), "outward", "test outward", "inward", "test inward", "style", "jira_system-link");

        final GenericValue genericValue = new MockGenericValue(OfBizDelegator.ISSUE_LINK_TYPE, expectedFields);
        setupDatabase(ImmutableList.of(genericValue), null);

        final Collection<IssueLinkType> issueLinkTypes = issueLinkTypeManager.getIssueLinkTypes(true);

        assertThat(issueLinkTypes, Matchers.<IssueLinkType>empty());
    }

    @Test
    public void shouldGetAllIssueLinkTypesWhenExists()
    {
        setupDatabase(buildDefaultGenericValue(), null);

        final Collection<IssueLinkType> issueLinkTypes = issueLinkTypeManager.getIssueLinkTypes();
        final Iterable<String> linkTypeNamesOnly = Iterables.transform(issueLinkTypes, linkTypeToNameTransformer);

        assertThat(linkTypeNamesOnly, Matchers.contains("test-link"));
    }

    @Test
    public void shouldRemoveIssueLinkTypeByEntityId() throws RemoveException, GenericEntityException
    {
        setupDatabase(ImmutableList.of((GenericValue) buildDefaultGenericValue()), Collections.<GenericValue>emptyList());

        issueLinkTypeManager.removeIssueLinkType(Long.valueOf(11));
        ofBizDelegator.verifyAll();
    }

    @Test
    public void shouldGetTestIssueLinkTypeByNameWhenExists()
    {
        setupDatabase(buildDefaultGenericValue(), null);
        @SuppressWarnings ("unchecked")
        final Collection<IssueLinkType> issueLinkTypes = issueLinkTypeManager.getIssueLinkTypesByName("test-link");
        final Iterable<String> linkTypeNamesOnly = Iterables.transform(issueLinkTypes, linkTypeToNameTransformer);

        assertThat(linkTypeNamesOnly, Matchers.contains("test-link"));
    }

    @Test
    public void shouldGetTestIssueLinkTypeByInwardDescriptionWhenExists()
    {
        setupDatabase(buildInwardDescriptionGenericValue(), null);

        final Collection<IssueLinkType> issueLinkTypes = issueLinkTypeManager.getIssueLinkTypesByInwardDescription("indesc");

        final Iterable<String> linkTypeNamesOnly = Iterables.transform(issueLinkTypes, linkTypeToNameTransformer);
        final Iterable<String> inwardOnly = Iterables.transform(issueLinkTypes, linkTypeToInwardTransformer);

        assertThat(linkTypeNamesOnly, Matchers.contains("test-link"));
        assertThat(inwardOnly, Matchers.contains("indesc"));
    }

    @Test
    public void shouldGetTestIssueLinkTypeByInwardDescriptionUppercaseWhenExists()
    {
        setupDatabase(buildInwardDescriptionGenericValue(), null);

        final Collection<IssueLinkType> issueLinkTypes = issueLinkTypeManager.getIssueLinkTypesByInwardDescription("INDESC");

        final Iterable<String> linkTypeNamesOnly = Iterables.transform(issueLinkTypes, linkTypeToNameTransformer);
        final Iterable<String> inwardOnly = Iterables.transform(issueLinkTypes, linkTypeToInwardTransformer);

        assertThat(linkTypeNamesOnly, Matchers.contains("test-link"));
        assertThat(inwardOnly, Matchers.contains("indesc"));
    }

    @Test
    public void shouldGetTestIssueLinkTypeByInwardDescriptionWhenDoesNotExist()
    {
        setupDatabase(buildInwardDescriptionGenericValue(), null);

        final Collection<IssueLinkType> issueLinkTypes = issueLinkTypeManager.getIssueLinkTypesByInwardDescription("doesntexist");
        assertTrue("No issues must be found as key does not exist", issueLinkTypes.isEmpty());
    }

    @Test
    public void shouldGetTestIssueLinkTypeByOutwardDescriptionWhenExists()
    {
        setupDatabase(buildOutwardDescriptionGenericValue(), null);

        Collection<IssueLinkType> issueLinkTypes = issueLinkTypeManager.getIssueLinkTypesByOutwardDescription("outdesc");
        final Iterable<String> linkTypeNamesOnly = Iterables.transform(issueLinkTypes, linkTypeToNameTransformer);
        final Iterable<String> outwardOnly = Iterables.transform(issueLinkTypes, linkTypeToOutwardTransformer);

        assertThat(linkTypeNamesOnly, Matchers.contains("test-link"));
        assertThat(outwardOnly, Matchers.contains("outdesc"));
    }

    @Test
    public void shouldGetTestIssueLinkTypeByOutwardDescriptionUppercaseWhenExists()
    {
        setupDatabase(buildOutwardDescriptionGenericValue(), null);

        // case is different - same result
        Collection<IssueLinkType> issueLinkTypes = issueLinkTypeManager.getIssueLinkTypesByOutwardDescription("OUTDESC");
        final Iterable<String> linkTypeNamesOnly = Iterables.transform(issueLinkTypes, linkTypeToNameTransformer);
        final Iterable<String> outwardOnly = Iterables.transform(issueLinkTypes, linkTypeToOutwardTransformer);

        assertThat(linkTypeNamesOnly, Matchers.contains("test-link"));
        assertThat(outwardOnly, Matchers.contains("outdesc"));
    }

    @Test
    public void shouldGetTestIssueLinkTypeByOutwardDescriptionWhenDoesNotExist()
    {
        setupDatabase(buildOutwardDescriptionGenericValue(), null);

        final Collection<IssueLinkType> issueLinkTypes = issueLinkTypeManager.getIssueLinkTypesByOutwardDescription("doesntexist");
        assertTrue("No issues must be found as key does not exist", issueLinkTypes.isEmpty());
    }

    @Test
    public void shouldGetIssueLinkTypeByStyleWhenExists()
    {
        setupDatabase(buildDefaultGenericValue(), null);

        @SuppressWarnings ("unchecked")
        final Collection<IssueLinkType> issueLinkTypes = issueLinkTypeManager.getIssueLinkTypesByStyle(null);
        final Iterable<String> linkTypeNamesOnly = Iterables.transform(issueLinkTypes, linkTypeToNameTransformer);

        assertThat(linkTypeNamesOnly, Matchers.contains("test-link"));
    }

    @Test
    public void shouldUpdateIssueLinkTypeHappyPath() throws GenericEntityException
    {
        final MockGenericValue start = buildDefaultGenericValue();
        final MockGenericValue end = new MockGenericValue(OfBizDelegator.ISSUE_LINK_TYPE, ImmutableMap.of("linkname", "test name", "id", Long.valueOf(11), "outward", "test outward", "inward", "test inward"));
        setupDatabase(start, end);

        final String name = "test name";
        final String outward = "test outward";
        final String inward = "test inward";
        final IssueLinkType issueLinkType = issueLinkTypeManager.getIssueLinkType(Long.valueOf(11));
        issueLinkTypeManager.updateIssueLinkType(issueLinkType, name, outward, inward);

        assertEquals(name, issueLinkType.getName());
        assertEquals(outward, issueLinkType.getOutward());
        assertEquals(inward, issueLinkType.getInward());
        ofBizDelegator.verifyAll();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void setupDatabase(final List<GenericValue> startingFields, final List<GenericValue> expectedFields)
    {
        ofBizDelegator = new MockOfBizDelegator(startingFields, expectedFields);
        setupManager(ofBizDelegator, new MemoryCacheManager());
    }

    private void setupDatabase(final GenericValue start, final GenericValue end)
    {
        setupDatabase(ImmutableList.of(start), end != null ? ImmutableList.of(end) : null);
    }


    private void setupManager(OfBizDelegator delegator, MemoryCacheManager cacheManager)
    {
        issueLinkTypeManager = new DefaultIssueLinkTypeManager(delegator, cacheManager);
    }

    private MockGenericValue buildDefaultGenericValue()
    {
        return new MockGenericValue(OfBizDelegator.ISSUE_LINK_TYPE, ImmutableMap.of("linkname", "test-link", "id", Long.valueOf(11)));
    }

    private MockGenericValue buildOutwardDescriptionGenericValue()
    {
        return new MockGenericValue(OfBizDelegator.ISSUE_LINK_TYPE, ImmutableMap.of("linkname", "test-link", "id", Long.valueOf(11), "outward", "outdesc"));
    }

    private MockGenericValue buildInwardDescriptionGenericValue()
    {
        return new MockGenericValue(OfBizDelegator.ISSUE_LINK_TYPE, ImmutableMap.of("linkname", "test-link", "id", Long.valueOf(11), "inward", "indesc"));
    }

    private static class LinkTypeToNameTransformer implements Function<IssueLinkType, String>
    {
        @Override
        public String apply(@Nullable final IssueLinkType input)
        {
            if (input == null)
            {
                throw new NullPointerException();
            }
            return input.getName();
        }
    }

    private static class LinkTypeToInwardTransformer implements Function<IssueLinkType, String>
    {
        @Override
        public String apply(@Nullable final IssueLinkType input)
        {
            if (input == null)
            {
                throw new NullPointerException();
            }
            return input.getInward();
        }
    }

    private static class LinkTypeToOutwardTransformer implements Function<IssueLinkType, String>
    {
        @Override
        public String apply(@Nullable final IssueLinkType input)
        {
            if (input == null)
            {
                throw new NullPointerException();
            }
            return input.getOutward();
        }
    }
}
