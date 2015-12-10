package com.atlassian.jira.appconsistency.integrity;

import com.atlassian.jira.appconsistency.integrity.amendment.Amendment;
import com.atlassian.jira.appconsistency.integrity.check.SearchRequestRelationCheck;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.matchers.AmendmentMatchers;
import com.atlassian.jira.matchers.IterableMatchers;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import javax.annotation.Nullable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class TestSearchRequestRelationCheck
{
    @Rule
    public MockitoContainer mockitoMocksInContainer = MockitoMocksInContainer.rule(this);

    private OfBizDelegator ofBizDelegator;

    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext authenticationContext;

    private SearchRequestRelationCheck searchRequestRelationCheck;

    @Before
    public void setUp() throws Exception
    {
        when(authenticationContext.getI18nHelper()).thenReturn(new MockI18nHelper());
        ofBizDelegator = new MockOfBizDelegator();

        ofBizDelegator.createValue("Project", FieldMap.build("id", 3000L));

        Long id = 1000L;
        ofBizDelegator.createValue("SearchRequest", FieldMap.build("id", id, "project", 3000L, "name", "Test Request " + id));
        id = 1001L;
        ofBizDelegator.createValue("SearchRequest", FieldMap.build("id", id, "project", 3001L, "name", "Test Request " + id));
        id = 1002L;
        ofBizDelegator.createValue("SearchRequest", FieldMap.build("id", id, "project", 3002L, "name", "Test Request " + id));

        searchRequestRelationCheck = new SearchRequestRelationCheck(ofBizDelegator, 1);
    }

    private final Function<GenericValue, Long> requestToIdTransformer = new Function<GenericValue, Long>()
    {
        @Override
        public Long apply(@Nullable final GenericValue genericValue)
        {
            return genericValue.getLong("id");
        }
    };


    @Test
    public void testPreview() throws IntegrityException, GenericEntityException
    {
        List<Amendment> amendments = searchRequestRelationCheck.preview();
        assertEquals(2, amendments.size());

        assertThat(
                amendments,
                Matchers.<Amendment>containsInAnyOrder(
                        Matchers.allOf(
                                AmendmentMatchers.isError(),
                                AmendmentMatchers.withMessage("admin.integrity.check.search.request.relation.check.preview [Test Request 1001]")
                        ),
                        Matchers.allOf(
                                AmendmentMatchers.isError(),
                                AmendmentMatchers.withMessage("admin.integrity.check.search.request.relation.check.preview [Test Request 1002]")
                        )
                )
        );

        // There should still be 3 Search Requests left as the preview method should not modify the database
        List<GenericValue> allRequests = ofBizDelegator.findAll("SearchRequest");

        assertThat(Iterables.transform(allRequests, requestToIdTransformer), Matchers.containsInAnyOrder(1000L, 1001L, 1002L));
    }

    @Test
    public void testCorrect() throws IntegrityException, GenericEntityException
    {
        // This should correct the problem
        List<Amendment> amendments = searchRequestRelationCheck.correct();
        assertEquals(2, amendments.size());

        assertThat(
                amendments,
                Matchers.<Amendment>containsInAnyOrder(
                        Matchers.allOf(
                                AmendmentMatchers.isError(),
                                AmendmentMatchers.withMessage("admin.integrity.check.search.request.relation.check.message [Test Request 1001]")
                        ),
                        Matchers.allOf(
                                AmendmentMatchers.isError(),
                                AmendmentMatchers.withMessage("admin.integrity.check.search.request.relation.check.message [Test Request 1002]")
                        )
                )
        );

        // There should be 1 entry
        List<GenericValue> allRequests = ofBizDelegator.findAll("SearchRequest");
        assertEquals(1000L, Iterables.getOnlyElement(allRequests).getLong("id").longValue());

        // This should return no amendments as they have just been corrected.
        amendments = searchRequestRelationCheck.preview();
        assertThat(amendments, IterableMatchers.emptyIterable(Amendment.class));
    }
}
