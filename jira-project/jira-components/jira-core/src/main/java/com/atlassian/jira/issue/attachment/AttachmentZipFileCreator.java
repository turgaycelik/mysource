package com.atlassian.jira.issue.attachment;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.AttachmentUtils;
import com.atlassian.jira.util.IOUtil;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class can create temporary zip files containing all the attachments on an issue
 *
 * @since v4.1
 */
public class AttachmentZipFileCreator
{
    private final Issue issue;

    public AttachmentZipFileCreator(final Issue issue)
    {
        this.issue = issue;
    }

    /**
     * This will return a ZIP file that contains all the attachments of an issue.  The file will be created in the
     * temporary directory and end in .zip
     * <p/>
     * you shoudl delete the file when you are done, otherwise, well this is JIRA not Bamboo!
     *
     * @return a zip file containing all the attachments of an issue
     *
     * @throws IOException if stuff goes wrong
     */
    public File toZipFile() throws IOException
    {
        File zipFile = File.createTempFile(issue.getKey() + "-", ".zip");
        Collection<Attachment> attachments = issue.getAttachments();
        UniqueFileNameGenerator uniqueFileNameGenerator = new UniqueFileNameGenerator();
        ZipArchiveOutputStream out = null;
        try
        {
            // Create the ZIP file

            out = new ZipArchiveOutputStream(new FileOutputStream(zipFile));

            // Compress the files
            for (Attachment attachment : attachments)
            {
                File attachmentFile = getAttachmentFile(attachment);

                // Add ZIP entry to output stream.
                final String attachmentFileName = attachment.getFilename();
                final ZipArchiveEntry zipEntry = new ZipArchiveEntry(uniqueFileNameGenerator.getUniqueFileName(attachmentFileName));
                out.putArchiveEntry(zipEntry);

                FileInputStream in = null;
                try
                {
                    in = new FileInputStream(attachmentFile);
                    // Transfer bytes from the attachment to the ZIP file
                    IOUtil.copy(in, out);

                    // Complete the entry
                    out.closeArchiveEntry();
                }
                finally
                {
                    IOUtil.shutdownStream(in);
                }
            }
        }
        finally
        {
            IOUtil.shutdownStream(out);
        }
        return zipFile;
    }

    /**
     * To allow for testing we make this able to be mocked out by the test
     *
     * @param attachment the attachment in play
     *
     * @return the file for a given attachment object
     */
    File getAttachmentFile(final Attachment attachment)
    {
        return AttachmentUtils.getAttachmentFile(attachment);
    }

    /**
     * A Set based thingy that remembers names that have been seen by it before and returns them munged into new unique
     * names.  brad.js will become brad.js.1 and brad.js.2 each time it is presented.
     */
    static class UniqueFileNameGenerator
    {
        private static final Pattern pattern = Pattern.compile("\\.([0-9]+)$");
        private Set<String> fileNamesSet = new HashSet<String>();

        public String getUniqueFileName(final String fileName)
        {
            String safeFileName = fileName;
            while (fileNamesSet.contains(safeFileName))
            {
                safeFileName = mungeFileName(safeFileName);
            }
            fileNamesSet.add(safeFileName);
            return safeFileName;
        }

        private String mungeFileName(final String fileName)
        {
            Matcher m = pattern.matcher(fileName);
            if (m.find())
            {
                // ok lets replace the number at the end
                String numberStr = m.group(m.groupCount());
                int number = Integer.parseInt(numberStr, 10) + 1;

                return m.replaceFirst("." + number);
            }
            else
            {
                return fileName + ".1";
            }
        }
    }

}
