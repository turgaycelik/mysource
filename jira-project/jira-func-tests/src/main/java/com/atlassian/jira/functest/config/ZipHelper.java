package com.atlassian.jira.functest.config;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Simple helper class to extract a zip file directory contents and all.
 *
 * @since v4.1
 */
public class ZipHelper
{
    public static void extractTo(final File zipFile, final File outDir) throws IOException
    {
        extractTo(new FileInputStream(zipFile), outDir);
    }

    public static void extractTo(InputStream is, final File outDir) throws IOException
    {
        ZipInputStream zis = new ZipInputStream(is);
        try
        {
            ZipEntry nextEntry;
            while ((nextEntry = zis.getNextEntry()) != null)
            {
                if (!nextEntry.isDirectory())
                {
                    File newFile = new File(outDir, nextEntry.getName());
                    copyStreamToFile(zis, newFile);
                }
            }
        }
        finally
        {
            IOUtils.closeQuietly(zis);
        }
    }

    private static void copyStreamToFile(final InputStream stream, final File output) throws IOException
    {
        FileOutputStream fos = FileUtils.openOutputStream(output);
        try
        {
            IOUtils.copy(stream, fos);
        }
        finally
        {
            IOUtils.closeQuietly(fos);
        }
    }
}
