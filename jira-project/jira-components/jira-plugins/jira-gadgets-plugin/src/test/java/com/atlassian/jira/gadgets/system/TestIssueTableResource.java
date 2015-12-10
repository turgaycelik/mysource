package com.atlassian.jira.gadgets.system;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItem;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.issue.fields.MockNavigableField;
import com.atlassian.jira.rest.api.messages.TextMessage;
import com.atlassian.jira.rest.v1.model.errors.ErrorCollection;
import com.atlassian.jira.rest.v1.model.errors.ValidationError;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.web.component.TableLayoutUtils;
import com.atlassian.query.Query;
import com.atlassian.query.order.OrderBy;
import com.atlassian.query.order.SearchSort;
import org.easymock.classextension.EasyMock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;
import static java.util.Arrays.asList;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.eq;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.isA;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

/**
 * Unit test for {@link com.atlassian.jira.gadgets.system.IssueTableResource}.
 *
 * @since v4.0
 */
public class TestIssueTableResource extends ResourceTest
{

    public void setUp()
    {
        TimeZoneManager timeZoneManager = createMock(TimeZoneManager.class);
        MockComponentWorker componentWorker = new MockComponentWorker();
        componentWorker.registerMock(TimeZoneManager.class, timeZoneManager);
        ComponentAccessor.initialiseWorker(componentWorker);
    }

