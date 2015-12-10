package com.atlassian.jira.web.filters;

import com.atlassian.core.filters.AbstractHttpFilter;
import com.atlassian.jira.bc.dataimport.DataImportService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.login.LoginManager;
import com.atlassian.jira.task.ImportTaskManager;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskProgressEvent;
import com.atlassian.jira.template.velocity.VelocityEngineFactory;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.LocaleParser;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.resourcebundle.DefaultResourceBundle;
import com.atlassian.jira.web.ServletContextKeys;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.util.ImportResultHandler;
import com.atlassian.jira.web.servletcontext.ServletContextReference;
import com.atlassian.jira.web.util.MetalResourcesManager;
import com.atlassian.johnson.JohnsonEventContainer;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

/**
 * This filter is used to report on progress of a data import.  It has to live in a filter first in the filterchain
 * since otherwhise we run the risk of having another filter call through to the ComponentManager.  This could cause
 * deadlocks in pico while the ComponentManager is being restarted during the data import!  This filter basically checks
 * if there's currently a data import task in progress and will show progress if there is.  Otherwise it simply
 * redirects to the dashboard.
 *
 * @since 4.4
 */
public class JiraImportProgressFilter extends AbstractHttpFilter
{
    private static final Logger log = LoggerFactory.getLogger(JiraImportProgressFilter.class);
    private static final String ALREADY_FILTERED = JiraImportProgressFilter.class.getName() + "_already_filtered";
    private static ServletContextReference<ImportTaskManager> taskManagerReference =
            new ServletContextReference<ImportTaskManager>(ServletContextKeys.DATA_IMPORT_TASK_MANAGER);
    private static ServletContextReference<SimpleTaskDescriptorBean> currentTaskReference =
            new ServletContextReference<SimpleTaskDescriptorBean>(ServletContextKeys.DATA_IMPORT_CURRENT_TASK);
    private ServletContext servletContext;


    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        super.init(filterConfig);
        servletContext = filterConfig.getServletContext();
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException
    {
        // Only apply this filter once per httpServletRequest
        if (request.getAttribute(ALREADY_FILTERED) != null)
        {
            filterChain.doFilter(request, response);

            return;
        }
        else
        {
            request.setAttribute(ALREADY_FILTERED, Boolean.TRUE);
        }

        if (taskManagerReference.get() != null && taskManagerReference.get().getTask() != null)
        {
            //setup encoding
            request.setCharacterEncoding("UTF-8");
            response.setContentType("text/html; charset=UTF-8");

            final String localeString = request.getParameter("locale");
            Locale locale = Locale.getDefault();
            if (StringUtils.isNotBlank(localeString))
            {
                locale = LocaleParser.parseLocale(localeString);
            }
            final SimpleTaskDescriptorBean currentTaskDescriptor = new SimpleTaskDescriptorBean(taskManagerReference.get().<DataImportService.ImportResult>getTask(), locale, taskManagerReference.get().getCachedResourceBundleStrings());
            currentTaskReference.set(currentTaskDescriptor);
            if (currentTaskDescriptor.isFinished())
            {
                try
                {
                    final DataImportService.ImportResult importResult = currentTaskDescriptor.getResult();
                    request.getSession(true).setAttribute(SessionKeys.DATA_IMPORT_RESULT, importResult);
                    if (Boolean.parseBoolean(request.getParameter("setup")))
                    {
                        response.sendRedirect(request.getContextPath() + "/secure/SetupImport.jspa");
                    }
                    else
                    {
                        if (importResult.isValid())
                        {
                            //render the JSP directly here since the XmlRestore action requires sysadmin privileges which
                            //you might loose after an import is finished.
                            ComponentAccessor.getComponent(LoginManager.class).logout(request, response);
                            taskManagerReference.get().clearCachedResourceBundleStrings();
                            response.sendRedirect(request.getContextPath() + "/secure/ImportResult.jspa");
                        }
                        else
                        {
                            //JRADEV-22455 :- if it's an upgrade error there is a chance the UserManager is stuffed so go to
                            // errors.jsp instead via a forward to avoid the login
                            handleImportErrors(importResult);
                            String url = "/secure/admin/XmlRestore!finish.jspa";
                            if (JohnsonEventContainer.get(servletContext).hasEvents())
                            {
                                url = "/secure/errors.jsp";
                            }
                            //go back to the webwork action to deal with normal errors.
                            taskManagerReference.get().clearCachedResourceBundleStrings();
                            response.sendRedirect(request.getContextPath() + url);
                        }
                    }

                    return;
                }
                catch (final ExecutionException e)
                {
                    currentTaskDescriptor.setExceptionCause(e.getCause() == null ? e : e.getCause());
                    log.error("Error performing import", e);
                }
                catch (final InterruptedException e)
                {
                    currentTaskDescriptor.setExceptionCause(e);
                    log.error("Error performing import", e);
                }
                finally
                {
                    taskManagerReference.get().shutdownNow();
                    taskManagerReference.set(null);
                    currentTaskReference.set(null);
                }
            }
            renderView(request, response, currentTaskDescriptor);
        }
        else
        {
            //no progress to report on.  Lets just go back to the dashboard.
            response.sendRedirect(request.getContextPath() + "/");
        }
    }

    private void handleImportErrors(DataImportService.ImportResult lastResult)
    {
        ImportResultHandler importResultHandler = ComponentAccessor.getComponent(ImportResultHandler.class);
        importResultHandler.handleErrorResult(servletContext, lastResult,
                ComponentAccessor.getI18nHelperFactory().getInstance(ComponentAccessor.getApplicationProperties().getDefaultLocale()),
                new SimpleErrorCollection());
    }


