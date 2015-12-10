package com.atlassian.jira.mail.util;

import com.atlassian.jira.mail.TemplateUser;
import com.atlassian.jira.user.ApplicationUser;

import javax.mail.BodyPart;

public interface MailAttachmentsManager
{
    /**
     * Content-ID for generated attachments.
     */
    public static final String CID_PREFIX = "jira-generated-image-";
    /**
     * Returns a link to access user avatar in email message.
     * If avatar can be attached to email, the cid link is returned,
     * otherwise avatar URL is returned and no image is attached.
     * (E.g. If Gravatar is enabled)
     *
     * @param username
     * @return cid link or Avatar URL
     */
    public String getAvatarUrl(String username);

    /**
     * Returns a link to access user avatar in email message.
     * If avatar can be attached to email, the cid link is returned,
     * otherwise avatar URL is returned and no image is attached.
     * (E.g. If Gravatar is enabled)
     *
     * @param templateUser
     * @return cid link or Avatar URL
     */
    public String getAvatarUrl(TemplateUser templateUser);

    /**
     * Returns a link to access user avatar in email message.
     * If avatar can be attached to email, the cid link is returned,
     * otherwise avatar URL is returned and no image is attached.
     * (E.g. If Gravatar is enabled)
     *
     * @param user
     * @return cid link or Avatar URL
     */
    public String getAvatarUrl(ApplicationUser user);

    /**
     * Tries to add image specified by path to email attachments.
     * Returns image cid link if succeeds or unchanged path if specified
     * path cannot be added as attachment
     *
     * @param path
     * @return
     */
    public String getImageUrl(String path);

    /**
     * If the path is a relative url tries to add the image specified as an attachment.
     * Returns a cid link if successful, or unchanged path if not
     * @since JIRA 6.3
     */
    public String getExternalImageUrl(String path);

    /**
     * Returns the number of attachments added to this manager
     *
     * @return number of attachments
     */
    public int getAttachmentsCount();

    /**
     * Builds bodyPart for each image (including avatars) added to this manager
     *
     * @return Added attachments as list of BodyParts
     */
    public Iterable<BodyPart> buildAttachmentsBodyParts();
}
