package com.atlassian.jira.web.util;

import java.io.IOException;
import java.io.StringWriter;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang.RandomStringUtils;
import org.hamcrest.text.StringContainsInOrder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v6.1
 */
public class TestMetalResourcesManagerImpl
{

    private StringWriter out;
    private MetalResourcesManager manager;
    private String randomTestContextPath;

    @Before
    public void setUp()
    {
        out = new StringWriter();
        randomTestContextPath = RandomStringUtils.randomAlphabetic(10);
        manager = new MetalResourcesManager(randomTestContextPath, out);
    }

    @Test
    public void shouldOutputStyleWithContextPathWhenWritingStyle() throws IOException
    {
        manager.writeStyle("myStyle.css");
        outputContainsInOrder("<link", "text/css", "stylesheet", randomTestContextPath + "/static-assets/myStyle.css");
    }

    @Test
    public void shouldOutputScriptWithContextPathWhenWritingScript() throws Exception
    {
        manager.writeScript("myScript.js");
        outputContainsInOrder("<script src=", randomTestContextPath + "/static-assets/myScript.js", "</script>");
    }

    @Test
    public void shouldOutputConditionWhenIfing() throws Exception
    {
        manager.startIf("my condition").endIf();
        outputContainsInOrder("<!--[if my condition]><![endif]-->");
    }

    private void outputContainsInOrder(String... order)
    {
        String output = out.toString();
        Assert.assertThat(output, StringContainsInOrder.stringContainsInOrder(ImmutableList.copyOf(order)));
    }


    /**
     * STATIC UTILS TESTS
     */

    @Test
    public void shouldIncludeDefaultStylesAndScriptsWhenIncludingMetal() throws IOException
    {
        MetalResourcesManager managerMock = mockMetalResources();

        MetalResourcesManager.includeMetalResources(managerMock);

        verify(managerMock).writeStyle("metal-all.css");
        verify(managerMock).writeScript("metal-all.js");
    }

    @Test
    public void shouldIncludeConditionalIEStylesWhenIncludingMetal() throws IOException
    {
        MetalResourcesManager managerMock = mockMetalResources();

        MetalResourcesManager.includeMetalResources(managerMock);

        InOrder inOrder = inOrder(managerMock);
        inOrder.verify(managerMock).startIf("lt IE 9");
        inOrder.verify(managerMock).writeStyle("metal-all-ie.css");
        inOrder.verify(managerMock).endIf();
    }

    @Test
    public void shouldIncludeConditionalIE9StylesWhenIncludingMetal() throws IOException
    {
        MetalResourcesManager managerMock = mockMetalResources();
        MetalResourcesManager.includeMetalResources(managerMock);

        InOrder inOrder = inOrder(managerMock);
        inOrder.verify(managerMock).startIf("IE 9");
        inOrder.verify(managerMock).writeStyle("metal-all-ie9.css");
        inOrder.verify(managerMock).endIf();
    }

    @Test
    public void shouldIncludeConditionalIEScriptsWhenIncludingMetal() throws IOException
    {
        MetalResourcesManager managerMock = mockMetalResources();
        MetalResourcesManager.includeMetalResources(managerMock);

        InOrder inOrder = inOrder(managerMock);
        inOrder.verify(managerMock).startIf("lt IE 9");
        inOrder.verify(managerMock).writeScript("metal-all-ie.js");
        inOrder.verify(managerMock).endIf();
    }

    private MetalResourcesManager mockMetalResources() throws IOException
    {
        MetalResourcesManager managerMock = Mockito.mock(MetalResourcesManager.class);
        when(managerMock.writeStyle(anyString())).thenReturn(managerMock);
        when(managerMock.writeScript(anyString())).thenReturn(managerMock);
        when(managerMock.startIf(anyString())).thenReturn(managerMock);
        when(managerMock.endIf()).thenReturn(managerMock);
        return managerMock;

    }


}
