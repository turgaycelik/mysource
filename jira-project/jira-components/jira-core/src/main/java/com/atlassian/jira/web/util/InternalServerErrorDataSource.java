package com.atlassian.jira.web.util;

import com.atlassian.core.logging.DatedLoggingEvent;
import com.atlassian.core.logging.ThreadLocalErrorCollection;
import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.system.ExtendedSystemInfoUtils;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginInformation;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.opensymphony.module.propertyset.PropertySet;

import org.apache.log4j.spi.LoggingEvent;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public class InternalServerErrorDataSource
{

    public static final String UNKNOWN = "Unknown";
    private final I18nHelper i18nBean;

    @Nullable
    private final ExtendedSystemInfoUtils extendedSystemInfoUtils;

    private final ServletContext servletContext;
    private final LocaleManager localeManager;

    private final HttpServletRequest request;
    private boolean prepareDataForSoy;

    public InternalServerErrorDataSource(final I18nHelper i18nHelper, final ExtendedSystemInfoUtils extendedSystemInfoUtils, final ServletContext servletContext, final LocaleManager localeManager, final HttpServletRequest req)
    {
        this.i18nBean = i18nHelper;
        this.extendedSystemInfoUtils = extendedSystemInfoUtils;
        this.servletContext = servletContext;
        this.localeManager = localeManager;
        this.request = req;
        this.prepareDataForSoy = true;
    }

    public ImmutableMap.Builder<String, Object> appendFullMessageData(ImmutableMap.Builder<String, Object> map, boolean isSysadmin)
    {

        map.put("generalInfo", getGeneralInfo());
        map.put("serverInfo", arrarify(translateKeys(getServerInfo(), i18nBean)));
        map.put("requestInfo", arrarify(translateKeys(getRequestInfo(), i18nBean)));
        map.put("requestAttributes", arrarify(translateKeys(getRequestAttributes(), i18nBean)));
        map.put("loggingEvents", getLoggingEvents());


        if (extendedSystemInfoUtils != null)
        {
            if (isSysadmin)
            {
                map.put("filePaths", arrarify(translateKeys(getFilePaths(), i18nBean)));
                map.put("sysInfo", arrarify(extendedSystemInfoUtils.getProps()));
            }
            else
            {
                Map<String, String> sysProps = extendedSystemInfoUtils.getProps();
                Set<String> sysadminOnlyProps = ImmutableSet.of(i18nBean.getText("admin.systeminfo.system.cwd"),
                        i18nBean.getText("admin.systeminfo.jvm.input.arguments"));
                map.put("sysInfo", arrarify(filterKeys(sysProps, sysadminOnlyProps, i18nBean.getText("system.error.property.not.sysadmin"))));
            }
            map.put("languageInfo", translateKeys(getLanguageInfo(), i18nBean));
            map.put("listeners", getListeners());
            map.put("services", getServices());
            map.put("buildInfoData", arrarify(extendedSystemInfoUtils.getBuildStats()));
            map.put("memInfo", arrarify(extendedSystemInfoUtils.getJvmStats()));
            map.put("plugins", getPlugins());
        }
        return map;
    }

    public ImmutableMap.Builder<String, Object> appendSimpleMessageData(ImmutableMap.Builder<String, Object> map)
    {
        map.put("generalInfo", getGeneralInfo());
        return map;
    }

    private Map<String, Object> getGeneralInfo()
    {
        final ImmutableMap.Builder<String, Object> map = ImmutableMap.builder();
        InternalServerErrorExceptionDataSource exceptionInfo = new InternalServerErrorExceptionDataSource((Throwable) request.getAttribute("javax.servlet.error.exception"), extendedSystemInfoUtils);
        map.put("interpretedMsg", exceptionInfo.getInterpretedMessage());
        map.put("cause", exceptionInfo.getRootCause());
        map.put("stacktrace", exceptionInfo.getStacktrace());
        map.put("referer", request.getHeader("Referer") != null ? request.getHeader("Referer") : UNKNOWN);
        map.put("servletErrorMessage", request.getAttribute("javax.servlet.error.message") != null ? request.getAttribute("javax.servlet.error.message") : "");
        return map.build();
    }

    private Map<String, Object> getLanguageInfo()
    {
        String defaultLang = extendedSystemInfoUtils.getDefaultLanguage();

        if (extendedSystemInfoUtils.isUsingSystemLocale())
        {
            defaultLang = defaultLang + " - " + i18nBean.getText("admin.systeminfo.system.default.locale");
        }

        return ImmutableMap.<String, Object>of(
                "admin.generalconfiguration.installed.languages",
                ImmutableList.copyOf(Iterables.transform(localeManager.getInstalledLocales(), new Function<Locale, String>()
                {
                    @Override
                    public String apply(@Nullable final Locale input)
                    {
                        return input.getDisplayName(i18nBean.getLocale());
                    }
                }
                )),
                "admin.generalconfiguration.default.language", defaultLang
        );
    }


    private Map<String, String> getServerInfo()
    {
        return ImmutableMap.of(
                "system.error.application.server", servletContext.getServerInfo(),
                "system.error.servlet.version", servletContext.getMajorVersion() + "." + servletContext.getMinorVersion());
    }

    private Map<String, String> getFilePaths()
    {
        return ImmutableMap.of(
                "system.error.location.of.log", extendedSystemInfoUtils.getLogPath(),
                "system.error.location.of.entityengine", extendedSystemInfoUtils.getEntityEngineXmlPath());
    }


    private Map<String, String> getRequestInfo()
    {
        Map<String, String> map = Maps.newHashMapWithExpectedSize(10);
        map.put("system.error.request.url", request.getRequestURL().toString());
        map.put("system.error.scheme", request.getScheme());
        map.put("system.error.server", request.getServerName());
        map.put("system.error.port", Integer.toString(request.getServerPort()));
        map.put("system.error.uri", request.getRequestURI());
        map.put("system.error.context.path", request.getContextPath());
        map.put("system.error.servlet.path", request.getServletPath());
        map.put("system.error.path.info", request.getPathInfo());
        map.put("system.error.query.string", request.getQueryString());
        return map;
    }

    private Map<String, String> getRequestAttributes()
    {
        ImmutableMap.Builder<String, String> map = ImmutableMap.builder();
        Enumeration attributeNames = request.getAttributeNames();
        while (attributeNames.hasMoreElements())
        {
            String attributeName = (String) attributeNames.nextElement();
            Object value = request.getAttribute(attributeName);
            if (value != null)
            {
                map.put(attributeName, value.toString());

            }
            else
            {
                map.put(attributeName, "null");
            }
        }
        return map.build();
    }

    private List<Map<String, Object>> getLoggingEvents()
    {
        return new ObjectToMapConverter<DatedLoggingEvent>()
        {
            @Override
            public void append(final DatedLoggingEvent input, final Map<String, Object> map)
            {
                LoggingEvent event = input.getEvent();
                map.put("loggerName", event.getLoggerName());
                map.put("level", event.getLevel());
                map.put("date", input.getDate().toString());
                map.put("message", event.getRenderedMessage());
                map.put("throwableStrRep", event.getThrowableInformation() != null ? event.getThrowableStrRep() : Collections.emptyList());
            }
        }.build(ThreadLocalErrorCollection.getList());
    }

    private List<Map<String, Object>> getListeners()
    {
        return new ObjectToMapConverter<GenericValue>()
        {
            @Override
            public void append(final GenericValue input, final Map<String, Object> map)
            {
                map.put("name", input.getString("name"));
                map.put("clazz", input.getString("clazz"));
                PropertySet propset = OFBizPropertyUtils.getPropertySet(input);
                Collection<String> keys = propset.getKeys(PropertySet.STRING);
                if (keys == null)
                {
                    keys = Collections.emptyList();
                }

                Map<String, String> values = Maps.newHashMapWithExpectedSize(keys.size());
                for (String key : keys)
                {
                    values.put(key, propset.getString(key));
                }
                map.put("properties", arrarify(values));
            }
        }.build(extendedSystemInfoUtils.getListeners());
    }


    private List<Map<String, Object>> getServices()
    {
        return new ObjectToMapConverter<JiraServiceContainer>()
        {
            @Override
            public void append(final JiraServiceContainer input, final Map<String, Object> map)
            {
                map.put("name", input.getName());
                map.put("class", input.getServiceClass());
                map.put("delay", extendedSystemInfoUtils.getMillisecondsToMinutes(input.getDelay()));
                map.put("properties", arrarify(translateValues(extendedSystemInfoUtils.getServicePropertyMap(input), i18nBean)));
            }
        }.build(extendedSystemInfoUtils.getServices());
    }

    private List<Map<String, Object>> getPlugins()
    {
        return new ObjectToMapConverter<Plugin>()
        {
            @Override
            public void append(final Plugin input, Map<String, Object> map)
            {
                PluginInformation info = input.getPluginInformation();
                map.put("name", input.getName());
                map.put("version", info.getVersion());
                map.put("vendor", info.getVendorName());
                map.put("enabled", extendedSystemInfoUtils.isPluginEnabled(input));
                map.put("parameters", arrarify(info.getParameters()));
            }
        }.build(extendedSystemInfoUtils.getPlugins());
    }

    public void notForSoy()
    {
        prepareDataForSoy = false;
    }


    //###### HELPERS

    private static <T> Map<String, String> filterKeys(Map<String, String> sourceMap, final Set<String> keysToRemove, final String replacementValue)
    {
        if (replacementValue == null)
        {
            return Maps.filterKeys(sourceMap, new Predicate<String>()
            {
                @Override
                public boolean apply(@Nullable final String input)
                {
                    return !keysToRemove.contains(input);
                }
            });
        }
        else
        {
            return Maps.transformEntries(sourceMap, new Maps.EntryTransformer<String, String, String>()
            {
                @Override
                public String transformEntry(@Nullable final String key, @Nullable final String value)
                {
                    if (keysToRemove.contains(key))
                    {
                        return replacementValue;
                    }
                    else
                    {
                        return value;
                    }
                }
            });
        }

    }

    private static <T> Map<String, T> translateKeys(Map<String, T> sourceMap, final I18nHelper i18n)
    {
        Map<String, T> resultMap = Maps.newHashMapWithExpectedSize(sourceMap.size());
        for (Map.Entry<String, T> entry : sourceMap.entrySet())
        {
            resultMap.put(i18n.getText(entry.getKey()), entry.getValue());
        }
        return resultMap;
    }

    private static Map<String, String> translateValues(Map<String, String> sourceMap, final I18nHelper i18n)
    {
        return Maps.transformValues(sourceMap, new Function<String, String>()
        {
            @Override
            public String apply(@Nullable final String input)
            {
                return i18n.getText(input);
            }
        });

    }

    private <T> Object arrarify(Map<String, T> values)
    {
        if (prepareDataForSoy)
        {
            return listifyMap(values);
        }
        else
        {
            return values;
        }
    }

    private static <T> List<Map<String, Object>> listifyMap(Map<String, T> values)
    {
        return ImmutableList.copyOf(Iterables.transform(values.entrySet(), new Function<Map.Entry<String, T>, Map<String, Object>>()
        {
            @Override
            public Map<String, Object> apply(final Map.Entry<String, T> input)
            {
                //sadly immutablemap.of wont work due to possible null values
                Map<String, Object> result = Maps.newHashMapWithExpectedSize(2);
                result.put("key", input.getKey());
                result.put("value", input.getValue());
                return result;
            }
        }));
    }

    private static <T> Map<String, T> underscorify(Map<String, T> values)
    {
        Map<String, T> result = Maps.newHashMapWithExpectedSize(values.size());
        for (Map.Entry<String, T> entry : values.entrySet())
        {
            result.put(entry.getKey().replace(".", "_"), entry.getValue());
        }
        return result;
    }


    private abstract static class ObjectToMapConverter<T>
    {
        public List<Map<String, Object>> build(Iterable<T> input)
        {
            if (input == null)
            {
                return Collections.emptyList();
            }
            return ImmutableList.copyOf(Iterables.transform(input, new Function<T, Map<String, Object>>()
            {
                @Override
                public Map<String, Object> apply(@Nullable final T input)
                {
                    Map<String, Object> map = Maps.newHashMap();
                    append(input, map);
                    return map;
                }
            }));
        }

        public abstract void append(T input, Map<String, Object> map);

    }


}
