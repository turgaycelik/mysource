package com.atlassian.jira.servlet;

import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.web.action.admin.GeneralConfigurationUpdatedEvent;
import com.atlassian.util.concurrent.ResettableLazyReference;

import com.octo.captcha.service.image.ImageCaptchaService;

@EventComponent
public final class JiraCaptchaServiceImpl implements JiraCaptchaService
{
    // These really shouldn't be in a static, but possible reliance on the broken behaviour of resetting all instances
    // during the construction of a new instance makes it unavoidable for now.  Note that it is dangerous that we are
    // referencing ComponentAccessor.getComponent inside of a lazy reference and the only reason we can get away with
    // it is that the JiraCaptchaService is unlikely to be accessed, either directly or indirectly, by either the
    // FeatureManager or ApplicationProperties.
    private static ResettableLazyReference<JiraCaptchaService> delegate = new ResettableLazyReference<JiraCaptchaService>()
    {
        @Override
        protected JiraCaptchaService create() throws Exception
        {
            final FeatureManager featureManager = ComponentAccessor.getComponent(FeatureManager.class);
            final boolean shouldUseCaptcha = publicSignup();
            if (featureManager.isOnDemand() && !shouldUseCaptcha)
            {
                return new NoOpCaptchaServiceImpl();
            }
            else
            {
                return new JiraImageCaptchaServiceImpl();
            }
        }
    };

    // In case this is created outside of PICO injection, reset the lazy reference
    public JiraCaptchaServiceImpl()
    {
        delegate.reset();
    }

    private static boolean publicSignup()
    {
        final ApplicationProperties applicationProperties = ComponentAccessor.getApplicationProperties();
        return ("public".equals(applicationProperties.getString(APKeys.JIRA_MODE)) && applicationProperties.getOption(APKeys.JIRA_OPTION_CAPTCHA_ON_SIGNUP));
    }

    public static ImageCaptchaService getInstance()
    {
        return delegate.get().getImageCaptchaService();
    }

    public ImageCaptchaService getImageCaptchaService()
    {
        return getInstance();
    }

    @EventListener
    @SuppressWarnings("unused")
    public void onGeneralConfigurationChange(GeneralConfigurationUpdatedEvent event)
    {
        delegate.reset();
    }
}
