package com.atlassian.jira.web.util;

import com.atlassian.jira.util.BuildUtilsInfo;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestProductVersionDataBean
{
    @Test
    public void testSimple() {
        BuildUtilsInfo info = mock(BuildUtilsInfo.class);
        when(info.getVersion()).thenReturn("1.2.5-m1");
        ProductVersionDataBean bean = new ProductVersionDataBean(info);
        assertEquals("<meta name=\"application-name\" content=\"JIRA\" data-name=\"jira\" data-version=\"1.2.5-m1\">", bean.getMetaTags());
        assertEquals("data-version=\"1.2.5-m1\"", bean.getBodyHtmlAttributes());
    }
    @Test
    public void testXSS() {
        BuildUtilsInfo info = mock(BuildUtilsInfo.class);
        when(info.getVersion()).thenReturn("1.2.<5-m1");
        ProductVersionDataBean bean = new ProductVersionDataBean(info);
        assertEquals("<meta name=\"application-name\" content=\"JIRA\" data-name=\"jira\" data-version=\"1.2.&lt;5-m1\">", bean.getMetaTags());
        assertEquals("data-version=\"1.2.&lt;5-m1\"", bean.getBodyHtmlAttributes());
    }

}
