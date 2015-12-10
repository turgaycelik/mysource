package com.atlassian.jira.web.servlet;

import java.io.IOException;
import java.util.List;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.servlet.MockHttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertThat;

/**
 * A test for MimeSniffing based on a refectoring of the the previous TestViewAttachmentServletNice
 */
public class TestMimeSniffingKit
{
    private static final String SAMPLE_IE_USER_AGENT = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)";
    private static final String SAMPLE_NON_IE_USER_AGENT = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.9) Gecko/20071025 Firefox/2.0.0.9";

    private static final String UTF_8 = "UTF-8";

    private MockApplicationProperties applicationProperties;

    @Before
    public void setUp() throws Exception
    {
        applicationProperties = new MockApplicationProperties();
        applicationProperties.setEncoding(UTF_8);

        ComponentAccessor.initialiseWorker(new MockComponentWorker()
                .addMock(ApplicationProperties.class, applicationProperties)
        );
    }

    @Test
    public void testGetContentDispositionDefaultPolicy() throws IOException
    {
        testWorkaroundPolicy();
    }

    @Test
    public void testGetContentDispositionWorkaroundPolicy() throws IOException {
        applicationProperties.setString(APKeys.JIRA_OPTION_IE_MIME_SNIFFING, APKeys.MIME_SNIFFING_WORKAROUND);
        testWorkaroundPolicy();
    }

    private void testWorkaroundPolicy() throws IOException
    {
        // responses will fall under one of these 3 options
        final Matcher<MockHttpServletResponse> isInline = hasContentDisposition("inline");
        final Matcher<MockHttpServletResponse> isAttachment = hasContentDisposition("attachment");

        // IE tests
        assertThat(responseFor("dodgysmelly<html", "dafile", "image/gif", SAMPLE_IE_USER_AGENT), isInline);
        assertThat(responseFor("dodgysmelly<script", "dafile", "image/jpeg", SAMPLE_IE_USER_AGENT), isInline);
        assertThat(responseFor("dodgysmelly<table", "dafile", "image/png", SAMPLE_IE_USER_AGENT), isInline);
        assertThat(responseFor("dodgysmelly<plaintext", "dafile", "text/plain", SAMPLE_IE_USER_AGENT), isInline);
        assertThat(responseFor("dodgysmelly<plaintext", "dafile", "whatever/unknown", SAMPLE_IE_USER_AGENT), isInline);
        assertThat(responseFor("no html tags in this file", "dafile", "image/gif", SAMPLE_IE_USER_AGENT), isInline);

        // html always attachment
        assertThat(responseFor("no html tags in this file but mime type is html", "dafile", "text/html", SAMPLE_IE_USER_AGENT), isAttachment);
        assertThat(responseFor("no html tags in this file but mime type is xml", "dafile", "text/xml", SAMPLE_IE_USER_AGENT), isAttachment);
        assertThat(responseFor("no html tags in this file but mime type is xml", "dafile", "application/xml", SAMPLE_IE_USER_AGENT), isAttachment);
        assertThat(responseFor("no html tags in this file but mime type is xml and xhtml", "dafile", "application/xhtml+xml", SAMPLE_IE_USER_AGENT), isAttachment);
        assertThat(responseFor("no html tags in this file but mime type is xhtml", "dafile", "text/xhtml", SAMPLE_IE_USER_AGENT), isAttachment);
        assertThat(responseFor("not much here, but filename has html ext", "dafile.htm", "whatever/something", SAMPLE_IE_USER_AGENT), isAttachment);
        assertThat(responseFor("not much here, but filename has html ext", "dafile.html", "text/plain", SAMPLE_IE_USER_AGENT), isAttachment);

        // non IE tests
        assertThat(responseFor("dodgysmelly<html", "dafile", "image/gif", SAMPLE_NON_IE_USER_AGENT), isInline);
        assertThat(responseFor("dodgysmelly<script", "dafile", "image/jpeg", SAMPLE_NON_IE_USER_AGENT), isInline);
        assertThat(responseFor("dodgysmelly<table", "dafile", "image/png", SAMPLE_NON_IE_USER_AGENT), isInline);
        assertThat(responseFor("dodgysmelly<plaintext", "dafile", "text/plain", SAMPLE_NON_IE_USER_AGENT), isInline);
        assertThat(responseFor("dodgysmelly<plaintext", "dafile", "whatever/unknown", SAMPLE_NON_IE_USER_AGENT), isInline);
        assertThat(responseFor("no html tags in this file", "dafile", "image/gif", SAMPLE_NON_IE_USER_AGENT), isInline);

        // html always attachment
        assertThat(responseFor("no html tags in this file but mime type is html", "dafile", "text/html", SAMPLE_NON_IE_USER_AGENT), isAttachment);
        assertThat(responseFor("not much here, but filename has html ext", "dafile.htm", "whatever/something", SAMPLE_NON_IE_USER_AGENT), isAttachment);
        assertThat(responseFor("not much here, but filename has html ext", "dafile.html", "text/plain", SAMPLE_NON_IE_USER_AGENT), isAttachment);
    }

    private MockHttpServletResponse responseFor(final String fileContents, String filename, String mimetype, String useragent) throws IOException
    {
        final MimeSniffingKit mimeSniffingKit = new MimeSniffingKit(applicationProperties);

        Attachment attachment = getMockAttachment(filename, mimetype);

        MockHttpServletResponse response = new MockHttpServletResponse();

        // run the test and return the response
        mimeSniffingKit.setAttachmentResponseHeaders(attachment, useragent, response);
        return response;
    }

    private Attachment getMockAttachment(final String filename, final String mimeType)
    {
        // just overriding what we need to answer the content disposition call graph
        MockGenericValue mgv = new MockGenericValue("attachment");
        return new Attachment(null, mgv)
        {
            public String getFilename()
            {
                return filename;
            }

            public String getMimetype()
            {
                return mimeType;
            }

            public Issue getIssueObject()
            {
                return null;
            }
        };
    }

    /**
     * Static factory method for HasContentDispositionMatcher.
     *
     * @param contentDisposition the expected content-disposition type (inline or attachment)
     * @return a HasContentDispositionMatcher
     */
    private static Matcher<MockHttpServletResponse> hasContentDisposition(String contentDisposition)
    {
        return new HasContentDispositionMatcher(contentDisposition);
    }

    private static Matcher<MockHttpServletResponse> isForcedDownloadInIE()
    {
        return new HasXDownloadOptionsNoOpen();
    }

    /**
     * Matcher for asserting on the value of the {@code Content-Disposition} HTTP response header.
     */
    private static class HasContentDispositionMatcher extends TypeSafeMatcher<MockHttpServletResponse>
    {
        private final String expected;

        public HasContentDispositionMatcher(String expected)
        {
            this.expected = expected;
        }

        @Override
        protected boolean matchesSafely(MockHttpServletResponse response)
        {
            List<String> values = response.getHeader("Content-Disposition");
            if (values.size() == 1)
            {
                String contentDisposition = values.get(0);

                // we only compare the first part up to the ;
                return expected.equals(StringUtils.split(contentDisposition, ';')[0]);
            }

            return false;
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendText(String.format("a MockHttpServletResponse with Content-Disposition: %s; ...", expected));
        }
    }

    private static class HasXDownloadOptionsNoOpen extends TypeSafeMatcher<MockHttpServletResponse>
    {
        @Override
        protected boolean matchesSafely(MockHttpServletResponse response)
        {
            List<String> values = response.getHeader("X-Download-Options");

            return values.size() == 1 && "noopen".equals(values.get(0));
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendText("a MockHttpServletResponse with X-Download-Options: noopen");
        }
    }
}
