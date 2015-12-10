package com.atlassian.jira.rest.util;

import com.atlassian.core.util.FileSize;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.XsrfCheckResult;
import com.atlassian.jira.security.xsrf.XsrfInvocationChecker;
import org.apache.commons.lang.StringUtils;
import webwork.config.Configuration;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

/**
 * @since v5.0
 */
public class AttachmentHelper
{
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final XsrfInvocationChecker xsrfChecker;
    private final JiraAuthenticationContext authenticationContext;

    public AttachmentHelper(XsrfInvocationChecker xsrfChecker, JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.xsrfChecker = xsrfChecker;
        this.authenticationContext = jiraAuthenticationContext;
    }


    /**
     * Validates the uploaded image. Validation includes
     *
     * - XSRF token check
     * - Filename check (is it blank)
     * - Size is not valid (Either not supplied by the client or is 0)
     * - Filesize is above the max attachment size
     *
     * @param request where the image data is
     * @param filename filename of image
     * @param size size of image
     * 
     * @return Validation result
     */
    public ValidationResult validate(final HttpServletRequest request, final String filename, @Nullable Long size)
    {
        InputStream inputStream;
        String contentType;

        final XsrfCheckResult xsrfCheckResult = xsrfChecker.checkWebRequestInvocation(request);
        if (xsrfCheckResult.isRequired() && !xsrfCheckResult.isValid())
        {
            return new ValidationResult(ValidationError.XSRF_TOKEN_INVALID, getText("xsrf.error.title"));

        }
        if (StringUtils.isBlank(filename))
        {
            return new ValidationResult(ValidationError.FILENAME_BLANK, null);
        }

        if (size == null || size < 0)
        {
            size = (long) request.getContentLength();

            if (size < 0)
            {
                return new ValidationResult(ValidationError.ATTACHMENT_IO_SIZE, getText("attachfile.error.io.size", filename));
            }
        }

        final Long largestAttachmentSize = new Long(getMaxAttachmentSize());
        if (size > largestAttachmentSize)
        {
            return new ValidationResult(ValidationError.ATTACHMENT_TO_LARGE, getText("avatarpicker.upload.size.toobig",
                    filename, FileSize.format(largestAttachmentSize)));
        }

        try
        {
            inputStream = request.getInputStream();
        }
        catch (IOException e)
        {
            return new ValidationResult(ValidationError.ATTACHMENT_IO_UNKNOWN, getText("attachfile.error.io.error", filename));
        }

        contentType = request.getContentType();

        if (StringUtils.isBlank(contentType))
        {
            contentType = DEFAULT_CONTENT_TYPE;
        }

        return new ValidationResult(inputStream, size, contentType);
    }

    String getMaxAttachmentSize()
    {
        return Configuration.getString(APKeys.JIRA_ATTACHMENT_SIZE);
    }

    private String getText(String text, Object... args)
    {
        return authenticationContext.getI18nHelper().getText(text, args);
    }


    public enum ValidationError
    {
        ATTACHMENT_TO_LARGE,
        ATTACHMENT_IO_SIZE,
        ATTACHMENT_IO_UNKNOWN,
        FILENAME_BLANK,
        XSRF_TOKEN_INVALID
    }


    public static class ValidationResult
    {

        private final long size;
        private final InputStream inputStream;
        private final String contentType;
        private final ValidationError errorType;
        private final String errorMessage;

        public ValidationResult(final InputStream inputStream, final long size, final String contentType)
        {
            this.inputStream = inputStream;
            this.size = size;
            this.contentType = contentType;
            this.errorType = null;
            this.errorMessage = null;
        }

        public ValidationResult(final ValidationError errorType, final String errorMessage)
        {
            this.inputStream = null;
            this.size = -1;
            this.contentType = null;
            this.errorType = errorType;
            this.errorMessage = errorMessage;
        }

        public long getSize()
        {
            return size;
        }

        public InputStream getInputStream()
        {
            return inputStream;
        }

        public String getContentType()
        {
            return contentType;
        }


        public ValidationError getErrorType()
        {
            return errorType;
        }

        public boolean isValid()
        {
            return errorType == null;
        }

        public String getErrorMessage()
        {
            return errorMessage;
        }
    }


}
