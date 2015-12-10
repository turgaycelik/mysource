package com.atlassian.jira.util;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Simple utility functions for dealing with zip files.
 */
public class ZipUtils
{
    /**
     * Retrieves an input stream reading from a named entry in a zip file.
     * @param zipFile the path to the zip file.
     * @param entryName the name of the entry to which an input stream should be returned.
     * @return an input stream reading from the named entry in the specified file.
     * @throws IOException if the zip file is unreadable, or if it does not contained the named entry.
     */
    public static InputStream streamForZipFileEntry(File zipFile, String entryName) throws IOException
    {
        final ZipFile file = new ZipFile(zipFile.getAbsolutePath());
        InputStream underlyingStream = null;
        try
        {
            ZipArchiveEntry entry = file.getEntry(entryName);
            if (entry == null)
            {
                return null;
            }
            underlyingStream = file.getInputStream(entry);
        }
        finally
        {
            if (underlyingStream == null)
                ZipFile.closeQuietly(file);
        }
        return new FilterInputStream(underlyingStream)
        {
            @Override
            public void close() throws IOException
            {
                super.close();
                file.close();
            }
        };
    }

    /**
     * Zip a directory into a file.
     * @param inputDir Directory to be compressed.
     * @param output Target file for the zip contents. If the file exists it will be overwritten.
     * @throws FileNotFoundException if source does not exist
     * @throws IOException For other IO errors
     * @since v6.2
     */
    public static void zip(File inputDir, File output) throws IOException
    {
        if (!inputDir.exists())
        {
            throw new FileNotFoundException(inputDir.getPath());
        }
        if (!output.exists())
        {
            output.createNewFile();
        }
        else
        {
            if (output.isDirectory())
            {
                throw new IOException("'" + output.getPath() + "' exists and is a directory. Cannot overwrite.");
            }
            if (!output.canWrite())
            {
                throw new IOException("'" + output.getPath() + "' exists and is not writable.");
            }
        }

        // Create ZipOutputStream
        ZipArchiveOutputStream out = new ZipArchiveOutputStream(new BufferedOutputStream(new FileOutputStream(output)));
        out.setLevel(ZipArchiveOutputStream.DEFAULT_COMPRESSION);
        try
        {
            compressDirectory(inputDir, out, "");
        }
        finally
        {
            out.close();
        }
    }

    private static void compressDirectory(final File inputDir, final ZipArchiveOutputStream out, final String parentPath) throws IOException
    {
        // Recursively process all the files in the directory
        File[] files = inputDir.listFiles();
        for (File file : files)
        {
            String name = parentPath + file.getName();
            ZipArchiveEntry archiveEntry = new ZipArchiveEntry(file, name);
            out.putArchiveEntry(archiveEntry);
            if (file.isDirectory())
            {
                compressDirectory(file, out, archiveEntry.getName());
            }
            else
            {
                FileInputStream input = new FileInputStream(file);
                try
                {
                    IOUtils.copyLarge(input, out);
                }
                finally
                {
                    IOUtils.closeQuietly(input);
                    out.closeArchiveEntry();
                }
            }
        }
    }

    /**
     * Expand a Zip file into a directory.
     * @param input Zip file to be expanded
     * @param outputDir Target directory. Files will be overwritten
     * @throws FileNotFoundException if source does not exist
     * @throws IOException For other IO errors
     * @since v6.2
     */
    public static void unzip(File input, File outputDir) throws IOException
    {
        if (!input.exists())
        {
            throw new FileNotFoundException(input.getPath());
        }
        if (!outputDir.exists())
        {
            outputDir.mkdirs();
        }
        else
        {
            if (!outputDir.isDirectory())
            {
                throw new IOException("'" + outputDir.getPath() + "' exists but is not a directory. Cannot overwrite.");
            }
            if (!outputDir.canWrite())
            {
                throw new IOException("'" + outputDir.getPath() + "' exists and is not writable.");
            }
        }

        // Create ZipOutputStream
        ZipArchiveInputStream in = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(input)));
        try
        {
            ZipArchiveEntry entry;
            do
            {
                entry = in.getNextZipEntry();
                if (entry != null && !entry.isDirectory())
                {
                    String fileName = entry.getName();
                    File file = new File(outputDir, fileName);
                    file.getParentFile().mkdirs();
                    FileOutputStream out = new FileOutputStream(file);
                    try
                    {
                        IOUtils.copyLarge(in, out);
                    }
                    finally
                    {
                        IOUtils.closeQuietly(out);
                    }
                }

            } while (entry != null);
        }
        finally
        {
            IOUtils.closeQuietly(in);
        }
    }


}
