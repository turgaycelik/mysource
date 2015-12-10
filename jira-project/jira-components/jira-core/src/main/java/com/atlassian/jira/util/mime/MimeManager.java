package com.atlassian.jira.util.mime;

import com.google.common.collect.ImmutableList;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;

public class MimeManager
{
    /**
     * A list of mime types that we wish to override.
     */
    private static final List<String> GENERIC_MIME_TYPES =
            ImmutableList.of("application/octet-stream", "application/x-zip-compressed");
    private static final Logger log = Logger.getLogger(MimeManager.class);
    private final String DEFAULT_MIME_TYPE = "text/html";

    private FileTypeMap fileTypeMap;

    public MimeManager(InputStream mimeTypesInputStream)
    {
        try
        {
            this.fileTypeMap = new MimetypesFileTypeMap(mimeTypesInputStream);
            try
            {
                if (mimeTypesInputStream != null)
                    mimeTypesInputStream.close();
            }
            catch (IOException e)
            {
                log.warn("Could not close mime types inputStream");
            }
        }
        catch (Exception e)
        {
            log.error("Could not load mimeTypes from inputStream.  Defaulting to default mimeTypes", e);
            this.fileTypeMap = new MimetypesFileTypeMap();
        }
    }

    /**
     * If the existing mime type is listed in {@link #GENERIC_MIME_TYPES}, then return a suggested
     * replacement by looking up the file registered under {@link com.atlassian.jira.ContainerRegistrar#MIME_TYPES_INPUTSTREAM_KEY}.
     * Else return the original mime type.
     *
     * Useful when uploading files, and the browser is lazy / sets the wrong content type.
     *
     * @param existingMimeType The mime type that we currently know for this file
     * @param fileName The name of the file
     * @return A suggested replacement if the existing mime type is contained by {@link #GENERIC_MIME_TYPES};
     * otherwise, the existing mime type is returned.
     *
     * @see com.atlassian.jira.ContainerRegistrar#MIME_TYPES_INPUTSTREAM_KEY
     */
    public String getSanitisedMimeType(String existingMimeType, String fileName)
    {
        // only override the mime-type if it is a generic type
        if (GENERIC_MIME_TYPES.contains(existingMimeType))
        {
            return getSuggestedMimeType(fileName);
        }
        else
        {
            return existingMimeType;
        }
    }

    /**
     * Suggests a mime type for a file name by looking up the file registered under
     * {@link com.atlassian.jira.ContainerRegistrar#MIME_TYPES_INPUTSTREAM_KEY}.
     *
     * @param fileName The name of the file
     * @return A suggested mime type for a file name;
     *
     * @see com.atlassian.jira.ContainerRegistrar#MIME_TYPES_INPUTSTREAM_KEY
     */
    public String getSuggestedMimeType(String fileName)
    {
        if (fileName == null)
        {
            return null;
        }

        // Paths with no extension
        if(!fileName.contains(".")) {
            return DEFAULT_MIME_TYPE;
        }

        // we need to lowercase the filename to handle extensions like ".JpeG". JRA-23304.
        // We pass Locale.US to avoid the Turkish bug.
        // See https://extranet.atlassian.com/display/~matt@atlassian.com/2009/02/05/The+infamous+Turkish+locale+bug
        return fileTypeMap.getContentType(fileName.toLowerCase(Locale.US));
    }
}
