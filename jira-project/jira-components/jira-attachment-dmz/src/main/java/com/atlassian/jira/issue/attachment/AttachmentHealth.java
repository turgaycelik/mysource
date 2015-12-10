package com.atlassian.jira.issue.attachment;

import com.atlassian.fugue.Option;
import com.atlassian.jira.util.ErrorCollection;

/**
 * Classes that implement this can report a health status.
 *
 * @since v6.3
 */
public interface AttachmentHealth
{
    /**
     * Health status for this component. Specifically the errors that cause the attachment subsystem to fail.
     *
     * @return An option of an error collection that contains error messages if there are any issues. The
     * option will be none if there are no errors.
     */
    Option<ErrorCollection> errors();
}
