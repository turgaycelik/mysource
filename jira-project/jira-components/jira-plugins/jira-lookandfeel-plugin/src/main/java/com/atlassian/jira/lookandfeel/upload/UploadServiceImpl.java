package com.atlassian.jira.lookandfeel.upload;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.LookAndFeelBean;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.lookandfeel.ImageScaler;
import com.atlassian.jira.lookandfeel.LookAndFeelConstants;
import com.atlassian.jira.lookandfeel.image.ImageDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 *
 *
 * @since v4.4
 */
public class UploadServiceImpl implements UploadService
{

    private final ImageScaler imageScaler;
    private final JiraHome jiraHome;
    private final ApplicationProperties applicationProperties;
    private final PluginSettings globalSettings;
    private final JiraAuthenticationContext authenticationContex;

    public UploadServiceImpl(ImageScaler imageScaler, JiraHome jiraHome, ApplicationProperties applicationProperties,
            final JiraAuthenticationContext authenticationContext, PluginSettingsFactory globalSettingsFactory)
    {
        this.imageScaler = imageScaler;
        this.jiraHome = jiraHome;
        this.applicationProperties = applicationProperties;
        this.globalSettings = globalSettingsFactory.createGlobalSettings();
        this.authenticationContex = authenticationContext;
    }

    @Override
    public List<String> uploadLogo(ImageDescriptor imageDescriptor, LookAndFeelBean lookAndFeelBean)
    {

        LogoUploader logoUploader = new LogoUploader(applicationProperties, jiraHome, imageScaler, authenticationContex.getI18nHelper(), this);
        if (logoUploader.validate(imageDescriptor))
        {
            String logoUrl = logoUploader.saveLogo(imageDescriptor.getInputStream(), LookAndFeelConstants.JIRA_LOGO_FILENAME,
                    LookAndFeelConstants.JIRA_SCALED_LOGO_FILENAME);

            String width = Integer.toString(logoUploader.getResizedWidth());
            String height = Integer.toString(logoUploader.getResizedHeight());
            lookAndFeelBean.setLogoWidth(width);
            lookAndFeelBean.setLogoHeight(height);
            lookAndFeelBean.setLogoUrl(ensureUrlCorrect(logoUrl));
            globalSettings.put(LookAndFeelConstants.USING_CUSTOM_LOGO, BooleanUtils.toStringTrueFalse(true));
        }
        return logoUploader.getErrorMessages();
    }

    @Override
    public List<String> uploadFavicon(LookAndFeelBean lookAndFeelBean, ImageDescriptor imageDescriptor)
    {
        final LogoUploader logoUploader = new LogoUploader(applicationProperties, jiraHome, imageScaler, authenticationContex.getI18nHelper(), this);
        if (logoUploader.validate(imageDescriptor))
        {
            String faviconUrl = logoUploader.saveFavicon(imageDescriptor.getInputStream(),
                    LookAndFeelConstants.JIRA_FAVICON_FILENAME,
                    LookAndFeelConstants.JIRA_FAVICON_HIRES_FILENAME,
                    LookAndFeelConstants.JIRA_SCALED_FAVICON_FILENAME,
                    LookAndFeelConstants.JIRA_FAVICON_IEFORMAT_FILENAME );
            if (StringUtils.isNotBlank(faviconUrl))
            {
                faviconUrl = ensureUrlCorrect(faviconUrl);
                String faviconHiResUrl = "/" + LookAndFeelConstants.JIRA_FAVICON_HIRES_FILENAME;
                lookAndFeelBean.setFaviconUrl(faviconUrl);
                lookAndFeelBean.setFaviconHiResUrl(faviconHiResUrl);
                globalSettings.put(LookAndFeelConstants.USING_CUSTOM_FAVICON, BooleanUtils.toStringTrueFalse(true));
            }
        }
        return logoUploader.getErrorMessages();
    }

    @Override
    public File getLogoDirectory()
    {
        String logoDirectoryName = jiraHome.getHomePath() + "/logos";
        File logoDirectory = new File(logoDirectoryName);
        if (!logoDirectory.exists())
        {
            logoDirectory.mkdirs();
        }
        return logoDirectory;
    }

    @Override
    public Map<String, String> uploadDefaultFavicon(BufferedImage image)
    {
        LogoUploader logoUploader = new LogoUploader(applicationProperties, jiraHome, imageScaler, authenticationContex.getI18nHelper(), this);
        Dimension dimension = logoUploader.saveDefaultFavicOn(image, LookAndFeelConstants.JIRA_DEFAULT_FAVICON_FILENAME, LookAndFeelConstants.JIRA_SCALED_DEFAULT_FAVICON_FILENAME);
        globalSettings.put(LookAndFeelConstants.USING_CUSTOM_DEFAULT_FAVICON, BooleanUtils.toStringTrueFalse(true));
        return  MapBuilder.<String, String>newBuilder()
                    .add("path", "/images/" + LookAndFeelConstants.JIRA_SCALED_DEFAULT_FAVICON_FILENAME)
                    .add("width", "" + dimension.width)
                    .add("height", "" + dimension.height).toMap();
    }

    @Override
    public Map<String, String> uploadDefaultLogo(BufferedImage image)
    {
        LogoUploader logoUploader = new LogoUploader(applicationProperties, jiraHome, imageScaler, authenticationContex.getI18nHelper(), this);
        Dimension dimensions = logoUploader.saveDefaultLogo(image, LookAndFeelConstants.JIRA_DEFAULT_LOGO_FILENAME, LookAndFeelConstants.JIRA_SCALED_DEFAULT_LOGO_FILENAME);
        globalSettings.put(LookAndFeelConstants.USING_CUSTOM_DEFAULT_LOGO, BooleanUtils.toStringTrueFalse(true));
        return  MapBuilder.<String, String>newBuilder()
                    .add("path", "/images/" + LookAndFeelConstants.JIRA_SCALED_DEFAULT_LOGO_FILENAME)
                    .add("width", "" + dimensions.width)
                    .add("height", "" + dimensions.height).toMap();

    }

    private String ensureUrlCorrect(String url)
    {
        // url must start with 'http://', 'http://', or else add the leading '/'
        if (StringUtils.isNotBlank(url) && !url.startsWith("http") && !url.startsWith("/"))
        {
            url = "/" + url;
        }
        return url;
    }

}
