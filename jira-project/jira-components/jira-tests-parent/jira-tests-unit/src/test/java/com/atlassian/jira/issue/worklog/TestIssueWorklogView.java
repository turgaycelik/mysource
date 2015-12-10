package com.atlassian.jira.issue.worklog;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.atlassian.core.ofbiz.AbstractOFBizTestCase;
import com.atlassian.jira.junit.rules.ClearStatics;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.ofbiz.FieldMap;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.entity.Entity.Name.ISSUE;
import static com.atlassian.jira.entity.Entity.Name.PROJECT;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

/**
 * @since v6.3
 */
public class TestIssueWorklogView extends AbstractOFBizTestCase
{
    // Note: Done as a map in case any need to have the name mapped, even though this is not currently the case
    // The key is the field name in "Issue" and the value is the field name in the view.
    private static final Map<String,String> ISSUE_FIELDS_IN_VIEW = ImmutableMap.<String,String>builder()
            .put("project", "project")
            .put("number", "number")
            .put("timeoriginalestimate", "timeoriginalestimate")
            .put("timeestimate", "timeestimate")
            .put("timespent", "timespent")
            .build();

    private static final String WORKLOG = "Worklog";
    private static final String VIEW = "IssueWorklogView";

    @Rule public ClearStatics clearStatics = new ClearStatics();

    DelegatorInterface genericDelegator;

    @Override
    @Before
    public void setUp()
    {
        genericDelegator = GenericDelegator.getGenericDelegator("default");

        new MockComponentWorker()
                .addMock(DelegatorInterface.class, genericDelegator)
                .init();
    }

    @After
    public void tearDown()
    {
        genericDelegator = null;
    }



    @SuppressWarnings("unchecked")  // containsInAnyOrder
    @Test
    public void testIssueWorklogView() throws GenericEntityException
    {
        final Timestamp ts1 = new Timestamp(System.currentTimeMillis() - 20000L);
        final Timestamp ts2 = new Timestamp(System.currentTimeMillis() - 10000L);
        final Timestamp ts3 = new Timestamp(System.currentTimeMillis());

        // Projects
        final GenericValue foo = project(1L, "FOO", "Foo project");
        final GenericValue bar = project(2L, "BAR", "Bar project");

        // Issues
        final GenericValue foo1 = issue(foo, 1L, "5", FieldMap.build("timeoriginalestimate", 42L));
        final GenericValue foo2 = issue(foo, 2L, "4", FieldMap.build("timeestimate", 42L));
        final GenericValue bar1 = issue(bar, 1L, "3", FieldMap.build("timespent", 42L));

        // Worklogs
        final GenericValue foo2wl1 = worklog(foo2, 10201L, "fred", new FieldMap()
                .add("grouplevel", "Group level")
                .add("created", ts3)
                .add("startdate", ts1)
                .add("timeworked", 43L));
        final GenericValue bar1wl1 = worklog(bar1, 20101L, "fred", new FieldMap()
                .add("grouplevel", "Group level")
                .add("created", ts1)
                .add("updated", ts3)
                .add("updateauthor", "admin")
                .add("startdate", ts2)
                .add("timeworked", 43L));
        final GenericValue bar1wl2 = worklog(bar1, 20102L, "admin", new FieldMap()
                .add("rolelevel", 44L)
                .add("created", ts2)
                .add("updateauthor", "fred")
                .add("startdate", ts1)
                .add("timeworked", 45L));

        // Sanity checks
        assertThat(genericDelegator.findAll(PROJECT), containsInAnyOrder(foo, bar));
        assertThat(genericDelegator.findAll(ISSUE), containsInAnyOrder(foo1, foo2, bar1));
        assertThat(genericDelegator.findAll(WORKLOG), containsInAnyOrder(foo2wl1, bar1wl1, bar1wl2));

        // Real check
        assertThat(genericDelegator.findAll(VIEW), Matchers.<GenericValue>containsInAnyOrder(
                view(foo2, foo2wl1),
                view(bar1, bar1wl1),
                view(bar1, bar1wl2)));
        assertThat(genericDelegator.findByAnd(VIEW, FieldMap.build("project", 1L)), containsInAnyOrder(
                view(foo2, foo2wl1)));
        assertThat(genericDelegator.findByAnd(VIEW, FieldMap.build("project", 2L)), Matchers.<GenericValue>containsInAnyOrder(
                view(bar1, bar1wl1),
                view(bar1, bar1wl2)));
        assertThat(genericDelegator.findByAnd(VIEW, FieldMap.build("project", 3L)), Matchers.<GenericValue>emptyIterable());
    }