    public void testValidateJql()
    {
        IssueTableResource itr = new IssueTableResource(null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        assertOkValidationResponse(itr.validateJql("50", null));
        assertOkValidationResponse(itr.validateJql("2", null));
        assertOkValidationResponse(itr.validateJql("20", null));
        assertOkValidationResponse(itr.validateJql("1", null));
        final ValidationError negativeError = new ValidationError(IssueTableResource.NUM_FIELD, "gadget.common.num.negative");
        final ValidationError tooHighError = new ValidationError(IssueTableResource.NUM_FIELD, "gadget.common.num.overlimit", "50");
        final ValidationError nanError = new ValidationError(IssueTableResource.NUM_FIELD, "gadget.common.num.nan");
        assertSingleError(itr.validateJql("51", null), tooHighError);
        assertSingleError(itr.validateJql("500", null), tooHighError);
        assertSingleError(itr.validateJql("-51", null), negativeError);
        assertSingleError(itr.validateJql("0", null), negativeError);
        assertSingleError(itr.validateJql("-0", null), negativeError);
        assertSingleError(itr.validateJql("fruitcake", null), nanError);
        assertSingleError(itr.validateJql("five", null), nanError);
    }
    public void testValidateJqlWithModifiedMax()
    {
        ApplicationProperties applicationProperties = createMock(ApplicationProperties.class);

        expect(applicationProperties.getDefaultBackedString("jira.table.gadget.max.rows")).andReturn("50");
        expect(applicationProperties.getDefaultBackedString("jira.table.gadget.max.rows")).andReturn("20");
        expect(applicationProperties.getDefaultBackedString("jira.table.gadget.max.rows")).andReturn(null);
        expect(applicationProperties.getDefaultBackedString("jira.table.gadget.max.rows")).andReturn(null);
        expect(applicationProperties.getDefaultBackedString("jira.table.gadget.max.rows")).andReturn("");
        expect(applicationProperties.getDefaultBackedString("jira.table.gadget.max.rows")).andReturn("");
        expect(applicationProperties.getDefaultBackedString("jira.table.gadget.max.rows")).andReturn("ab");
        expect(applicationProperties.getDefaultBackedString("jira.table.gadget.max.rows")).andReturn("ab");


        EasyMock.replay(applicationProperties);
        IssueTableResource itr = new IssueTableResource(null, null, null, null, null, null, null, null, null, null, null, applicationProperties, null, null);
        final ValidationError tooHighError = new ValidationError(IssueTableResource.NUM_FIELD, "gadget.common.num.overlimit", "50");
        assertOkValidationResponse(itr.validateJql("50", null));
        assertSingleError(itr.validateJql("50", null), new ValidationError(IssueTableResource.NUM_FIELD, "gadget.common.num.overlimit", "20"));
        assertOkValidationResponse(itr.validateJql("50", null));
        assertSingleError(itr.validateJql("51", null), tooHighError);
        assertOkValidationResponse(itr.validateJql("50", null));
        assertSingleError(itr.validateJql("51", null), tooHighError);
        assertOkValidationResponse(itr.validateJql("50", null));
        assertSingleError(itr.validateJql("51", null), tooHighError);

        EasyMock.verify(applicationProperties);
    }

    public void testValidateColumnNames() throws FieldException
    {
        JiraAuthenticationContext mockJiraAuthenticationContext = createMock(JiraAuthenticationContext.class);
        final User barney = new MockUser("barney");
        expect(mockJiraAuthenticationContext.getLoggedInUser()).andReturn(barney);
        final FieldManager mockFieldManager = createMock(FieldManager.class);
        final Set<NavigableField> navigableFields = new HashSet<NavigableField>();
        navigableFields.add(new MockNavigableField("col1"));
        navigableFields.add(new MockNavigableField("col2"));
        navigableFields.add(new MockNavigableField("col3"));
        navigableFields.add(new MockNavigableField("col4"));
        expect(mockFieldManager.getAvailableNavigableFields(barney)).andReturn(navigableFields);
        replay(mockJiraAuthenticationContext, mockFieldManager);

        IssueTableResource itr = new IssueTableResource(mockJiraAuthenticationContext, null, null, null, null, mockFieldManager, null, null, null, null, null, null, null, null);
        List<ValidationError> errors = new ArrayList<ValidationError>();
        itr.validateColumnNames(asList("col1", "col2", "col3", "col4", "--Default--"), errors);
        assertTrue(errors.isEmpty());
        verify(mockJiraAuthenticationContext, mockFieldManager);
    }

    public void testValidateColumnNamesNotFound() throws FieldException
    {
        JiraAuthenticationContext mockJiraAuthenticationContext = createMock(JiraAuthenticationContext.class);
        final User barney = new MockUser("barney");
        expect(mockJiraAuthenticationContext.getLoggedInUser()).andReturn(barney);
        final FieldManager mockFieldManager = createMock(FieldManager.class);
        final Set<NavigableField> navigableFields = new HashSet<NavigableField>();
        navigableFields.add(new MockNavigableField("col1"));
        navigableFields.add(new MockNavigableField("col2"));
        expect(mockFieldManager.getAvailableNavigableFields(barney)).andReturn(navigableFields);
        replay(mockJiraAuthenticationContext, mockFieldManager);

        IssueTableResource itr = new IssueTableResource(mockJiraAuthenticationContext, null, null, null, null, mockFieldManager, null, null, null, null, null, null, null, null);
        List<ValidationError> errors = new ArrayList<ValidationError>();
        itr.validateColumnNames(asList("col1", "col2", "col3", "col4"), errors);
        assertEquals(1, errors.size());
        assertEquals(new ValidationError(IssueTableResource.COLUMN_NAMES, "gadget.issuetable.common.cols.not.found", "col3, col4"), errors.get(0));
        verify(mockJiraAuthenticationContext, mockFieldManager);
    }

    public void testValidateColumnNamesEmpty() throws FieldException
    {
        IssueTableResource itr = new IssueTableResource(null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        List<ValidationError> errors = new ArrayList<ValidationError>();
        itr.validateColumnNames(null, errors);
        assertTrue(errors.isEmpty());
    }

    public void testStripNumberPrefix()
    {
        IssueTableResource itr = new IssueTableResource(null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        assertEquals(new Long(123L), itr.stripFilterPrefix("filter-123", "filter-"));
        assertEquals(new Long(0L), itr.stripFilterPrefix("foobar-0", "foobar-"));
        assertEquals(new Long(-31L), itr.stripFilterPrefix("project--31", "project-"));
        assertEquals(new Long(10094L), itr.stripFilterPrefix("10094", "project-"));
        try
        {
            itr.stripFilterPrefix("filter-filter-123", "filter-");
            fail("expected exception");
        }
        catch (Exception yay)
        {

        }
    }

    public void testAddOrderByToSearchRequestOrderByEmpty()
    {
        IssueTableResource itr = new IssueTableResource(null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        // checking that we don't blow up for blank sortby
        itr.addOrderByToSearchRequest(null, null);
        itr.addOrderByToSearchRequest(null, "");
        itr.addOrderByToSearchRequest(null, " ");
    }

    public void testAddOrderByToSearchRequest()
    {
        JiraAuthenticationContext mockJiraAuthenticationContext = createMock(JiraAuthenticationContext.class);
        final User barney = new MockUser("barney");
        expect(mockJiraAuthenticationContext.getLoggedInUser()).andReturn(barney);

        final SearchSortUtil mockSearchSortUtil = createMock(SearchSortUtil.class);

        final OrderBy mockOrderBy = createMock(OrderBy.class);
        expect(mockOrderBy.getSearchSorts()).andReturn(Collections.<SearchSort>emptyList());
        expect(mockSearchSortUtil.getOrderByClause(isA(Map.class))).andReturn(mockOrderBy);
        expect(mockSearchSortUtil.mergeSearchSorts(eq(barney), isA(Collection.class), isA(Collection.class), eq(3))).andReturn(Collections.<SearchSort>emptyList());
        replay(mockJiraAuthenticationContext, mockSearchSortUtil, mockOrderBy);

        IssueTableResource itr = new IssueTableResource(mockJiraAuthenticationContext, null, null, null, null, null, mockSearchSortUtil, null, null, null, null, null, null, null);
        final SearchRequest searchRequest = new SearchRequest();
        itr.addOrderByToSearchRequest(searchRequest, "foobar:ASC");
        assertNotNull(searchRequest.getQuery());
        verify(mockJiraAuthenticationContext, mockSearchSortUtil, mockOrderBy);
    }

    public void testGetFilterTable() throws FieldException
    {
        JiraAuthenticationContext mockJiraAuthenticationContext = createMock(JiraAuthenticationContext.class);
        final User barney = new MockUser("barney");
        expect(mockJiraAuthenticationContext.getLoggedInUser()).andReturn(barney).anyTimes();
        final Query query = JqlQueryBuilder.newBuilder().buildQuery();
        SearchRequest mockSearchRequest = createMock(SearchRequest.class);
        expect(mockSearchRequest.getId()).andReturn(888L).atLeastOnce();
        expect(mockSearchRequest.getName()).andReturn("the search request").atLeastOnce();
        expect(mockSearchRequest.getDescription()).andReturn("the description").atLeastOnce();
        expect(mockSearchRequest.isLoaded()).andReturn(true).atLeastOnce();
        expect(mockSearchRequest.getQuery()).andReturn(query).atLeastOnce();
        final SearchRequestService mockSearchRequestService = createMock(SearchRequestService.class);
        expect(mockSearchRequestService.getFilter(new JiraServiceContextImpl(barney), 888L)).andReturn(mockSearchRequest);

        final SearchService searchService = createMock(SearchService.class);
        expect(searchService.validateQuery(barney, query, 888L)).andReturn(new MessageSetImpl());

        final TableLayoutUtils mockTableLayoutUtils = createMock(TableLayoutUtils.class);
        final List<String> columnNames = asList("status", "priority");

        expect(mockTableLayoutUtils.getColumns(barney, "context", columnNames, true)).andReturn(null);
        replay(mockJiraAuthenticationContext, mockSearchRequest, mockSearchRequestService, mockTableLayoutUtils, searchService);

        IssueTableResource itr = new IssueTableResource(mockJiraAuthenticationContext, searchService, null, mockTableLayoutUtils, mockSearchRequestService, null, null, null, null, null, null, null, null, null)
        {
            @Override
            void addOrderByToSearchRequest(final SearchRequest searchRequest, final String sortBy)
            {
                // do nothing
            }

            @Override
            Response createResponse(final HttpServletRequest request, final IssueTableResource.LinkedLabelledQuery linkedLabelledQuery, final List<ColumnLayoutItem> columns, final boolean isPaging, final int start, final int numberToShow, final boolean enableSorting, final boolean displayHeader, final boolean showActions, final Map<String, String> columnSortJql)
            {
                return Response.ok(new TextMessage("No input validation errors found.")).cacheControl(NO_CACHE).build();
            }
        };
        final Response response = itr.getFilterTable(null, "context", "filter-888", columnNames, null, false, 0, "50", false, false, false, true);
        assertEquals(200, response.getStatus());
        verify(mockJiraAuthenticationContext, mockSearchRequest, mockSearchRequestService, mockTableLayoutUtils, searchService);
    }

    public void testGetFilterTableWithJqlAsFilterId() throws FieldException
    {
        JiraAuthenticationContext mockJiraAuthenticationContext = createMock(JiraAuthenticationContext.class);
        final User barney = new MockUser("barney");
        expect(mockJiraAuthenticationContext.getLoggedInUser()).andReturn(barney).anyTimes();

        final Query query = JqlQueryBuilder.newBuilder().buildQuery();
        final SearchRequestService mockSearchRequestService = createMock(SearchRequestService.class);

        final SearchService searchService = createMock(SearchService.class);
        expect(searchService.parseQuery(barney, "summary ~ Test")).andReturn(new SearchService.ParseResult(query, new MessageSetImpl()));
        expect(searchService.getQueryString(barney, query)).andReturn("summary ~ Test");
        expect(searchService.validateQuery(barney, query, null)).andReturn(new MessageSetImpl());

        final TableLayoutUtils mockTableLayoutUtils = createMock(TableLayoutUtils.class);
        final List<String> columnNames = asList("status", "priority");

        expect(mockTableLayoutUtils.getColumns(barney, "context", columnNames, true)).andReturn(null);
        replay(mockJiraAuthenticationContext, mockSearchRequestService, mockTableLayoutUtils, searchService);

        IssueTableResource itr = new IssueTableResource(mockJiraAuthenticationContext, searchService, null, mockTableLayoutUtils, mockSearchRequestService, null, null, null, null, null, null, null, null, null)
        {
            @Override
            void addOrderByToSearchRequest(final SearchRequest searchRequest, final String sortBy)
            {
                // do nothing
            }

            @Override
            Response createResponse(final HttpServletRequest request, final IssueTableResource.LinkedLabelledQuery linkedLabelledQuery, final List<ColumnLayoutItem> columns, final boolean isPaging, final int start, final int numberToShow, final boolean enableSorting, final boolean displayHeader, final boolean showActions, final Map<String, String> columnSortJql)
            {
                return Response.ok(new TextMessage("No input validation errors found.")).cacheControl(NO_CACHE).build();
            }
        };
        final Response response = itr.getFilterTable(null, "context", "jql-summary ~ Test", columnNames, null, false, 0, "50", false, false, false, true);
        assertEquals(200, response.getStatus());
        verify(mockJiraAuthenticationContext, mockSearchRequestService, mockTableLayoutUtils, searchService);
    }


    public void testGetFilterTableWithValidationFailure() throws FieldException
    {
        JiraAuthenticationContext mockJiraAuthenticationContext = createMock(JiraAuthenticationContext.class);
        final User barney = new MockUser("barney");
        expect(mockJiraAuthenticationContext.getLoggedInUser()).andReturn(barney).anyTimes();
        final Query query = JqlQueryBuilder.newBuilder().buildQuery();
        SearchRequest mockSearchRequest = createMock(SearchRequest.class);
        expect(mockSearchRequest.getQuery()).andReturn(query).atLeastOnce();
        final SearchRequestService mockSearchRequestService = createMock(SearchRequestService.class);
        expect(mockSearchRequestService.getFilter(new JiraServiceContextImpl(barney), 888L)).andReturn(mockSearchRequest);

        final SearchService searchService = createMock(SearchService.class);
        final MessageSetImpl validationMessageSet = new MessageSetImpl();
        validationMessageSet.addErrorMessage("failed to validate");
        expect(searchService.validateQuery(barney, query, 888L)).andReturn(validationMessageSet);

        final TableLayoutUtils mockTableLayoutUtils = createMock(TableLayoutUtils.class);
        final List<String> columnNames = asList("status", "priority");

        replay(mockJiraAuthenticationContext, mockSearchRequest, mockSearchRequestService, mockTableLayoutUtils, searchService);

        IssueTableResource itr = new IssueTableResource(mockJiraAuthenticationContext, searchService, null, mockTableLayoutUtils, mockSearchRequestService, null, null, null, null, null, null, null, null, null)
        {
            @Override
            void addOrderByToSearchRequest(final SearchRequest searchRequest, final String sortBy)
            {
                // do nothing
            }

            @Override
            Response createResponse(final HttpServletRequest request, final IssueTableResource.LinkedLabelledQuery linkedLabelledQuery, final List<ColumnLayoutItem> columns, final boolean isPaging, final int start, final int numberToShow, final boolean enableSorting, final boolean displayHeader, final boolean showActions, Map<String, String> columnSortJql)
            {
                return Response.ok(new TextMessage("No input validation errors found.")).cacheControl(NO_CACHE).build();
            }
        };
        final Response response = itr.getFilterTable(null, "context", "filter-888", columnNames, null, false, 0, "50", false, false, false, false);
        assertEquals(400, response.getStatus());
        verify(mockJiraAuthenticationContext, mockSearchRequest, mockSearchRequestService, mockTableLayoutUtils, searchService);
    }

    public void testGetFilterTableWithJqlAsFilterIdDoesNotParse() throws FieldException
    {
        JiraAuthenticationContext mockJiraAuthenticationContext = createMock(JiraAuthenticationContext.class);
        final User barney = new MockUser("barney");
        expect(mockJiraAuthenticationContext.getLoggedInUser()).andReturn(barney).anyTimes();

        final SearchRequestService mockSearchRequestService = createMock(SearchRequestService.class);

        final MessageSet errors = new MessageSetImpl();
        errors.addErrorMessage("Don't Like You");
        final SearchService searchService = createMock(SearchService.class);
        expect(searchService.parseQuery(barney, "summary ~ Test")).andReturn(new SearchService.ParseResult(null, errors));

        final TableLayoutUtils mockTableLayoutUtils = createMock(TableLayoutUtils.class);
        final List<String> columnNames = asList("status", "priority");

        replay(mockJiraAuthenticationContext, mockSearchRequestService, mockTableLayoutUtils, searchService);

        IssueTableResource itr = new IssueTableResource(mockJiraAuthenticationContext, searchService, null, mockTableLayoutUtils, mockSearchRequestService, null, null, null, null, null, null, null, null, null)
        {
            @Override
            void addOrderByToSearchRequest(final SearchRequest searchRequest, final String sortBy)
            {
                // do nothing
            }

            @Override
            Response createResponse(final HttpServletRequest request, final IssueTableResource.LinkedLabelledQuery linkedLabelledQuery, final List<ColumnLayoutItem> columns, final boolean isPaging, final int start, final int numberToShow, final boolean enableSorting, final boolean displayHeader, final boolean showActions, Map<String, String> columnSortJql)
            {
                return Response.ok(new TextMessage("No input validation errors found.")).cacheControl(NO_CACHE).build();
            }
        };
        final Response response = itr.getFilterTable(null, "context", "jql-summary ~ Test", columnNames, null, false, 0, "50", false, false, false, true);
        assertEquals(400, response.getStatus());
        verify(mockJiraAuthenticationContext, mockSearchRequestService, mockTableLayoutUtils, searchService);
    }

    private void assertSingleError(final Response response, final ValidationError tooHighError)
    {
        assertEquals(400, response.getStatus());
        assertEquals(asList(tooHighError), ((ErrorCollection) response.getEntity()).getErrors());
    }

    private void assertOkValidationResponse(final Response response)
    {
        assertEquals(Response.ok(new TextMessage("No input validation errors found.")).cacheControl(NO_CACHE).build(), response);
    }
}
