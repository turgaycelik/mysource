package com.atlassian.jira.web.tags;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import webwork.util.ServletValueStack;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestStripHtmlMarkup
{
    private static final String PLAIN_TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
    private static final String HTML_TEXT = "Lorem <em>ipsum</em> dolor sit <script></script>amet, consectetur adipiscing elit.";

    private final PageContext pageContext = mock(PageContext.class);
    private final JspWriter jspWriter = mock(JspWriter.class);
    private final ServletValueStack stack = mock(ServletValueStack.class);

    private final StripHtmlMarkup tag = new StripHtmlMarkup();

    @Before
    public void setUpPageContext()
    {
        tag.setPageContext(pageContext);
        when(pageContext.getOut()).thenReturn(jspWriter);
        final ServletRequest request = mock(ServletRequest.class);
        when(request.getAttribute(anyString())).thenReturn(stack);
        when(pageContext.getRequest()).thenReturn(request);
    }

    @Test
    public void gracefullyHandleRuntimeException()
    {
        when(pageContext.getOut()).thenThrow(new RuntimeException("catch me if you can"));
        expectEndTagSuccessful();
    }

    @Test
    public void gracefullyHandleCheckedException() throws IOException
    {
        doThrow(new IOException("catch me if you can")).when(jspWriter).write(anyString());
        expectEndTagSuccessful();
    }

    @Test
    public void nullValueReplacedByEmptyString() throws IOException
    {
        givenValue(null);
        thenExpectOutput("");
    }

    @Test
    public void contentIsEscaped() throws IOException
    {
        givenValue("\"");
        thenExpectOutput("&quot;");
    }

    @Test
    public void plainText() throws IOException
    {
        givenValue(PLAIN_TEXT);
        thenExpectOutput(PLAIN_TEXT);
    }

    @Test
    public void htmlText() throws IOException
    {
        givenValue(HTML_TEXT);
        thenExpectOutput(PLAIN_TEXT);
    }

    @Test
    public void brokenHtmlText() throws IOException
    {
        givenValue("broken <html>foo");
        thenExpectOutput("broken foo");
    }

    private void givenValue(final String value)
    {
        when(stack.findValue(value)).thenReturn(value);
        tag.setValue(value);
    }

    private void thenExpectOutput(final String output) throws IOException
    {
        expectEndTagSuccessful();
        verify(jspWriter).write(output);
    }

    private void expectEndTagSuccessful()
    {
        final int result = tag.doEndTag();
        assertThat(result, Matchers.is(BodyTagSupport.EVAL_PAGE));
    }

}
