package com.atlassian.jira.lookandfeel.image;

import com.atlassian.jira.util.I18nHelper;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * This represents an image that is uploaded from a provided URL
 *
 * @since v4.4
 */
public class URLImageDescriptor extends ImageDescriptor
{
    private final I18nHelper i18nHelper;

    public URLImageDescriptor(String serverPath, String filename, I18nHelper i18nHelper) throws IOException
    {
        this.i18nHelper = i18nHelper;
        resolveUrl(serverPath, filename);

    }

    private void resolveUrl(final String serverPath, final String filename) throws IOException
    {
         if (filename != null)
         {
             if(filename.startsWith("http") || filename.startsWith("file:"))
             {
                 handleAbsoluteUrl(filename);
             }
             else
             {
                 handleRelativeUrl(serverPath, filename);
             }
         }
    }

    private void handleAbsoluteUrl(final String filename)  throws IOException
    {
        URL url = new URL(filename);
        URLConnection urlConnection = url.openConnection();
        urlConnection.connect();
        contentType =  urlConnection.getContentType();
        fileName = filename;
        imageData = urlConnection.getInputStream();
    }

    private void handleRelativeUrl(final String serverPath, String filename) throws IOException
    {
        File file = new File(serverPath, filename);
        contentType =  URLConnection.guessContentTypeFromName(filename);
        this.fileName = filename;
        imageData = new FileInputStream(file);
    }

    @Override
    public String getImageDescriptorType()
    {
        return i18nHelper.getText("jira.lookandfeel.urlimagedescriptor.type");
    }
}
