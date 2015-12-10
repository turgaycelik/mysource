package com.atlassian.jira.util.http.response;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.Cookie;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.mock.servlet.MockHttpServletResponse;
import com.atlassian.jira.mock.servlet.MockServletOutputStream;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 */
public class TestCapturingResponseWrapper
{
    private static final String HOW_COOL_IS_BRAD = "How cool is brad?";

    @Test
    public void testGeneralByteCaptureWithWriter() throws IOException
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        MockHttpServletResponse underlyingResponse = new MockHttpServletResponse(pw);

        CapturingResponseWrapper wrapper = new CapturingResponseWrapper(underlyingResponse, 100);

        wrapper.getWriter().print(HOW_COOL_IS_BRAD);

        assertEquals(HOW_COOL_IS_BRAD.length(), wrapper.size());
        assertEquals(HOW_COOL_IS_BRAD, new String(wrapper.getBytes()));
    }

    @Test
    public void testGeneralByteCaptureWithServletOutputStream() throws IOException
    {
        StringWriter sw = new StringWriter();
        MockServletOutputStream outputStream = new MockServletOutputStream(sw);

        MockHttpServletResponse underlyingResponse = new MockHttpServletResponse(outputStream);

        CapturingResponseWrapper wrapper = new CapturingResponseWrapper(underlyingResponse, 100);

        wrapper.getOutputStream().print(HOW_COOL_IS_BRAD);

        assertEquals(HOW_COOL_IS_BRAD.length(), wrapper.size());
        assertEquals(HOW_COOL_IS_BRAD, new String(wrapper.getBytes()));
    }

    @Test
    public void testMaxSize() throws IOException
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        MockHttpServletResponse underlyingResponse = new MockHttpServletResponse(pw);

        CapturingResponseWrapper wrapper = new CapturingResponseWrapper(underlyingResponse, 5);

        wrapper.getWriter().print(HOW_COOL_IS_BRAD);

        assertEquals(5, wrapper.size());
        assertEquals(HOW_COOL_IS_BRAD.substring(0,5), new String(wrapper.getBytes()));

    }

    @Test
    public void testCookieCapture()
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        MockHttpServletResponse underlyingResponse = new MockHttpServletResponse(pw);

        CapturingResponseWrapper wrapper = new CapturingResponseWrapper(underlyingResponse, 100);

        List expectedList = EasyList.build(new Cookie("abc","xyz"), new Cookie("123","456")); 
        wrapper.addCookie((Cookie) expectedList.get(0));
        wrapper.addCookie((Cookie) expectedList.get(1));

        List actualList = wrapper.getCookieList();
        assertEquals(expectedList,actualList);
    }

    @Test
    public void testHeaderCapture()
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        MockHttpServletResponse underlyingResponse = new MockHttpServletResponse(pw);

        CapturingResponseWrapper wrapper = new CapturingResponseWrapper(underlyingResponse, 100);
        wrapper.addHeader("strHeader","val1");

        List headerList = wrapper.getHeaderList();
        assertNotNull(headerList);
        assertEquals(1, headerList.size());
        CapturingResponseWrapper.HttpHeader httpHeader = (CapturingResponseWrapper.HttpHeader) headerList.get(0);
        assertEquals("strHeader",httpHeader.getName());
        assertEquals("val1",httpHeader.getValue());

        wrapper.addIntHeader("intHeader", 666);

        headerList = wrapper.getHeaderList();
        assertNotNull(headerList);
        assertEquals(2, headerList.size());
        httpHeader = (CapturingResponseWrapper.HttpHeader) headerList.get(1);
        assertEquals("intHeader",httpHeader.getName());
        assertEquals("666",httpHeader.getValue());

        Date now = new Date();
        String dateStr = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z").format(now);

        wrapper.addDateHeader("dateHeader", now.getTime());

        headerList = wrapper.getHeaderList();
        assertNotNull(headerList);
        assertEquals(3, headerList.size());
        httpHeader = (CapturingResponseWrapper.HttpHeader) headerList.get(2);
        assertEquals("dateHeader",httpHeader.getName());
        assertEquals(dateStr,httpHeader.getValue());

        // now test the setting of values
        wrapper.setHeader("strHeader", "val2");
        headerList = wrapper.getHeaderList();
        httpHeader = (CapturingResponseWrapper.HttpHeader) headerList.get(0);
        assertEquals("strHeader",httpHeader.getName());
        assertEquals("val2",httpHeader.getValue());


        wrapper.setIntHeader("intHeader", 999);

        headerList = wrapper.getHeaderList();
        httpHeader = (CapturingResponseWrapper.HttpHeader) headerList.get(1);
        assertEquals("intHeader",httpHeader.getName());
        assertEquals("999",httpHeader.getValue());


        now = new Date();
        dateStr = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z").format(now);
        wrapper.setDateHeader("dateHeader", now.getTime());

        headerList = wrapper.getHeaderList();
        httpHeader = (CapturingResponseWrapper.HttpHeader) headerList.get(2);
        assertEquals("dateHeader",httpHeader.getName());
        assertEquals(dateStr,httpHeader.getValue());

        // and add via a set
        wrapper.setHeader("addHeader", "added");
        headerList = wrapper.getHeaderList();
        assertEquals(4, headerList.size());
        httpHeader = (CapturingResponseWrapper.HttpHeader) headerList.get(3);
        assertEquals("addHeader",httpHeader.getName());
        assertEquals("added",httpHeader.getValue());

    }
}
