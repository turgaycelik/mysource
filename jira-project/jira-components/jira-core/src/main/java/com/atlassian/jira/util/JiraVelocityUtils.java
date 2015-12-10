package com.atlassian.jira.util;

import com.atlassian.core.util.XMLUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.datetime.DateTimeVelocityUtils;
import com.atlassian.jira.datetime.DateVelocityUtils;
import com.atlassian.jira.mail.JiraMailQueueUtils;
import com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager;
import com.atlassian.jira.plugin.profile.UserFormatManager;
import com.atlassian.jira.render.Encoder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.template.soy.SoyTemplateRendererProvider;
import com.atlassian.jira.util.collect.CompositeMap;
import com.atlassian.jira.util.collect.MemoizingMap.Master;
import com.atlassian.jira.util.collect.MemoizingMap.Master.Builder;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.bean.PermissionCheckBean;
import com.atlassian.jira.web.component.IssueConstantWebComponent;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.atlassian.jira.web.util.ExternalLinkUtilImpl;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import com.atlassian.util.concurrent.LazyReference;
import com.opensymphony.util.TextUtils;
import org.apache.velocity.app.FieldMethodizer;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.atlassian.jira.datetime.DateTimeStyle.COMPLETE;

/**
 * Helper class that contains a number of utility methods for velocity templates.
 *
 * This class is not in the JIRA API, so Plugin developers are recommended to use {@link VelocityParamFactory} instead, which
 * can be dependency injected, else use {@link com.atlassian.jira.component.ComponentAccessor#getVelocityParamFactory()}.
 */
public class JiraVelocityUtils
{
    private static final Master<String, Object> MASTER;

    static
    {
        //WARNING: Do not add items that have direct references to JIRA PICO or Plugin components. This map is static
        //and you will end up holding onto references to components after they are destoryed during a restore or
        // plugin restart. If you need to access PICO/Plugin components then call addLazy instead.

        final Builder<String, Object> builder = Master.builder();
        builder.add("xmlutils", new XMLUtils());
        builder.add("textutils", new TextUtils());
        builder.add("urlcodec", new JiraUrlCodec());
        builder.add("urlModeAbsolute", UrlMode.ABSOLUTE);
        builder.add("dateTimeStyle", new FieldMethodizer(DateTimeStyle.class.getName()));

        // lazily created items
        builder.addLazy("currentMillis", new Supplier<Long>()
        {
            public Long get()
            {
                return System.currentTimeMillis();
            }
        });
        builder.addLazy("externalLinkUtil", new Supplier<ExternalLinkUtil>()
        {
            public ExternalLinkUtil get()
            {
                return ExternalLinkUtilImpl.getInstance();
            }
        });

        final Supplier<VelocityRequestContext> requestContext = new Supplier<VelocityRequestContext>()
        {
            public VelocityRequestContext get()
            {
                // a request context object should be used instead of using the 'req' object, or 'baseurl' object below. (JRA-11038)
                return new DefaultVelocityRequestContextFactory(ComponentAccessor.getApplicationProperties()).getJiraVelocityRequestContext();
            }
        };

        builder.addLazy("requestContext", requestContext);
        builder.addLazy("baseurl", new Supplier<String>()
        {
            public String get()
            {
                return requestContext.get().getBaseUrl();
            }
        });

        builder.addLazy("issueConstantWebComponent",  new Supplier<IssueConstantWebComponent>() {

            public IssueConstantWebComponent get()
            {
                return new IssueConstantWebComponent();
            }
        });

        builder.addLazy("webResourceManager", new Supplier<WebResourceManager>()
        {
            public WebResourceManager get()
            {
                return ComponentAccessor.getWebResourceManager();
            }
        });
        builder.addLazy("webResourceUrlProvider", new Supplier<WebResourceUrlProvider>()
        {
            public WebResourceUrlProvider get()
            {
                return ComponentAccessor.getWebResourceUrlProvider();
            }
        });
        builder.addLazy("userformat", new Supplier<UserFormatManager>()
        {
            public UserFormatManager get()
            {
                return ComponentAccessor.getComponentOfType(UserFormatManager.class);
            }
        });
        builder.addLazy("map", new Supplier<EasyMap>()
        {
            public EasyMap get()
            {
                return new EasyMap();
            }
        });
        builder.addLazy("atl_token", new Supplier<String>()
        {
            public String get()
            {
                return getXsrfToken();
            }
        });
        builder.addLazy("keyboardShortcutManager",new Supplier<KeyboardShortcutManager>()
        {
            public KeyboardShortcutManager get()
            {
                return ComponentAccessor.getComponentOfType(KeyboardShortcutManager.class);
            }
        });
        builder.addLazy("dateTimes", new Supplier<DateTimeVelocityUtils>()
        {
            @Override
            public DateTimeVelocityUtils get()
            {
                return new DateTimeVelocityUtils(dateTimeFormatter());
            }
        });
        builder.addLazy("dates", new Supplier<DateVelocityUtils>()
        {
            @Override
            public DateVelocityUtils get()
            {
                return new DateVelocityUtils(dateTimeFormatter());
            }
        });
        builder.addLazy("cfValueEncoder", new Supplier<Encoder>()
        {
            @Override
            public Encoder get()
            {
                return ComponentAccessor.getComponentOfType(Encoder.class);
            }
        });

        MASTER = builder.master();
    }

