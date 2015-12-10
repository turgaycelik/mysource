package com.atlassian.jira.servlet;

import com.octo.captcha.service.image.ImageCaptchaService;

/**
 * This gives out CAPTCHA services
 *
 * @since v4.0.1
 */
public interface JiraCaptchaService
{
    /**
     * @return a NON NULL image based CAPTCHA Service
     */
    public ImageCaptchaService getImageCaptchaService();
}
