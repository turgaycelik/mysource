package com.atlassian.jira.mail.util;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.mail.TemplateUser;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.web.ServletContextProvider;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.servlet.ServletContext;

public class MailAttachmentsManagerImpl implements MailAttachmentsManager
{
    private static final Logger log = Logger.getLogger(MailAttachmentsManagerImpl.class);

    private final AvatarService avatarService;
    private final UserManager userManager;
    private final AvatarManager avatarManager;
    private final ApplicationProperties applicationProperties;

    @ClusterSafe ("This is a local object used in the creation of a single email")
    private final Map<MailAttachment, String> mailAttachments = Collections.synchronizedMap(Maps.<MailAttachment, String>newHashMap());

    public MailAttachmentsManagerImpl(AvatarService avatarService, UserManager userManager, AvatarManager avatarManager, ApplicationProperties applicationProperties)
    {
        this.avatarService = avatarService;
        this.userManager = userManager;
        this.avatarManager = avatarManager;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public String getAvatarUrl(String username)
    {
        final ApplicationUser user = userManager.getUserByName(username);
        return getAvatarUrl(user);
    }

    @Override
    public String getAvatarUrl(TemplateUser templateUser)
    {
        return getAvatarUrl(templateUser.getName());
    }

    @Override
    public String getAvatarUrl(ApplicationUser user)
    {
        if (user != null && avatarService.isUsingExternalAvatar(getLoggedInUser(), user))
        {
            // If Gravatar is enabled we do not need to add Avatar as attachment
            return avatarService.getAvatarUrlNoPermCheck(user, null).toString();
        }
        return addAttachmentAndReturnCid(new AvatarAttachment(user));
    }

    private ApplicationUser getLoggedInUser()
    {
        return ComponentAccessor.getJiraAuthenticationContext().getUser();
    }

    private String addAttachmentAndReturnCid(MailAttachment mailAttachment)
    {
        if(mailAttachments.containsKey(mailAttachment))
        {
            return buildCidUrl(mailAttachments.get(mailAttachment));
        }
        else
        {
            String cid = generateCid(mailAttachment);
            if(mailAttachments.containsValue(cid))
            {
                //shouldn't happen, but if append additional UUID to cid to ensure uniqueness
                cid = cid + UUID.randomUUID().toString();
            }
            mailAttachments.put(mailAttachment, cid);
            return buildCidUrl(cid);
        }
    }

    private String buildCidUrl(String cid)
    {
        return "cid:" + cid;
    }

    private String generateCid(MailAttachment attachment)
    {
        return CID_PREFIX + attachment.getUniqueName();
    }

    @Override
    public String getImageUrl(String path)
    {
        final ServletContext servletContext = ServletContextProvider.getServletContext();
        try
        {
            //If path cannot be understood by servlet context it's probably absolutePath to external resource
            // and it cannot be attached to mail
            if (servletContext.getResource(path) == null)
            {
                return getAbsoluteUrl(path);
            }
        }
        catch (MalformedURLException e)
        {
            return getAbsoluteUrl(path);
        }

        return addAttachmentAndReturnCid(new ImageAttachment(path));
    }

    public String getExternalImageUrl(String path)
    {
        try
        {
            final URI uri = new URI(path);
            if (uri.isAbsolute()) {
                return path;
            }

            return addAttachmentAndReturnCid(new UrlImageAttachment(getAbsoluteUrl(path)));
        }
        catch (URISyntaxException e)
        {
            log.trace("Cannot understand URI: " + path, e);
            return path;
        }
    }

    protected String getAbsoluteUrl(String path)
    {
        final String baseUrl = applicationProperties.getString(APKeys.JIRA_BASEURL);
        try
        {
            final URI uri = new URI(path);
            return uri.isAbsolute() ? path : StringUtils.stripEnd(baseUrl, "/") + "/" + StringUtils.stripStart(path, "/");
        }
        catch (URISyntaxException e)
        {
            log.trace("Cannot understand URI: " + path, e);
            return path;
        }
    }

    @Override
    public int getAttachmentsCount()
    {
        return mailAttachments.size();
    }

    @Override
    public Iterable<BodyPart> buildAttachmentsBodyParts()
    {
        final Iterable<BodyPart> bodyParts = Iterables.transform(mailAttachments.entrySet(), new Function<Map.Entry<MailAttachment, String>, BodyPart>()
        {
            @Override
            public BodyPart apply(Map.Entry<MailAttachment, String> input)
            {
                final BodyPart bodyPart = input.getKey().buildBodyPart();
                if (bodyPart != null)
                {
                    try
                    {
                        final String cid = input.getValue();
                        bodyPart.setHeader("Content-ID", String.format("<%s>", cid));

                    }
                    catch (MessagingException e)
                    {
                        log.warn("Cannot add 'Content-ID' header to mail part", e);
                        return null;
                    }
                }
                return bodyPart;
            }
        });
        return Iterables.filter(bodyParts, Predicates.notNull());
    }


    private static interface MailAttachment
    {
        BodyPart buildBodyPart();

        String getUniqueName();
    }

    /**
     * Reads static image using ServletContext and build BodyPart from it.
     */
    private static class ImageAttachment implements MailAttachment
    {
        protected static class ResourceData
        {
            private final String mimeType;
            private final InputStream inputStream;

            protected ResourceData(final String mimeType, final InputStream inputStream) {
                this.mimeType = mimeType;
                this.inputStream = inputStream;
            }
        }

        protected final String imagePath;

        private ImageAttachment(String imagePath)
        {
            Preconditions.checkNotNull(imagePath);
            this.imagePath = imagePath;
        }

        @Override
        public BodyPart buildBodyPart()
        {
            try
            {
                final ResourceData resourceData = getResourceData();
                final MimeBodyPart bodyPart = new MimeBodyPart();
                bodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(resourceData.inputStream, resourceData.mimeType)));
                bodyPart.setDisposition(MimeBodyPart.INLINE);
                return bodyPart;
            }
            catch (MessagingException e)
            {
                log.warn(String.format("Cannot add image as Mail attachment: '%s'", imagePath), e);
                return null;
            }
            catch (IOException e)
            {
                log.warn(String.format("Cannot load resource for: '%s'", imagePath), e);
                return null;
            }
        }