    /**
     * Static method to construct a map with a number of common parameters used by velocity templates.
     * <p>
     * For API compliance, use {@link VelocityParamFactory#getDefaultVelocityParams(JiraAuthenticationContext)} instead.
     *
     * @param authenticationContext JiraAuthenticationContext
     * @return a Map with common velocity parameters
     */
    public static Map<String, Object> getDefaultVelocityParams(final JiraAuthenticationContext authenticationContext)
    {
        return getDefaultVelocityParams(new HashMap<String, Object>(), authenticationContext);
    }

    /**
     * Static method to construct a map with a number of common parameters used by velocity templates.
     * <p>
     * For API compliance, use {@link VelocityParamFactory#getDefaultVelocityParams(java.util.Map, com.atlassian.jira.security.JiraAuthenticationContext)} instead.
     *
     * @param startingParams        Map of parameters that may be used to override any of the parameters set here.
     * @param authenticationContext JiraAuthenticationContext
     * @return a Map with common velocity parameters
     */
    public static Map<String, Object> getDefaultVelocityParams(Map<String, Object> startingParams, final JiraAuthenticationContext authenticationContext)
    {
        //JRADEV-990: startingParams needs to be a mutable map.
        startingParams = (startingParams == null) ? new HashMap<String, Object>() : startingParams;
        return CompositeMap.of(startingParams, createVelocityParams(authenticationContext));
    }

    public static Map<String, Object> createVelocityParams(final JiraAuthenticationContext authenticationContext)
    {
        // lazy master builder for the parameters
        final Map<String, Object> localParams = new HashMap<String, Object>();

        localParams.put("currentCalendar", new LazyCalendar(authenticationContext.getLocale(),
                ComponentAccessor.getComponentOfType(ApplicationProperties.class)));
        localParams.put("authcontext", authenticationContext);
        localParams.put("outlookdate", authenticationContext.getOutlookDate());
        localParams.put("dateFormatter", dateTimeFormatter().withStyle(COMPLETE).forLoggedInUser());
        localParams.put("dateutils", new DateUtils(authenticationContext));
        // an bean to help with concise permission checks (done as part of JRA-13469) but needed in general
        localParams.put("permissionCheck", new PermissionCheckBean(authenticationContext, ComponentAccessor.getPermissionManager()));
        localParams.put("featureManager", ComponentAccessor.getComponent(FeatureManager.class));
        localParams.put("soyRenderer", ComponentAccessor.getComponent(SoyTemplateRendererProvider.class).getRenderer());

        if (ExecutingHttpRequest.get() != null)
        {
            localParams.put("req", ExecutingHttpRequest.get());
        }

        return MASTER.combine(JiraMailQueueUtils.getContextParamsMaster()).toMap(localParams);
    }

    private static DateTimeFormatter dateTimeFormatter()
    {
        return ComponentAccessor.getComponentOfType(DateTimeFormatter.class);
    }

    private static String getXsrfToken()
    {
        return getXsrfTokenGenerator().generateToken();
    }

    private static XsrfTokenGenerator getXsrfTokenGenerator()
    {
        return ComponentAccessor.getComponentOfType(XsrfTokenGenerator.class);
    }

    public static class LazyCalendar
    {
        private final LazyReference<Calendar> reference;
        private final ApplicationProperties applicationProperties;

        LazyCalendar(final Locale locale, final ApplicationProperties applicationProperties)
        {
            this.applicationProperties = applicationProperties;
            reference = new LazyReference<Calendar>()
            {
                @Override
                protected Calendar create() throws Exception
                {
                    return Calendar.getInstance(locale);
                }
            };
        }

        public int getFirstDayOfWeek()
        {
            return reference.get().getFirstDayOfWeek();
        }

        public boolean isUseISO8601()
        {
            return applicationProperties.getOption(APKeys.JIRA_DATE_TIME_PICKER_USE_ISO8601);
        }
    }

    /**
     * Date utils class.
     */
    static class DateUtils extends com.atlassian.core.util.DateUtils
    {
        public DateUtils(JiraAuthenticationContext authenticationContext)
        {
            super(authenticationContext.getI18nHelper().getDefaultResourceBundle());
        }

        /**
         * This method is used to instantiate a Date in a Velocity template.
         *
         * @param currentMillis a long
         * @return a new Date
         */
        public Date date(long currentMillis)
        {
            return new Date(currentMillis);
        }
    }
}
