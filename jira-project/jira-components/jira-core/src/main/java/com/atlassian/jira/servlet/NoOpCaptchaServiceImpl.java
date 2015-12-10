package com.atlassian.jira.servlet;

import com.octo.captcha.service.image.ImageCaptchaService;


/**
 *
 * @since v6.1
 */
public class NoOpCaptchaServiceImpl implements JiraCaptchaService {

    @Override
    public ImageCaptchaService getImageCaptchaService() {
        return NoOpImageCaptchaService.instance();
    }
}