        protected ResourceData getResourceData() throws MessagingException, IOException
        {
            final ServletContext servletContext = ServletContextProvider.getServletContext();
            final InputStream resourceStream = servletContext.getResourceAsStream(imagePath);
            final String mimeType = servletContext.getMimeType(imagePath);
            return new ResourceData(mimeType, resourceStream);
        }

        @Override
        public String getUniqueName()
        {
            final String name = FilenameUtils.getBaseName(imagePath);
            final UUID uuid = UUID.randomUUID();
            return String.format("static-%s-%s", name, uuid.toString());
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            final ImageAttachment that = (ImageAttachment) o;

            return imagePath.equals(that.imagePath);

        }

        @Override
        public int hashCode()
        {
            return imagePath.hashCode();
        }
    }

    /**
     * Reads an image from an URL and build a mail BodyPart out of it.
     * Should probably only be used for local resources, that aren't accessible via ServletContext
     */
    private static class UrlImageAttachment extends ImageAttachment
    {
        private UrlImageAttachment(final String imagePath)
        {
            super(imagePath);
        }

        @Override
        protected ResourceData getResourceData() throws MessagingException, IOException
        {
            try
            {
                URL url = new URL(imagePath);
                final URLConnection connection = url.openConnection();
                return new ResourceData(connection.getContentType(), connection.getInputStream());
            }
            catch (MalformedURLException e)
            {
                throw new MessagingException("Malformed atttachment URL", e);
            }
        }
    }

    /**
     * Reads user avatar using AvatarManager and build BodyPart form it
     */
    private class AvatarAttachment implements MailAttachment
    {
        public static final int ANONYMOUS_USER_HASH_CODE = 0;
        private final ApplicationUser user;

        private AvatarAttachment(ApplicationUser user)
        {
            this.user = user;
        }

        @Override
        public BodyPart buildBodyPart()
        {
            Avatar avatar = avatarService.getAvatarTagged(user, user);
            try
            {
                ToBodyPartConsumer dataAccessor = new ToBodyPartConsumer(avatar.getContentType());

                // The size we provide here must match the dimensions of the embedded avatar image in
                // jira-components/jira-core/src/main/resources/templates/email/html/includes/header.vm , otherwise
                // Outlook will rewrite the image and destroy our JIRA metadata needed for JRA-25705
                avatarManager.readAvatarData(avatar, AvatarManager.ImageSize.MEDIUM, dataAccessor);

                final BodyPart bodyPart = dataAccessor.getBodyPart();
                bodyPart.setDisposition(MimeBodyPart.INLINE);
                return bodyPart;
            }
            catch (FileNotFoundException e)
            {
                log.warn(String.format("Cannot add avatar as Mail attachment for user '%s' - file not found", user == null ? "anonymous" : user.getName()));
                return null;
            }
            catch (IOException e)
            {
                log.warn(String.format("Cannot add avatar as Mail attachment for user '%s'", user == null ? "anonymous" : user.getName()), e);
                return null;
            }
            catch (MessagingException e)
            {
                log.warn(String.format("Problem with disposition while adding avatar as Mail attachment for user '%s'", user == null ? "anonymous" : user.getName()), e);
                return null;
            }
        }

        @Override
        public String getUniqueName()
        {
            final UUID uuid = UUID.randomUUID();
            return String.format("avatar-%s-%s", user == null ? "" : user.getUsername(), uuid.toString());
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            final AvatarAttachment that = (AvatarAttachment) o;
            if (this.user == null) { return that.user == null; }

            return user.equals(that.user);

        }

        @Override
        public int hashCode()
        {
            return user == null ? ANONYMOUS_USER_HASH_CODE : user.hashCode();
        }
    }

    private static class ToBodyPartConsumer implements Consumer<InputStream>
    {
        private BodyPart bodyPart;
        private final String contentType;

        private ToBodyPartConsumer(String contentType) {
            this.contentType = contentType;
        }

        @Override
        public void consume(InputStream element)
        {
            try
            {
                bodyPart = new MimeBodyPart();
                bodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(element, contentType)));
            }
            catch (MessagingException e)
            {
                log.warn("Cannot read avatar", e);
                bodyPart = null;
            }
            catch (IOException e)
            {
                log.warn("Cannot read avatar", e);
                bodyPart = null;
            }
        }

        private BodyPart getBodyPart()
        {
            return bodyPart;
        }
    }
}
