package com.atlassian.jira.lookandfeel.upload;

import com.atlassian.jira.config.properties.LookAndFeelBean;
import com.atlassian.jira.lookandfeel.image.ImageDescriptor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Uploads logos and favicons
 *
 * @since v4.4
 */
public interface UploadService
{
    List<String> uploadLogo(ImageDescriptor imageDescriptor, LookAndFeelBean lookAndFeelBean);

    List<String> uploadFavicon(LookAndFeelBean lookAndFeelBean, ImageDescriptor imageDescriptor);

    File getLogoDirectory();

    Map<String, String> uploadDefaultFavicon(BufferedImage image);

    Map<String, String> uploadDefaultLogo(BufferedImage image);
}