    private void renderView(HttpServletRequest request, HttpServletResponse response, SimpleTaskDescriptorBean task)
            throws ServletException
    {
        try
        {
            // use our own velocity instance, we can't use jira velocity manager
            // because it is in spring
            final VelocityEngine velocityEngine = new VelocityEngine();
            Properties props = new Properties();
            props.put("resource.loader", "class");
            props.put("class.resource.loader.description", "Velocity Classpath Resource Loader");
            props.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

            VelocityEngineFactory.Default.enableDevMode(props);

            props.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
            props.setProperty("runtime.log.logsystem.log4j.category", "velocity");
            props.setProperty("velocimacro.library", "");
            velocityEngine.setApplicationAttribute("javax.servlet.ServletContext", servletContext);
            velocityEngine.init(props);

            final Template template = velocityEngine.getTemplate("/templates/jira/importprogress/dataimportprogress.vm");

            VelocityContext context = new VelocityContext();
            context.put("resourcesHtml", MetalResourcesManager.getMetalResources(request.getContextPath()));
            context.put("task", task);
            template.merge(context, response.getWriter());
        }
        catch (Exception e)
        {
            throw new ServletException(e);
        }
    }

    public static class SimpleTaskDescriptorBean
    {
        private final SimpleDateFormat dateFormat = new SimpleDateFormat();
        private final TaskDescriptor<DataImportService.ImportResult> descriptor;
        private final Locale locale;
        private Throwable exceptionCause;
        private TaskProgressEvent lastProgressEvent;
        private String description;
        private ResourceBundle resourceBundle;
        private PeriodFormatter periodFormatter;
        private final Map<String, String> cachedResourceBundleStrings;

        SimpleTaskDescriptorBean(TaskDescriptor<DataImportService.ImportResult> descriptor, Locale locale, Map<String, String> cachedResourceBundleStrings)
        {
            this.descriptor = descriptor;
            this.locale = locale;
            this.description = descriptor.getDescription();
            this.lastProgressEvent = descriptor.getTaskProgressIndicator().getLastProgressEvent();
            this.cachedResourceBundleStrings = cachedResourceBundleStrings;
            resourceBundle = DefaultResourceBundle.getDefaultResourceBundle(locale);

            periodFormatter = new PeriodFormatterBuilder()
                    .printZeroNever()
                    .appendHours()
                    .appendSuffix(" " + getText("common.words.hour"), " " + getText("common.words.hours"))
                    .appendSeparator(", ")
                    .appendMinutes()
                    .appendSuffix(" " + getText("common.words.minute"), " " + getText("common.words.minutes"))
                    .appendSeparator(", ")
                    .appendSeconds()
                    .appendSuffix(" " + getText("common.words.second"), " " + getText("common.words.seconds"))
                    .toFormatter();
        }

        public boolean isFinished()
        {
            return descriptor.isFinished();
        }

        public DataImportService.ImportResult getResult() throws ExecutionException, InterruptedException
        {
            return descriptor.getResult();
        }

        public void setExceptionCause(Throwable throwable)
        {
            this.exceptionCause = throwable;
        }

        public Throwable getExceptionCause()
        {
            return exceptionCause;
        }

        public long getProgressNumber()
        {
            if (descriptor.isFinished() || (descriptor.getTaskProgressIndicator() == null))
            {
                return 100;
            }
            if (lastProgressEvent == null)
            {
                return 0;
            }
            return Math.max(Math.min(100, lastProgressEvent.getTaskProgress()), 0);
        }

        public long getInverseProgressNumber()
        {
            return 100 - getProgressNumber();
        }

        public String getDescription()
        {
            return description;
        }

        public String getProgressMessage()
        {
            if (lastProgressEvent.getCurrentSubTask() != null)
            {
                return lastProgressEvent.getCurrentSubTask() + ": " + lastProgressEvent.getMessage();
            }
            return lastProgressEvent.getMessage();
        }

        public String getFormattedProgress()
        {
            if (!descriptor.isStarted())
            {
                return getText("common.tasks.info.starting", formattedDate(descriptor.getSubmittedTimestamp()));
            }

            if (descriptor.isFinished())
            {
                if (exceptionCause != null)
                {
                    return getText("common.tasks.info.completed.with.error", getFormattedElapsedRunTime());
                }
                else
                {
                    return getText("common.tasks.info.completed", getFormattedElapsedRunTime());
                }
            }

            if (lastProgressEvent != null)
            {
                if (lastProgressEvent.getTaskProgress() >= 0)
                {
                    return getText("common.tasks.info.progressing", Long.toString(getProgressNumber()), getFormattedElapsedRunTime());
                }
            }

            return getText("common.tasks.info.progress.unknown", getFormattedElapsedRunTime());
        }

        public String getFormattedStartDate()
        {
            return getText("common.tasks.info.started", formattedDate(descriptor.getStartedTimestamp()));
        }

        private String formattedDate(final Date date)
        {
            return dateFormat.format(date);
        }

        public String getFormattedElapsedRunTime()
        {
            return periodFormatter.withLocale(locale).print(new Period(descriptor.getElapsedRunTime()));
        }

        public TaskProgressEvent getLastProgressEvent()
        {
            return lastProgressEvent;
        }

        public boolean isStarted()
        {
            return descriptor.isStarted();
        }

        public String getText(String key)
        {
            String text = cachedResourceBundleStrings.containsKey(key) ? cachedResourceBundleStrings.get(key) : resourceBundle.getString(key);
            return MessageFormat.format(text, new Object[0]);
        }

        public String getText(String key, String... args)
        {
            String text = cachedResourceBundleStrings.containsKey(key) ? cachedResourceBundleStrings.get(key) : resourceBundle.getString(key);
            return MessageFormat.format(text, args);
        }
    }
}
