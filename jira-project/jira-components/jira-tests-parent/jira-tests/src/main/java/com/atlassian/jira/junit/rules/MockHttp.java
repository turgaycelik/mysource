package com.atlassian.jira.junit.rules;

import com.atlassian.jira.mock.servlet.MockHttpServletRequest;
import com.atlassian.jira.mock.servlet.MockHttpServletResponse;
import com.atlassian.jira.web.ExecutingHttpRequest;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.mockito.Mockito;
import webwork.action.ActionContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * <p/>
 * Sets up and tears down automatically the following, static methods used in the JIRA production code for
 * accessing current HTTP objects:
 * <ul>
 *     <li>{@link webwork.action.ActionContext}</li>
 *     <li>{@link com.atlassian.jira.web.ExecutingHttpRequest}</li>
 * </ul>
 *
 *
 *
 * @since 6.0
 */
public class MockHttp<R extends HttpServletRequest, S extends HttpServletResponse> extends TestWatcher
{
    public static class MockitoMocks extends MockHttp<HttpServletRequest,HttpServletResponse>
    {

        private MockitoMocks(HttpServletRequest mockRequest, HttpServletResponse mockResponse)
        {
            super(mockRequest, mockResponse);
        }
    }

    public static class DefaultMocks extends MockHttp<MockHttpServletRequest,MockHttpServletResponse>
    {

        private DefaultMocks(MockHttpServletRequest mockRequest, MockHttpServletResponse mockResponse)
        {
            super(mockRequest, mockResponse);
        }
    }

    public static <RR extends HttpServletRequest, RS extends HttpServletResponse> MockHttp<RR,RS> withMocks(
            RR mockRequest, RS mockResponse)
    {
        return new MockHttp<RR,RS>(mockRequest, mockResponse);
    }

    public static DefaultMocks withDefaultMocks()
    {
        return new DefaultMocks(new MockHttpServletRequest(), new MockHttpServletResponse());
    }

    public static MockitoMocks withMockitoMocks()
    {
        return new MockitoMocks(Mockito.mock(HttpServletRequest.class), Mockito.mock(HttpServletResponse.class));
    }

    private final R mockRequest;
    private final S mockResponse;

    private MockHttp(R mockRequest, S mockResponse)
    {
        this.mockRequest = notNull("mockRequest", mockRequest);
        this.mockResponse = notNull("mockResponse", mockResponse);
    }

    @Override
    protected void starting(Description description)
    {
        ActionContext.setRequest(mockRequest);
        ActionContext.setResponse(mockResponse);
        ExecutingHttpRequest.set(mockRequest, mockResponse);
    }

    @Override
    protected void finished(Description description)
    {
        ActionContext.setRequest(null);
        ActionContext.setResponse(null);
        ExecutingHttpRequest.clear();
    }

    public R mockRequest()
    {
        return mockRequest;
    }

    public S mockResponse()
    {
        return mockResponse;
    }
}
