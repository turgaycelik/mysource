package com.atlassian.jira.web.servlet;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.attachment.AttachmentZipFileCreator;
import com.atlassian.jira.issue.attachment.AttachmentZipKit;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.AttachmentUtils;
import com.atlassian.jira.util.BrowserUtils;
import com.atlassian.jira.util.IOUtil;
import com.atlassian.jira.util.http.JiraHttpUtils;
import com.atlassian.jira.util.mime.MimeManager;
import com.atlassian.jira.web.exception.WebExceptionChecker;
import com.atlassian.seraph.util.RedirectUtils;
import org.apache.log4j.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This servlet can BUNDLE up all the attachments of a in issue into 1 ZIP file OR it can unzip a specific entry of a
 * named attachment
 *
 * @since 4.1
 */
public class AttachmentZipServlet extends HttpServlet
{
    private static final Logger log = Logger.getLogger(AttachmentZipServlet.class);

    private static final String SECURE_VIEWS_SECURITYBREACH_JSP = "/secure/views/securitybreach.jsp";
    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    private static final Pattern ISSUE_ID_ONLY = Pattern.compile(".+/([0-9]+)\\.zip");
    private static final Pattern ISSUE_ID_AND_ZIP = Pattern.compile(".+/unzip/([0-9]+)/([0-9]+)(\\[|%5B)([0-9]+)(\\]|%5D)/.*");

    @Override
    protected void doGet(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse)
            throws ServletException, IOException
    {
        // check that attachments and zip support is enabled
        if (!checkSupportEnabled())
        {
            httpServletResponse.sendError(404, "Attachments as ZIP support is disabled");
            return;
        }
        Issue issue;
        final String uri = httpServletRequest.getRequestURI();
        try
        {
            // this does permission checks as well
            issue = getIssue(uri);
        }
        catch (PermissionException e)
        {
            redirectForSecurityBreach(httpServletRequest, httpServletResponse);
            return;
        }
        if (issue == null)
        {
            httpServletResponse.sendError(404, "Could not find issue");
            return;
        }
        if (uri.contains("unzip"))
        {
            unzipSpecifiedAttachment(httpServletRequest, httpServletResponse, issue, uri);
        }
        else
        {
            zipAllAttachments(httpServletRequest, httpServletResponse, issue);
        }
    }

    private void zipAllAttachments(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse, final Issue issue)
            throws IOException, ServletException
    {
        String issueKey = issue.getKey();
        File zipFile;
        try
        {
            zipFile = createAttachmentsZipFile(issue);
        }
        catch (IOException e)
        {
            log.error("Can not create temporary zip file : " + httpServletRequest.getPathInfo() + ": " + e.getMessage(), e);
            httpServletResponse.sendError(404, "Could not create zip file for issue : " + issueKey);
            return;
        }

        try
        {
            setResponseHeaders(httpServletRequest, httpServletResponse, zipFile, issueKey);
            writeZipResponse(httpServletResponse, new FileInputStream(zipFile));
        }
        catch (Exception e)
        {
            if (WebExceptionChecker.canBeSafelyIgnored(e))
            {
                return;
            }
            // now send a 404 only if we have not yet written any bytes to the out stream
            if (!httpServletResponse.isCommitted())
            {
                httpServletResponse.sendError(404, "Could not serve zip file of attachments for issue " + issueKey + " : " + e.getMessage());
            }
            else
            {
                throw new ServletException("Could not serve zip file of attachments for issue " + issueKey, e);
            }
        }
        finally
        {
            deleteFile(zipFile);
        }
    }

    private void unzipSpecifiedAttachment(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse, final Issue issue, final String uri)
            throws IOException
    {
        String issueKey = issue.getKey();
        Matcher matcher = ISSUE_ID_AND_ZIP.matcher(uri);
        if (!matcher.find() || matcher.groupCount() != 5)
        {
            httpServletResponse.sendError(404, "Could not create zip file for issue : " + issueKey);
            return;
        }
        long attachmentId = Long.parseLong(matcher.group(2));
        int entryIndex = Integer.parseInt(matcher.group(4));

        AttachmentZipKit zipKit = new AttachmentZipKit();
        File zipFile = getFileFor(issue, attachmentId);
        if (!zipKit.isZip(zipFile))
        {
            httpServletResponse.sendError(404, "The attachment is not a zip file: " + issueKey + " : " + attachmentId);
            return;
        }
        streamSpecificZipEntry(httpServletRequest, httpServletResponse, entryIndex, zipKit, zipFile);
    }

    private void streamSpecificZipEntry(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse,
            final int entryIndex, final AttachmentZipKit zipKit, final File zipFile) throws IOException
    {
        BufferedInputStream bufferedZipEntryInputStream = null;
        try
        {
            final AttachmentZipKit.ZipEntryInputStream zipEntryInputStream = zipKit.extractFile(zipFile, entryIndex);
            bufferedZipEntryInputStream = new BufferedInputStream(zipEntryInputStream);
            sniffContentAndSetZipEntryResponseHeaders(httpServletRequest, httpServletResponse, bufferedZipEntryInputStream,
                    zipEntryInputStream.getZipEntry().getName(), zipEntryInputStream.getZipEntry().getSize());
            IOUtil.copy(bufferedZipEntryInputStream, httpServletResponse.getOutputStream());
        }
        finally
        {
            IOUtil.shutdownStream(bufferedZipEntryInputStream);
        }
    }

    private void sniffContentAndSetZipEntryResponseHeaders(final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse, final BufferedInputStream zipEntryInputStream,
            final String zipEntryName, final long zipEntrySize)
            throws IOException
    {
        MimeManager mimeManager = ComponentAccessor.getComponent(MimeManager.class);

        String suggestedContentType = mimeManager.getSanitisedMimeType(APPLICATION_OCTET_STREAM, zipEntryName);

        setFileDownloadHeaders(httpServletRequest, httpServletResponse, zipEntryInputStream, zipEntrySize, zipEntryName, suggestedContentType);
    }

