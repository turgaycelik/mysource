package com.atlassian.jira.web.servlet;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.util.IOUtil;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.util.HostileAttachmentsHelper;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * This class can sniff a file according to the current JIRA settings and determine how an attachment should be handled
 *
 * @since v4.1
 */
public class MimeSniffingKit
{
    public static final String CONTENT_DISPOSITION_ATTACHMENT = "attachment";
    public static final String CONTENT_DISPOSITION_INLINE = "inline";

    private static final Logger log = Logger.getLogger(MimeSniffingKit.class);
    private final ApplicationProperties applicationProperties;
    private HostileAttachmentsHelper attachmentHelper;

    public MimeSniffingKit(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
        this.attachmentHelper = new HostileAttachmentsHelper();
    }

    /**
     * Sets the appropriate HTTP response headers on an attachment download response. Depending on the JIRA security
     * settings and the browser making the request, this can contain headers such as {@code Content-Disposition} and
     * {@code X-Download-Options} to force downloading rather than opening attachments.
     *
     * @param attachment the Attachment in play
     * @param userAgent    the User-agent request header
     * @param httpServletResponse the attachment download response
     * @throws IOException if stuff goes wrong
     */
    public void setAttachmentResponseHeaders(final Attachment attachment, final String userAgent,
            final HttpServletResponse httpServletResponse) throws IOException
    {
        String filename = attachment.getFilename();
        BufferedInputStream inputStream = null;

        try
        {
            OpenAttachmentStrategy strategy = getOpenAttachmentStrategy(filename, attachment.getMimetype());
            strategy.setResponseHeaders(httpServletResponse);
        }
        finally
        {
            if (inputStream != null)
            {
                IOUtil.shutdownStream(inputStream);
            }
        }
    }

    /**
     * Sets the appropriate HTTP response headers on an attachment download response. Depending on the JIRA security
     * settings and the browser making the request, this can contain headers such as {@code Content-Disposition} and
     * {@code X-Download-Options} to force downloading rather than opening attachments.
     *
     * @param fileName the name of the file
     * @param mimeContentType the content-type of the file
     * @param inputStream the input stream that is being downloaded
     * @param userAgent    the User-agent request header
     * @param httpServletResponse the attachment download response
     * @throws IOException if stuff goes wrong
     */
    public void setAttachmentResponseHeaders(final String fileName, final String mimeContentType, final String userAgent,
            final BufferedInputStream inputStream, final HttpServletResponse httpServletResponse) throws IOException
    {
        OpenAttachmentStrategy strategy = getOpenAttachmentStrategy(fileName, mimeContentType);
        strategy.setResponseHeaders(httpServletResponse);
    }

    private void setContentDispositionOnResponse(HttpServletResponse httpServletResponse, String filename, String contentDisposition)
    {
        String filenameEncoding = applicationProperties.getEncoding();

        // note the special *= syntax is used for embedding the encoding that the filename is in as per RFC 2231
        // http://www.faqs.org/rfcs/rfc2231.html
        httpServletResponse.setHeader("Content-Disposition", String.format("%s; filename*=%s''%s;", contentDisposition, filenameEncoding, JiraUrlCodec.encode(filename, true)));
    }

    /**
     * This will suggest a content disposition type (inline or attachment) for the given file, respecting the settings
     * in JIRA and taking IE badness into account.
     *
     * @param fileName        the name of the file
     * @param mimeContentType the exisiting content type
     *
     * @return either INLINE or ATTACHMENT ready for a Content-Disposition header
     *
     * @throws IOException if stuff goes wrong
     */
    @Nonnull
    private OpenAttachmentStrategy getOpenAttachmentStrategy(final String fileName, final String mimeContentType)
            throws IOException
    {
        String mimeSniffingPolicy = getMimeSniffingPolicy();
        boolean forceDownload = false;
        if (log.isDebugEnabled() && mimeSniffingPolicy.equalsIgnoreCase(APKeys.MIME_SNIFFING_OWNED))
        {
            log.debug("Mime sniffing policy is insecure, attachment will always be displayed inline");
        }
        if (!mimeSniffingPolicy.equalsIgnoreCase(APKeys.MIME_SNIFFING_OWNED) && isExecutableContent(fileName, mimeContentType))
        {
            // only in owned mode we allow inline html
            forceDownload = true;
            if (log.isDebugEnabled())
            {
                log.debug("Attachment \"" + fileName + "\" (" + mimeContentType + ")" + " presents as executable content, forcing download.");
            }
        }
        else if (mimeSniffingPolicy.equalsIgnoreCase(APKeys.MIME_SNIFFING_PARANOID))
        {
            forceDownload = true;
        }

        return forceDownload ? new ForceDownload(fileName) : new ShowInline(fileName);
    }

    boolean isExecutableContent(String name, String contentType)
    {
        return attachmentHelper.isExecutableFileExtension(name) || attachmentHelper.isExecutableContentType(contentType);
    }


    private String getMimeSniffingPolicy()
    {
        String mimeSniffingPolicy = applicationProperties.getDefaultBackedString(APKeys.JIRA_OPTION_IE_MIME_SNIFFING);
        if (mimeSniffingPolicy == null)
        {
            mimeSniffingPolicy = APKeys.MIME_SNIFFING_WORKAROUND; // hard-coded default
            log.warn("Missing MIME sniffing policy application property " + APKeys.JIRA_OPTION_IE_MIME_SNIFFING
                    + " ! Defaulting to " + APKeys.MIME_SNIFFING_WORKAROUND);
        }
        if (!(APKeys.MIME_SNIFFING_OWNED.equalsIgnoreCase(mimeSniffingPolicy)
                || APKeys.MIME_SNIFFING_PARANOID.equalsIgnoreCase(mimeSniffingPolicy)
                || APKeys.MIME_SNIFFING_WORKAROUND.equalsIgnoreCase(mimeSniffingPolicy)))
        {
            log.warn("MIME sniffing policy application property is invalid: " + mimeSniffingPolicy
                    + " ! Defaulting to " + APKeys.MIME_SNIFFING_WORKAROUND);
            mimeSniffingPolicy = APKeys.MIME_SNIFFING_WORKAROUND; // hard-coded default
        }
        return mimeSniffingPolicy;
    }

    /**
     * Strategy for opening attachments.
     */
    private abstract class OpenAttachmentStrategy
    {
        final String filename;

        public OpenAttachmentStrategy(String filename)
        {
            this.filename = Assertions.notNull(filename);
        }

        abstract void setResponseHeaders(HttpServletResponse httpServletResponse);
    }

    /**
     * Instructs the browser to display the attachment in-line.
     */
    private class ShowInline extends OpenAttachmentStrategy
    {
        public ShowInline(String fileName) { super(fileName); }

        @Override
        public void setResponseHeaders(HttpServletResponse httpServletResponse)
        {
            setContentDispositionOnResponse(httpServletResponse, filename, CONTENT_DISPOSITION_INLINE);
        }
    }

    /**
     * Instructs the browser to save the attachment.
     */
    private class ForceDownload extends OpenAttachmentStrategy
    {
        private ForceDownload(String filename) {
            super(filename);
        }

        @Override
        public void setResponseHeaders(HttpServletResponse httpServletResponse)
        {
            setContentDispositionOnResponse(httpServletResponse, filename, CONTENT_DISPOSITION_ATTACHMENT);

            // keeps IE8 from displaying an HTML file inline, thereby preventing XSS attacks using HTML attachments
            httpServletResponse.setHeader("X-Download-Options", "noopen");
        }
    }
}