    GenericValue project(long id, String key, String name) throws GenericEntityException
    {
        return genericDelegator.create(PROJECT, FieldMap.build(
                "id", id,
                "key", key,
                "name", name));
    }

    GenericValue issue(GenericValue project, long number, String type, Map<String,Object> more)
            throws GenericEntityException
    {
        final long projectId = project.getLong("id");
        final FieldMap fields = FieldMap.build(
                "id", projectId * 100L + number,
                "project", projectId,
                "number", number,
                "summary", project.getString("key") + " #" + number,
                "type", type);
        if (more != null)
        {
            fields.putAll(more);
        }
        return genericDelegator.create(ISSUE, fields);
    }

    GenericValue worklog(GenericValue issue, long id, String author, Map<String,Object> more) throws GenericEntityException
    {
        final FieldMap fields = FieldMap.build(
                "id", id,
                "issue", issue.getLong("id"),
                "author", author,
                "body", "Worklog " + id);
        if (more != null)
        {
            fields.putAll(more);
        }
        return genericDelegator.create(WORKLOG, fields);
    }

    static Matcher<GenericValue> view(GenericValue issue, GenericValue worklog)
    {
        return new ViewMatcher(issue, worklog);
    }

    /**
     * A matcher that ignores mismatches of entries that are null on one side and missing on the other.
     */
    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
    static class ViewMatcher extends TypeSafeMatcher<GenericValue>
    {
        final GenericValue issue;
        final GenericValue worklog;

        ViewMatcher(final GenericValue issue, final GenericValue worklog)
        {
            this.issue = issue;
            this.worklog = worklog;
        }

        @Override
        protected boolean matchesSafely(final GenericValue view)
        {
            if (!VIEW.equals(view.getEntityName()))
            {
                return false;
            }
            final Set<String> seen = new HashSet<String>(64);
            return hasMappedIssueFields(seen, view) &&
                    hasOnlyValidFields(seen, view) &&
                    hasAllNonNullWorklogFields(seen);
        }

        // Verifies that the fields from Issue are correctly mapped
        private boolean hasMappedIssueFields(final Set<String> seen, final GenericValue view)
        {
            for (Map.Entry<String,String> issueEntry : ISSUE_FIELDS_IN_VIEW.entrySet())
            {
                final String issueKey = issueEntry.getKey();
                final String viewKey = issueEntry.getValue();

                seen.add(viewKey);
                if (!Objects.equal(issue.get(issueKey), view.get(viewKey)))
                {
                    return false;
                }
            }
            return true;
        }

        // Verifies that all view fields that weren't provided by the mapped Issue fields came from Worklog instead
        private boolean hasOnlyValidFields(final Set<String> seen, final GenericValue view)
        {
            for (Map.Entry<String,Object> viewEntry : view.entrySet())
            {
                final String fieldName = viewEntry.getKey();
                final Object fieldValue = viewEntry.getValue();

                // If the field wasn't already handled as one of the mapped fields from Issue, then make sure
                // the value came from Worklog.  Note: This implicitly verifies that the field name is valid
                // for Worklog in the first place, as an exception will be thrown if it isn't.
                if (seen.add(fieldName) && !Objects.equal(fieldValue, worklog.get(fieldName)))
                {
                    return false;
                }
            }
            return true;
        }

        // Verifies that all of the non-null fields from Worklog made it into the view
        private boolean hasAllNonNullWorklogFields(final Set<String> seen)
        {
            for (Map.Entry<String,Object> worklogEntry : worklog.entrySet())
            {
                final String fieldName = worklogEntry.getKey();
                final Object fieldValue = worklogEntry.getValue();

                if (!seen.contains(fieldName) && fieldValue != null)
                {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendText("\nView of {\n\tIssue  : ").appendText(String.valueOf(issue))
                    .appendText("\n\tWorklog: ").appendText(String.valueOf(worklog))
                    .appendText("}\n");
        }
    }
}
