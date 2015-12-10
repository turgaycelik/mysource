package com.atlassian.jira.plugin.issueview;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface IssueViewURLHandler
{
    public static final String NO_HEADERS_PARAMETER = "noResponseHeaders";

    String getURLWithoutContextPath(IssueViewModuleDescriptor moduleDescriptor, String issueKey);

    void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException;
}