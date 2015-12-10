package com.atlassian.jira.issue.attachment;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.jira.issue.IssueKey;

import com.google.common.base.Preconditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains utility methods for dealing with attachments on the file system.
 *
 * @since v6.3
 */
public final class FileAttachments
{
    private static final Logger log = LoggerFactory.getLogger(FileAttachments.class);

    /**
     * Returns the directory containing attachments for an issue.
     * @param rootDir The root directory of all attachments.
     * @param originalProjectKey The project key that was used to store the attachments (i.e. the original project key)
     * @param issueKey The issue key
     * @return The directory containing attachments for an issue.
     */
    public static File getAttachmentDirectoryForIssue(final File rootDir, final String originalProjectKey, final String issueKey)
    {
        final File projectDir = new File(rootDir, originalProjectKey);
        return new File(projectDir, computeIssueKeyForOriginalProjectKey(originalProjectKey, issueKey));
    }

    /**
     * Helper method to mung an issue key to the given project key.
     * @param originalProjectKey The project key that the issue should be under.
     * @param issueKey The issue key to mung.
     * @return Munged issue key with the specified original project key as the issue prefix.
     */
    public static String computeIssueKeyForOriginalProjectKey(final String originalProjectKey, final String issueKey)
    {
        final long issueNumber = IssueKey.from(issueKey).getIssueNumber();
        return IssueKey.format(originalProjectKey, issueNumber);
    }

    /**
     * Returns a file that refers to an attachment specified by the given attachment key and the attachment root directory.
     * @param attachment The attachment key.
     * @param rootDir The root directory of all attachments. NOT THE DIRECTORY FOR AN ISSUE'S ATTACHMENT.
     * @return a file that refers to an attachment specified by the given attachment key and the attachment root directory.
     */
    public static File getAttachmentFileHolder(final AttachmentKey attachment, final File rootDir)
    {
        final File issueDir = getAttachmentDirectoryForIssue(rootDir, attachment.getProjectKey(), attachment.getIssueKey());
        return getAttachmentFileHolder(
                new AttachmentAdapterImpl(attachment.getAttachmentId(), attachment.getAttachmentFilename()),
                issueDir);
    }

    /**
     * Returns a file that refers to the default location of an attachment specified by the given attachment key
     * and the attachment root directory.
     * @param attachment The attachment key.
     * @param rootDir The root directory of all attachments. NOT THE DIRECTORY FOR AN ISSUE'S ATTACHMENT.
     * @return a file that refers to the default location of an attachment specified by the given attachment key
     * and the attachment root directory.
     */
    public static File getDefaultAttachmentFileHolder(final AttachmentKey attachment, final File rootDir)
    {
        final File issueDir = getAttachmentDirectoryForIssue(rootDir, attachment.getProjectKey(), attachment.getIssueKey());
        return getDefaultAttachmentFile(
                new AttachmentAdapterImpl(attachment.getAttachmentId(), attachment.getAttachmentFilename()),
                issueDir);
    }

    /**
     * Returns a file that refers to an attachment specified by the given attachment key and the attachment root directory.
     * @param attachment The attachment adapter
     * @param attachmentDir The directory of issue's attachments. NOT THE ROOT DIRECTORY FOR ALL ATTACHMENTS.
     * @return a file that refers to an attachment specified by the given attachment key and the attachment root directory.
     * @deprecated we should be using {@link #getAttachmentFileHolder(AttachmentKey, java.io.File)} once AttachmentStore is
     * removed.
     */
    @Deprecated
    public static File getAttachmentFileHolder(final AttachmentStore.AttachmentAdapter attachment,
            final File attachmentDir)
    {
        //First try a direct lookup which is fast.
        final File defaultFile = getDefaultAttachmentFile(attachment, attachmentDir);
        if (defaultFile.exists())
        {
            return defaultFile;
        }
        final File legacyFile = getLegacyAttachmentFile(attachment, attachmentDir);
        if (legacyFile.exists())
        {
            return legacyFile;
        }
        else
        {
            //Now lets try a slower lookup by ID if this fails.
            final File legacyAttachmentById = findLegacyAttachmentById(attachment, attachmentDir);
            // If *that* didn't work then we fall back to just returning whatever the default should be.
            // The most common case for this is when a new attachment gets uploaded, though other error cases could
            // also result in it (like if someone manually deleted the file from underneath JIRA).
            if (legacyAttachmentById == null)
            {
                return defaultFile;
            }
            else
            {
                return legacyAttachmentById;
            }
        }
    }

    // For a brief period of time we thought that doing this was sufficient to avoid Encoding issues. However, see
    // http://jira.atlassian.com/browse/JRA-23311 for an explanation of how this can fail. However, we still need
    // to do this to find any attachments that were stored under the old scheme.
    private static File findLegacyAttachmentById(final AttachmentStore.AttachmentAdapter attachment, final File attachmentDir)
    {
        //Find all the files that start with "attachment.id_"
        final Pattern allFilesPattern = Pattern.compile("^" + attachment.getId() + "_.+");
        final File[] list = attachmentDir.listFiles(new FilenameFilter()
        {
            public boolean accept(final File dir, final String name)
            {
                Matcher m = allFilesPattern.matcher(name);
                return m.matches();
            }
        });

        if (list == null || list.length == 0)
        {
            return null;
        }
        else if (list.length > 1)
        {
            //More than 1 file found, the list could contain the thumbnail version of the attachment, search the
            //list excluding anything starting with ID_thumb_
            final Pattern thumbnailExcludingPattern = Pattern.compile("^" + attachment.getId() + "_(?!thumb_).+");
            File firstFile = null;
            int matchCount = 0;
            for (File file : list)
            {
                if (thumbnailExcludingPattern.matcher(file.getName()).matches())
                {
                    matchCount++;
                    if (firstFile == null)
                    {
                        firstFile = file;
                    }
                }
            }

            //If only one match is found then lets use it. Otherwise lets return the first match.
            if (matchCount == 1)
            {
                return firstFile;
            }

            log.warn("More than one file found for attachment id " + attachment.getId() + " in " +
                    attachmentDir + ". The first entry will be returned.");
        }
        return list[0];
    }

    private static File getDefaultAttachmentFile(final AttachmentStore.AttachmentAdapter attachment, final File attachmentDirectory)
    {
        return new File(attachmentDirectory, attachment.getId().toString());
    }

    private static File getLegacyAttachmentFile(final AttachmentStore.AttachmentAdapter attachment, final File attachmentDir)
    {
        return new File(attachmentDir, attachment.getId() + "_" + attachment.getFilename());
    }

    public static File validateFileForAttachment(final Attachment metaData, final File file)
    {
        Preconditions.checkArgument(file.exists() && file.isFile() && file.canRead(), "Source file is unavailable");
        Preconditions.checkArgument(file.length() == metaData.getFilesize(),
                "Source file has different length to what is store in Attachment metadata. Expected %s, but is %s.", metaData.getFilesize(), file.length());
        return file;
    }

    private FileAttachments() {}
}