    private File getFileFor(final Issue issue, final long attachmentId)
    {
        for (final Object o : issue.getAttachments())
        {
            Attachment attachment = (Attachment) o;
            if (attachment.getId().equals(attachmentId))
            {
                return AttachmentUtils.getAttachmentFile(attachment);
            }
        }
        return null;
    }

    private void redirectForSecurityBreach(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse)
            throws ServletException, IOException
    {
        if (getLoggedInUser() != null)
        {
            RequestDispatcher rd = httpServletRequest.getRequestDispatcher(SECURE_VIEWS_SECURITYBREACH_JSP);
            JiraHttpUtils.setNoCacheHeaders(httpServletResponse);
            rd.forward(httpServletRequest, httpServletResponse);
        }
        else
        {
            httpServletResponse.sendRedirect(RedirectUtils.getLoginUrl(httpServletRequest));
        }
    }

    /**
     * The URL pattern is either /attachmentzip/1234 or attachmentzip/unzip/1234/4567[n]
     *
     * @param url the url in play
     * @return the parsed issue from it or PermissionException
     * @throws PermissionException if the current user cant see the issue
     */

    private Issue getIssue(final String url) throws PermissionException
    {
        Matcher matcher = ISSUE_ID_ONLY.matcher(url);
        String issueId = null;
        if (matcher.find())
        {
            issueId = matcher.group(1);
        }
        else
        {
            matcher = ISSUE_ID_AND_ZIP.matcher(url);
            // we must have BOTH bits considered good
            if (matcher.find() && matcher.groupCount() == 5)
            {
                issueId = matcher.group(1);
            }

        }
        return parseForIssue(issueId);
    }

    private Issue parseForIssue(final String issueId) throws PermissionException
    {
        if (issueId == null)
        {
            return null;
        }
        try
        {
            Long id = Long.parseLong(issueId);
            Issue issue = ComponentAccessor.getIssueManager().getIssueObject(id);
            if (issue != null)
            {
                // ok does the user have permission to see that issue
                if (!hasPermissionToViewAttachment(issue))
                {
                    throw new PermissionException("The user does not have permission to see this issue");
                }
            }
            return issue;
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private File createAttachmentsZipFile(final Issue issue) throws IOException
    {
        AttachmentZipFileCreator zipFileCreator = new AttachmentZipFileCreator(issue);
        return zipFileCreator.toZipFile();
    }

    private boolean writeZipResponse(final HttpServletResponse httpServletResponse, final InputStream inputStream)
            throws IOException
    {
        boolean bytesWritten = false;
        OutputStream out = httpServletResponse.getOutputStream();

        byte[] buffer = new byte[4096];
        try
        {
            while (true)
            {
                int bytesRead = inputStream.read(buffer);
                if (bytesRead == -1)
                {
                    break;
                }
                out.write(buffer, 0, bytesRead);
                bytesWritten = true;
            }
        }
        finally
        {
            IOUtil.shutdownStream(inputStream);
            IOUtil.shutdownStream(out);
        }
        return bytesWritten;
    }

    /**
     * Sets the content type, content length and "Content-Disposition" header of the response
     *
     * @param request the HTTP request
     * @param response HTTP response
     * @param file the zip file created
     * @param issueKey the issue key in play
     * @throws java.io.IOException if the stream cant be written to
     */
    private void setResponseHeaders(final HttpServletRequest request, final HttpServletResponse response,
            final File file, final String issueKey) throws IOException
    {
        setFileDownloadHeaders(request, response, new BufferedInputStream(new FileInputStream(file)),
                file.length(), issueKey + ".zip", "application/zip");
    }

    private void setFileDownloadHeaders(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse,
            final BufferedInputStream inputStream, final long fileSize, final String fileName, final String contentType)
            throws IOException
    {
        httpServletResponse.setContentType(contentType);
        httpServletResponse.setContentLength((int) fileSize);

        MimeSniffingKit sniffingKit = ComponentAccessor.getComponent(MimeSniffingKit.class);
        String userAgent = httpServletRequest.getHeader(BrowserUtils.USER_AGENT_HEADER);

        // sets the relevant headers
        sniffingKit.setAttachmentResponseHeaders(fileName, contentType, userAgent, inputStream, httpServletResponse);
    }

    /**
     * Checks if the given user had permission to see the attachments for an issue.
     *
     * @param issue the issue in play
     * @return true if user can see the attachments, false otherwise
     * @throws com.atlassian.jira.exception.DataAccessException because of bad JIRA design
     */
    private boolean hasPermissionToViewAttachment(Issue issue) throws DataAccessException
    {
        return ComponentAccessor.getPermissionManager().hasPermission(Permissions.BROWSE, issue, getLoggedInUser());
    }

    @SuppressWarnings ("ResultOfMethodCallIgnored")
    private void deleteFile(final File zipFile)
    {
        if (zipFile != null)
        {
            zipFile.delete();
        }
    }


    private boolean checkSupportEnabled()
    {
        ApplicationProperties ap = getApplicationProperties();
        return ap.getOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS) && ap.getOption(APKeys.JIRA_OPTION_ALLOW_ZIP_SUPPORT);
    }

    ApplicationProperties getApplicationProperties()
    {
        return ComponentAccessor.getApplicationProperties();
    }

    protected User getLoggedInUser()
    {
        return ComponentAccessor.getComponent(JiraAuthenticationContext.class).getLoggedInUser();
    }
}
