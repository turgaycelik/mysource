package com.atlassian.jira.lookandfeel.filter;

import com.atlassian.plugin.servlet.ResourceDownloadUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


/**
 * Streams image files to the ServletOutputStream - uses buffer size of 1kb
 *
 * @since v4.4
 */
public class ImageDownloader
{
    private final int BUFSIZE = 1024;

    /**
     *  Sends an image to the ServletResponse output stream.
     *
     * @param req The request
     *  @param resp The response
     * @param context The servletContext
     * @param filename The name of the image you want to download.
     * @param shouldClose  Whether the output stream should be closed or not
     */
    public void doDownload(final HttpServletRequest req, final HttpServletResponse resp, final ServletContext context,
            final String filename, final boolean shouldClose) throws IOException
    {
        ServletOutputStream op = null;
        DataInputStream in = null;

        try
        {
            final File f = new File(filename);
            final String mimeType;
            if (filename.endsWith(".ico"))
            {
                mimeType = "image/x-icon";
            }
            else
            {
                mimeType = context.getMimeType(filename);
            }
            if (mimeType == null)
            {
                context.log("Could not get MIME type of " + filename);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            resp.setContentType(mimeType);
            resp.setContentLength((int) f.length());
            // addCacheHeaders if needed
            if (req.getServletPath().startsWith("/s"))
            {
                ResourceDownloadUtils.addPublicCachingHeaders(req, resp);
            }
            op = resp.getOutputStream();
            in = new DataInputStream(new FileInputStream(f));
            byte[] bbuf = new byte[BUFSIZE];
            int length = 0;
            while ((in != null) && ((length = in.read(bbuf)) != -1))
            {
                op.write(bbuf, 0, length);
            }
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
            if (op != null)
            {
                op.flush();
                // only close if the caller requests it
                // in our case coming from a filter the stream should not be committed
                if (shouldClose)
                {
                    op.close();
                }
            }
        }
    }

}
