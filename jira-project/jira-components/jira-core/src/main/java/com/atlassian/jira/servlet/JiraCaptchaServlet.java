package com.atlassian.jira.servlet;

import com.atlassian.jira.component.ComponentAccessor;
import com.google.common.annotations.VisibleForTesting;
import com.octo.captcha.CaptchaException;
import com.octo.captcha.service.CaptchaServiceException;
import org.apache.log4j.Logger;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JiraCaptchaServlet extends HttpServlet
{
    private static final Logger log = Logger.getLogger(JiraCaptchaServlet.class);

    /**
     * Returns a captcha image challenge for a given id.
     *
     * <p>The id used to generate the challenge will be the {@link javax.servlet.http.HttpSession http session} id.</p>
     *
     * <p>Later on, the same id will be used to validate the response to the captcha challenge.</p>
     *
     * @param request The http request for this captcha image.
     * @param response The http response where the captch image will be written to.
     *
     * @see com.octo.captcha.service.image.ImageCaptchaService#getImageChallengeForID(String, java.util.Locale).
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            final byte[] captchaChallengeAsJpeg;
            final ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
            try
            {
                final String captchaId = request.getSession().getId();
                BufferedImage imageChallenge = null;
                while(imageChallenge == null)
                {
                    try
                    {
                        imageChallenge = getCaptchaService().getImageCaptchaService().getImageChallengeForID(captchaId, request.getLocale());
                    }
                    catch (CaptchaException e)
                    {
                        log.debug("CaptchaException thrown when image was being generated. This was most likely caused by running on OS X which has font size issues. Ignoring the exception. See http://jcaptcha.octo.com/jira/browse/FWK-58.", e);
                    }
                }

                writeJpegImage(jpegOutputStream, imageChallenge);
            }
            catch (IllegalArgumentException e)
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            catch (CaptchaServiceException e)
            {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            captchaChallengeAsJpeg = jpegOutputStream.toByteArray();

            setResponseHeaders(response);

            final ServletOutputStream responseOutputStream = response.getOutputStream();
            responseOutputStream.write(captchaChallengeAsJpeg);
            responseOutputStream.flush();
            responseOutputStream.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void setResponseHeaders(final HttpServletResponse response)
    {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");
    }

    @VisibleForTesting
    JiraCaptchaService getCaptchaService()
    {
        return ComponentAccessor.getComponent(JiraCaptchaService.class);
    }

    private void writeJpegImage(final ByteArrayOutputStream os, final BufferedImage bufferedImage) throws IOException
    {
        final ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(os);
        final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        writer.setOutput(imageOutputStream);
        writer.write(bufferedImage);
    }
}