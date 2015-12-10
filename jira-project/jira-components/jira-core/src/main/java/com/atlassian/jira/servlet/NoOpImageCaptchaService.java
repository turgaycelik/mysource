package com.atlassian.jira.servlet;

import com.octo.captcha.service.CaptchaServiceException;
import com.octo.captcha.service.image.ImageCaptchaService;

import java.awt.image.BufferedImage;
import java.util.Locale;

/**
 * no op Captcha service for the express purpose of reducing memory in OnDemand
 *
 * @since v6.1
 */
public class NoOpImageCaptchaService implements ImageCaptchaService
{
    private static final BufferedImage BLANK_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    private static ImageCaptchaService INSTANCE;

    public static ImageCaptchaService instance() {
        if (INSTANCE == null)
        {
            INSTANCE = new NoOpImageCaptchaService();
        }
        return INSTANCE;
    }

    private NoOpImageCaptchaService()
    {
    }

    @Override
    public BufferedImage getImageChallengeForID(final String ID) throws CaptchaServiceException
    {
        return BLANK_IMAGE;
    }

    @Override
    public BufferedImage getImageChallengeForID(final String ID, final Locale locale) throws CaptchaServiceException
    {
        return BLANK_IMAGE;
    }

    @Override
    public Object getChallengeForID(final String ID) throws CaptchaServiceException
    {
        return BLANK_IMAGE;
    }

    @Override
    public Object getChallengeForID(final String ID, final Locale locale) throws CaptchaServiceException
    {
        return BLANK_IMAGE;
    }

    @Override
    public String getQuestionForID(final String ID) throws CaptchaServiceException
    {
        return "";
    }

    @Override
    public String getQuestionForID(final String ID, final Locale locale) throws CaptchaServiceException
    {
        return "";
    }

    @Override
    public Boolean validateResponseForID(final String ID, final Object response) throws CaptchaServiceException
    {
        return true;
    }
}
