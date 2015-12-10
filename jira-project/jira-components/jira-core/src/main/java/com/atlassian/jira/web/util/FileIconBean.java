/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.util;

import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.mime.MimeManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileIconBean
{
    //todo - load this from properties files.
    public static final List<FileIcon> DEFAULT_FILE_ICONS;

    static
    {
        final CollectionBuilder<FileIcon> builder = CollectionBuilder.newBuilder();
        builder.add(new FileIcon(".pdf", "application/pdf", "pdf.gif", "PDF File"));
        builder.add(new FileIcon(".gif", "image/gif", "image.gif", "GIF File"));
        builder.add(new FileIcon(".png", "image/png", "image.gif", "PNG File"));
        builder.add(new FileIcon(".jpeg", "image/jpeg", "image.gif", "JPEG File"));
        builder.add(new FileIcon(".xml", "text/xml", "xml.gif", "XML File"));
        builder.add(new FileIcon(".html", "text/html", "html.gif", "HTML File"));
        builder.add(new FileIcon(".java", "", "java.gif", "Java Source File"));
        builder.add(new FileIcon(".jar", "", "java.gif", "Java Archive File"));
        builder.add(new FileIcon(".txt", "text/plain", "text.gif", "Text File"));
        builder.add(new FileIcon(".zip", "application/zip", "zip.gif", "Zip Archive"));
        builder.add(new FileIcon(".gz", "application/x-gzip-compressed", "zip.gif", "GZip Archive"));

        // Note: not using specialised MIME types for 2007 docs, as it's not important here
        // see mime.types file for actual 2007 MIME types
        // JRA-14106: register ALL Office 2007 XML file types
        final String[] wordExtensions = new String[] { ".doc", ".docx", "docm", "dotx", "dotm" };
        final String[] excelExtensions = new String[] { ".xls", "xlsx", "xlsm", "xltx", "xltm", "xlsb", "xlam" };
        final String[] powerpointExtensions = new String[] { "pptx", "pptm", "potx", "potm", "ppam", "ppsx", "ppsm" };

        for (final String wordExtension : wordExtensions)
        {
            builder.add(new FileIcon(wordExtension, "application/msword", "word.gif", "Microsoft Word"));
        }

        for (final String excelExtension : excelExtensions)
        {
            builder.add(new FileIcon(excelExtension, "application/vnd.ms-excel", "excel.gif", "Microsoft Excel"));
        }

        for (final String powerpointExtension : powerpointExtensions)
        {
            builder.add(new FileIcon(powerpointExtension, "application/vnd.ms-powerpoint", "powerpoint.gif", "Microsoft PowerPoint"));
        }
        DEFAULT_FILE_ICONS = builder.asList();
    }

    private final List<FileIcon> mimeTypes;
    private final MimeManager mimeManager;

    public FileIconBean(final List<FileIcon> mimeTypes, final MimeManager mimeManager)
    {
        this.mimeTypes = Collections.unmodifiableList(new ArrayList<FileIcon>(mimeTypes));
        this.mimeManager = mimeManager;
    }

    /**
     * Return a FileIcon if <em>either</em> the fileName <em>or</em> the mimeType matches.
     */
    public FileIcon getFileIcon(final String fileName, final String mimeType)
    {
        for (final FileIcon fileIcon : mimeTypes)
        {
            if (fileIcon.getMimeType().equals(mimeType))
            {
                return fileIcon;
            }
            else if ((fileName != null) && fileName.trim().toLowerCase().endsWith(fileIcon.getFileExtension()))
            {
                return fileIcon;
            }
            else if (fileIcon.getMimeType().equals(mimeManager.getSuggestedMimeType(fileName)))
            {
                return fileIcon;
            }
        }
        return null;
    }

    public static final class FileIcon
    {
        private final String fileExtension;
        private final String mimeType;
        private final String icon;
        private final String altText;

        public FileIcon(final String fileExtension, final String mimeType, final String icon, final String altText)
        {
            this.fileExtension = fileExtension;
            this.mimeType = mimeType;
            this.icon = icon;
            this.altText = altText;
        }

        public String getFileExtension()
        {
            return fileExtension;
        }

        public String getMimeType()
        {
            return mimeType;
        }

        public String getIcon()
        {
            return icon;
        }

        public String getAltText()
        {
            return altText;
        }
    }
}
