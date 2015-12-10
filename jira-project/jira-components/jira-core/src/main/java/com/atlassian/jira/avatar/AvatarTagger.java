package com.atlassian.jira.avatar;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Saves an image as a png with metadata signifying this image is a JIRA Avatar
 * (used by the email handler to decide whether or not to attach an image)
 *
 * @since v6.1
 */
public interface AvatarTagger
{
    final String JIRA_SYSTEM_IMAGE_TYPE = "jira-system-image-type";
    final String AVATAR_SYSTEM_IMAGE_TYPE = "avatar";

    void saveTaggedAvatar(RenderedImage in, String name, File file) throws IOException;

    String tagAvatar(long id, String filename) throws IOException;

    void saveTaggedAvatar(RenderedImage image, String targetFormat, OutputStream target) throws IOException;
}
