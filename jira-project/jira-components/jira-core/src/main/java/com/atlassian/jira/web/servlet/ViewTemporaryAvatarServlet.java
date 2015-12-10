package com.atlassian.jira.web.servlet;

import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.ImageScaler;
import com.atlassian.jira.avatar.TemporaryAvatar;
import com.atlassian.jira.avatar.TemporaryAvatars;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.IOUtil;
import com.atlassian.jira.web.SessionKeys;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.RenderedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Streams out an avatar image that has just been uploaded so that cropping/scaling operations can be performed.
 *
 * @since v4.0
 */
public class ViewTemporaryAvatarServlet extends ViewProjectAvatarServlet
{
    private static final int BUFFER_SIZE = 8192;
    private static final String PARAM_SIZE = "size";
    private static final String PARAM_CROPPED = "cropped";

    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
    {
        final TemporaryAvatars temporaryAvatars = ComponentAccessor.getComponent(TemporaryAvatars.class);
        final TemporaryAvatar temporaryAvatar = temporaryAvatars.getCurrentTemporaryAvatar();

        if (temporaryAvatar == null || !temporaryAvatar.getFile().exists())
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setHeader("Expires", "Fri, 01 Jan 1990 00:00:00 GMT");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-control", "no-cache, must-revalidate");
        response.setContentType(temporaryAvatar.getTemporaryContentType());
        final OutputStream out = response.getOutputStream();
        boolean bytesWritten = false;
        try
        {
            // If the cropped image is requested and we have cropping instructions, return the cropped image
            if ("true".equalsIgnoreCase(request.getParameter(PARAM_CROPPED)) && temporaryAvatar.getSelection() != null)
            {
                final ImageScaler scaler = new ImageScaler();
                // large avatar is the default. Small is only used if small size is requested.
                AvatarManager.ImageSize size = AvatarManager.ImageSize.fromString(request.getParameter(PARAM_SIZE));
                RenderedImage image = scaler.getSelectedImageData(ImageIO.read(new FileInputStream(temporaryAvatar.getFile())), temporaryAvatar.getSelection(), size.getPixels());
                ImageIO.write(image, AvatarManager.AVATAR_IMAGE_FORMAT_FULL.getName(), out);
            }
            else
            {
                // Otherwise, return the whole image
                IOUtil.copy(new FileInputStream(temporaryAvatar.getFile()), out, BUFFER_SIZE);
            }
            bytesWritten = true;
        }
        catch (IOException e)
        {
            handleOutputStreamingException(response, bytesWritten, e);
        }
    }
}
