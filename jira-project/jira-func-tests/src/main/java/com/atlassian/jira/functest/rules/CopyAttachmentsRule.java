package com.atlassian.jira.functest.rules;

import java.io.File;
import java.io.IOException;

import com.atlassian.jira.functest.framework.FuncTestCase;

import org.apache.commons.io.FileUtils;

/**
 * Cleans up the attachments directory and copies set of attachments into the directory. Cleans up the attachments dir after the test.
 * @since v6.1
 */
public class CopyAttachmentsRule extends RemoveAttachmentsRule
{
    public CopyAttachmentsRule(final FuncTestCase testCase)
    {
        super(testCase);
    }

    public void copyAttachmentsFrom(String subDirectory)
    {
        final File testAttachmentsPath = new File(environmentData.getXMLDataLocation(), subDirectory);

        try
        {
            FileUtils.copyDirectory(testAttachmentsPath, getAttachmentPath());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
