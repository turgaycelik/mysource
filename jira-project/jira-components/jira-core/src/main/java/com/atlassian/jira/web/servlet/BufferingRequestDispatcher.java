package com.atlassian.jira.web.servlet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Dispatches requests, buffering the contents of the response and returning it as a String.  This is needed because
 * JIRA's velocity manager doesn't allow writing to a writer, it only returns the content as a String, so the JSP can't
 * write directly to the responses writer.  This class is intended for use in velocity templates to include JSPs.
 */
public class BufferingRequestDispatcher
{
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    public BufferingRequestDispatcher(HttpServletRequest request, HttpServletResponse response)
    {
        this.request = request;
        this.response = response;
    }

    /**
     * Return the contents of the given path.  This is called include because its functionally the same as a jsp:include
     * when used in a velocity template.
     *
     * @param path The path to dispatch the request to.
     * @return A CharArrayWriter with the output of the JSP.  This is a little more efficient than returning a String,
     *         because you can use CharArrayWriter.writeTo() to write the output directly to another writer.
     * @throws ServletException If an error occured dispatching the request.
     * @throws IOException If an error occured dispatching the request.
     */
    public CharArrayWriter include(String path) throws ServletException, IOException
    {
        RequestDispatcher requestDispatcher = request.getRequestDispatcher(path);
        CharArrayWriter writer = new CharArrayWriter();
        final PrintWriter printWriter = new PrintWriter(writer);
        requestDispatcher.include(request, new HttpServletResponseWrapper(response)
        {
            // This will only work if we forward to servlets that use the writer.  For JSPs, this is always the case.
            public PrintWriter getWriter()
            {
                return printWriter;
            }
            public ServletOutputStream getOutputStream()
            {
                throw new UnsupportedOperationException("The BufferingRequestDispatcher only supports writing to the " +
                    "responses writer. Please use getWriter() instead.");
            }
            
        });
        return writer;
    }

}
