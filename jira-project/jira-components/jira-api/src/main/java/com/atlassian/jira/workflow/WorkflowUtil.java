package com.atlassian.jira.workflow;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.core.util.StringUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.workflow.names.WorkflowCopyNameFactory;

import com.google.common.base.Preconditions;
import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.WorkflowContext;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.opensymphony.workflow.loader.WorkflowLoader;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import static org.apache.commons.lang.StringUtils.stripToNull;

public class WorkflowUtil
{
    private static final Logger log = Logger.getLogger(WorkflowUtil.class);

    /**
     * This method adds to an existing list stored in the transient args map.
     * <p/>
     * If the existing list does not exist, the new list is just added -
     * otherwise the new list is added to the old list, and the result readded
     * to the transientArgs map.
     */
    public static void addToExistingTransientArgs(final Map transientArgs, final String key, final List list)
    {
        final List existingList = (List) transientArgs.get(key);

        if (existingList == null)
        {
            transientArgs.put(key, list);
        }
        else
        {
            existingList.addAll(list);
            transientArgs.put(key, existingList);
        }
    }

    /**
     * Get the next usable ID value for a given list of descriptors.
     */
    public static int getNextId(final List descriptors)
    {
        return getNextId(descriptors, 1);
    }

    /**
     * Get the next usable ID value for a given list of descriptors and a start point.
     */
    public static int getNextId(final List descriptors, final int start)
    {
        int maxId = start;
        for (final Object descriptor1 : descriptors)
        {
            final AbstractDescriptor descriptor = (AbstractDescriptor) descriptor1;
            if (descriptor.getId() >= maxId)
            {
                maxId = descriptor.getId() + 1;
            }
        }

        return maxId;
    }

    /**
     * Variable interpolation. Eg. given a project TestProject and groupName '${pkey}-users', will return 'TP-users', or null if groupName is null
     *
     * @deprecated Use {@link #replaceProjectKey(com.atlassian.jira.project.Project, String)} instead. Since v5.0.
     */
    public static String interpolateProjectKey(final GenericValue project, String groupName)
    {
        if ((groupName != null) && (groupName.indexOf("${") != -1) && (groupName.indexOf("}") != -1))
        {
            groupName = groupName.substring(0, groupName.indexOf("${")) + project.getString("key") + groupName.substring(groupName.indexOf("}") + 1);
        }
        return groupName;
    }

    /**
     * Replaces ${pkey} in the given groupName with the given Project's key.
     *
     * Eg. given a project TestProject(key="TP") and groupName '${pkey}-users', will return 'TP-users', or null if groupName is null
     *
     * @deprecated Use Project Roles instead. Since v5.2.
     */
    public static String replaceProjectKey(final Project project, String groupName)
    {
        if (groupName == null)
        {
            return null;
        }

        int index = groupName.indexOf("${pkey}");
        if (index == -1)
        {
            return groupName;
        }
        return groupName.substring(0, index) + project.getKey() + groupName.substring(index + "${pkey}".length());
    }

    /**
     * Return a meta attribute applying to a whole workflow (ie. right under the <workflow> start tag).
     *
     * <p/>
     * This method is deprecated because it is uses GenericValues and not considered a good candidate for a static utility method.
     * If you really want to get hold of a Workflow's global meta attributes, use the WorkflowManager component directly.
     * eg:
     * <pre>
     *     workflowManager.getWorkflow(issue).getDescriptor().getMetaAttributes().get(metaKey);
     * </pre>
     *
     * @deprecated Use WorkflowManager instead as described above. Since v6.3.
     */
    public static String getGlobalMetaAttributeForIssue(final GenericValue issue, final String metaKey)
    {
        JiraWorkflow issueWorkflow = null;
        try
        {
            issueWorkflow = getWorkflowManager().getWorkflow(issue);
        }
        catch (final WorkflowException e)
        {
            throw new RuntimeException("Could not get workflow for issue " + issue);
        }
        final String metaValue = (String) issueWorkflow.getDescriptor().getMetaAttributes().get(metaKey);
        return interpolate(metaValue, issue);
    }

    /**
     * Return a workflow meta attribute for the current state of an issue.
     *
     * <p/>
     * This method is deprecated because it is uses GenericValues and not considered a good candidate for a static utility method.
     * If you really want to get hold of a Workflow meta attributes, use the {@link com.atlassian.jira.issue.Issue} object and {@link WorkflowManager} component.
     * eg:
     * <pre>
     *     workflowManager.getWorkflow(issue).getLinkedStep(issue.getStatusObject()).getMetaAttributes();
     * </pre>
     *
     * @deprecated Use {@link com.atlassian.jira.issue.Issue} and {@link WorkflowManager} instead as described above. Since v6.3.
     */
    public static String getMetaAttributeForIssue(final GenericValue issue, final String metaKey)
    {
        final String metaValue = (String) getMetaAttributesForIssue(issue).get(metaKey);
        return interpolate(metaValue, issue);
    }

    /**
     * Return all meta attribute values whose key starts with a certain prefix. For example, given:
     * <meta name="jira.status.id">3</meta>
     * <meta name="jira.permission.subtasks.comment.group">jira-qa</meta>
     * <meta name="jira.permission.subtasks.comment.group.1">jira-administrators</meta>
     * <p/>
     * Prefix 'jira.permission.subtasks.comment.group' would return {'jira-qa', 'jira-administrators'}.
     * Unfortunately OSWorkflow does not allow multiple meta attributes with the same name.
     *
     * <p/>
     * This method is deprecated because it is uses GenericValues and not considered a good candidate for a static utility method.
     * If you really want to get hold of a Workflow meta attributes, use the {@link com.atlassian.jira.issue.Issue} object and {@link WorkflowManager} component.
     * eg:
     * <pre>
     *     workflowManager.getWorkflow(issue).getLinkedStep(issue.getStatusObject()).getMetaAttributes();
     * </pre>
     *
     * @deprecated Use {@link com.atlassian.jira.issue.Issue} and {@link WorkflowManager} instead as described above. Since v6.3.
     */
    public static List getMetaAttributesForIssue(final GenericValue issue, final String metaKeyPrefix)
    {
        final Map metaAttributes = getMetaAttributesForIssue(issue);
        final List results = new ArrayList(metaAttributes.size());
        final Iterator iter = metaAttributes.keySet().iterator();
        while (iter.hasNext())
        {
            final String key = (String) iter.next();
            if (key.startsWith(metaKeyPrefix))
            {
                results.add(interpolate((String) metaAttributes.get(key), issue));
            }
        }
        return results;
    }

    /**
     * Get all meta attributes for an issue's current state.
     *
     * <p/>
     * This method is deprecated because it is uses GenericValues and not considered a good candidate for a static utility method.
     * If you really want to get hold of a Workflow meta attributes, use the {@link com.atlassian.jira.issue.Issue} object and {@link WorkflowManager} component.
     * eg:
     * <pre>
     *     workflowManager.getWorkflow(issue).getLinkedStep(issue.getStatusObject()).getMetaAttributes();
     * </pre>
     *
     * @deprecated Use {@link com.atlassian.jira.issue.Issue} and {@link WorkflowManager} instead as described above. Since v6.3.
     */
    public static Map getMetaAttributesForIssue(final GenericValue issue)
    {
        StepDescriptor stepDesc = null;
        try
        {
            stepDesc = WorkflowUtil.getStepDescriptorForIssue(issue);
        }
        catch (final WorkflowException e)
        {
            throw new RuntimeException("Could not get workflow for issue " + issue);
        }
        final Map metaAttributes = stepDesc.getMetaAttributes();
        if (metaAttributes == null)
        {
            throw new RuntimeException("Null meta attributes");
        }
        return metaAttributes;
    }

    /**
     * Converts a {@link com.opensymphony.workflow.loader.WorkflowDescriptor} to XML.
     *
     * @param descriptor The {@link com.opensymphony.workflow.loader.WorkflowDescriptor} to convert
     * @return An XML representation of the workflowdescritpor passed in.
     */
    public static String convertDescriptorToXML(final WorkflowDescriptor descriptor)
    {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter);
        writer.println(WorkflowDescriptor.XML_HEADER);
        writer.println(WorkflowDescriptor.DOCTYPE_DECL);
        descriptor.writeXML(writer, 0);
        writer.flush();
        writer.close();

        return stringWriter.toString();
    }

    private static String interpolate(final String metaValue, final GenericValue issue)
    {
        if ((metaValue != null) && (metaValue.indexOf("${") != -1))
        {
            GenericValue project = null;
            project = ComponentAccessor.getProjectManager().getProject(issue);
            return WorkflowUtil.interpolateProjectKey(project, metaValue);
        }
        else
        {
            return metaValue;
        }
    }

    /**
     * Check if given workflow name is valid that means it: Is not blank, contains only ASCII characters, does not
     * contain leading or trailing whitespaces If any of the above is not meet the first error is added to {@code errorCollection}
     * If workflow name is valid then no error will be added to {@code errorCollection}
     *
     * @param workflowName name of the workflow to check
     * @param fieldName field name that the error should be associated with in {@code errorCollection}
     * @param errorCollection error collection that collects errors
     * @return true if workflow name is valid false otherwise
     * @throws NullPointerException if fieldName or errorCollection is null
     * @since 5.1.7
     */
    public static boolean isAcceptableName(final String workflowName, @Nonnull final String fieldName, @Nonnull final ErrorCollection errorCollection)
    {
        return isAcceptableNameInt(workflowName,
                Preconditions.checkNotNull(fieldName),
                Preconditions.checkNotNull(errorCollection));
    }

    /**
     * See {@link #isAcceptableName(String, String, com.atlassian.jira.util.ErrorCollection)}
     * <p/>
     * This method does not provide information about error type.
     */
    public static boolean isAcceptableName(final String workflowName)
    {
        return isAcceptableNameInt(workflowName, null, null);
    }

    private static boolean isAcceptableNameInt(String workflowName, @Nullable String fieldName, @Nullable ErrorCollection errorCollection)
    {
        if (org.apache.commons.lang.StringUtils.isBlank(workflowName))
        {
            addError(errorCollection, fieldName, "admin.errors.you.must.specify.a.workflow.name");
            return false;
        }
        else if (!StringUtils.isStringAllASCII(workflowName))
        {
            addError(errorCollection, fieldName, "admin.errors.please.use.only.ascii.characters");
            return false;
        }
        else if (!workflowName.trim().equals(workflowName))
        {
            // JRA-29521 in MSSQL and MySQL (in postgresql and oracle this does not happen) comparison between char and varchar
            // does not take into account trailing spaces so in those databases 'a' = 'a '
            // However those databases returns values for varchar according to ANSI specification with trailing spaces.
            // Because in JIRA workflow are identified by name when user copied workflow that was only different by
            // trailing space the java implementation though that this is different name but when OfBizWorkflowDescriptorStore
            // tried to get the workflow from db it go the original. So when the copy was saved the original was lost
            // and the data was corrupted. Issues and schema had assigned workflow that no longer exists (the one without space).
            addError(errorCollection, fieldName, "admin.errors.workflow.name.cannot.contain.leading.or.trailing.whitespaces");
            return false;
        }
        return true;
    }

    private static void addError(ErrorCollection errorCollection, String fieldName, String errorKey)
    {
        if (errorCollection == null)
        {
            return;
        }
        if (fieldName == null)
        {
            errorCollection.addErrorMessage(getI18nBean().getText(errorKey));
            return;
        }
        errorCollection.addError(fieldName, getI18nBean().getText(errorKey));
    }

    /**
     * Retrieves a descriptor from the workflow definition for this issue's current state.
     */
    private static StepDescriptor getStepDescriptorForIssue(final GenericValue issue) throws WorkflowException
    {
        if (!"Issue".equals(issue.getEntityName()))
        {
            throw new IllegalArgumentException("Cannot get step descriptor for non-issue (" + issue + ")");
        }
        final JiraWorkflow issueWorkflow = getWorkflowManager().getWorkflow(issue);
        return issueWorkflow.getLinkedStep(ComponentAccessor.getConstantsManager().getStatus(issue.getString("status")));
    }

    /**
     * JRA-4429 (prevent invalid characters)
     */
    public static void checkInvalidCharacters(final String fieldValue, final String fieldName, final ErrorCollection errorCollection)
    {
        if (fieldValue.indexOf('<') != -1)
        {
            errorCollection.addError(fieldName, getI18nBean().getText("admin.errors.invalid.character", "'<'"));
            errorCollection.addReason(ErrorCollection.Reason.VALIDATION_FAILED);
        }

        if (fieldValue.indexOf('&') != -1)
        {
            errorCollection.addError(fieldName, getI18nBean().getText("admin.errors.invalid.character", "'&'"));
            errorCollection.addReason(ErrorCollection.Reason.VALIDATION_FAILED);
        }

        // JRA-5733 - '"' is also invalid
        if (fieldValue.indexOf('"') != -1)
        {
            errorCollection.addError(fieldName, getI18nBean().getText("admin.errors.invalid.character", "'\"'"));
            errorCollection.addReason(ErrorCollection.Reason.VALIDATION_FAILED);
        }
    }

    /**
     * Return true if the passed string is a reserved workflow property key. Reserved keys are those that can't be
     * changed by the user and can only be used internally by JIRA. The UI tries to hide these keys from the user.
     *
     * @param key the key to check.
     *
     * @return {@code true} if the passed key is reserved or {@code false} otherwise.
     */
    public static boolean isReservedKey(final String key)
    {
        final String normalisedKey = stripToNull(key);

        if (normalisedKey == null)
        {
            return false;
        }
        for (int i = 0; i < JiraWorkflow.JIRA_META_ATTRIBUTE_ALLOWED_LIST.length; i++)
        {
            // Check if our meta attribute starts with one of the allowed prefixes, eg. 'jira.permission'
            String allowedPrefix = JiraWorkflow.JIRA_META_ATTRIBUTE_ALLOWED_LIST[i];
            if (normalisedKey.startsWith(allowedPrefix))
            {
                return false;
            }
        }
        return normalisedKey.startsWith(JiraWorkflow.JIRA_META_ATTRIBUTE_KEY_PREFIX);
    }


    /**
     * Converts a string representation of a workflow XML into the {@link com.opensymphony.workflow.loader.WorkflowDescriptor}
     * object representation.
     *
     * @param workflowDescriptorXML the XML representation of an OSWorkflow
     * @return the {@link com.opensymphony.workflow.loader.WorkflowDescriptor} that represents the workflow.
     * @throws FactoryException thrown if the XML is malformed or can not be converted to the object representation.
     */
    public static WorkflowDescriptor convertXMLtoWorkflowDescriptor(final String workflowDescriptorXML) throws FactoryException
    {
        if (org.apache.commons.lang.StringUtils.isEmpty(workflowDescriptorXML))
        {
            throw new FactoryException("Error: workflow descriptor XML can not be null.");
        }

        InputStream is = null;
        try
        {
            is = new ByteArrayInputStream(workflowDescriptorXML.getBytes("UTF-8"));
            // The descriptor XML has encoding hard-coded to UTF-8, so convert the descriptor to UTF-8 bytes
            return WorkflowLoader.load(is, true);
        }
        catch (final Exception e)
        {
            throw new FactoryException("Error converting XML to workflow descriptor.", e);
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (final IOException e)
                {
                    log.warn("Error closing stream, while converting XML to workflow descriptor.", e);
                }
            }
        }
    }

    /**
     * Appends "(Draft)" to the end of the workflow name for an draft workflow.
     *
     * @param workflow The workflow to create the display name for.
     * @return A String with the workflow name plus an optional (Draft).
     */
    public static String getWorkflowDisplayName(final JiraWorkflow workflow)
    {
        if (workflow == null)
        {
            return null;
        }

        if (workflow.isDraftWorkflow())
        {
            return workflow.getName() + " (" + getI18nBean().getText("common.words.draft") + ")";
        }
        return workflow.getName();
    }

    /**
     * Creates a name to be used for a copy of a given workflow.
     *
     * @param currentName The name of the current workflow.
     * @return A name for the copy of the current workflow.
     *
     * @deprecated Since 5.1. Use {@link com.atlassian.jira.workflow.names.WorkflowCopyNameFactory} instead.
     */
    @Deprecated
    public static String cloneWorkflowName(final String currentName)
    {
        return getWorkflowCopyNameFactory().createFrom(currentName, getAuthenticationContext().getLocale());
    }

    private static JiraAuthenticationContext getAuthenticationContext()
    {
        return ComponentAccessor.getJiraAuthenticationContext();
    }

    private static WorkflowCopyNameFactory getWorkflowCopyNameFactory()
    {
        return ComponentAccessor.getComponent(WorkflowCopyNameFactory.class);
    }

    public static WorkflowManager getWorkflowManager()
    {
        return ComponentAccessor.getComponentOfType(WorkflowManager.class);
    }

    private static I18nHelper getI18nBean()
    {
        return getAuthenticationContext().getI18nHelper();
    }

    /**
     * Get the translated display name of a workflow transition.
     *
     * @param descriptor The action descriptor to get the name of
     * @return The name of the transition.
     */
    public static String getWorkflowTransitionDisplayName(final ActionDescriptor descriptor)
    {
        if(descriptor == null)
        {
            return getI18nBean().getText("common.words.unknown");
        }
        final Map<String, Object> metadata = descriptor.getMetaAttributes();
        if (metadata.containsKey(JiraWorkflow.JIRA_META_ATTRIBUTE_I18N))
        {
            final String key = (String) metadata.get(JiraWorkflow.JIRA_META_ATTRIBUTE_I18N);
            final String value = getI18nBean().getText(key);
            if ((value != null) && !"".equals(value.trim()) && !value.trim().equals(key.trim()))
            {
                return value;
            }
        }
        return descriptor.getName();
    }

    /**
     * Get the translated description of the workflow transition.
     *
     * @param descriptor The action descriptor to get the description of
     * @return the translated description of the workflow transition.
     */
    public static String getWorkflowTransitionDescription(final ActionDescriptor descriptor)
    {
        return (String) descriptor.getMetaAttributes().get("jira.description");
    }

    /**
     * Given a map of transientVars from a Workflow Function, returns the username of the caller.
     *
     * @param transientVars the "transientVars" from the workflow FunctionProvider
     * @return the username of the caller (can be null for anonymous).
     *
     * @since 4.4
     *
     * @see com.opensymphony.workflow.FunctionProvider#execute(java.util.Map, java.util.Map, com.opensymphony.module.propertyset.PropertySet)
     * @see com.opensymphony.workflow.WorkflowContext#getCaller()
     * @see #getCaller(java.util.Map)
     * @deprecated Use {@link #getCallerUser(java.util.Map)} instead. Since v6.0.
     */
    public static String getCallerName(Map transientVars)
    {
        ApplicationUser appUser = getCallerUser(transientVars);
        return appUser != null ? appUser.getUsername() : null;
    }


    /**
     * Given a map of transientVars from a Workflow Function, returns the user's key of the caller. It is highly
     * discouraged to use this method directly when fetching user object. Please use {@link
     * #getCallerUser(java.util.Map)} instead.
     *
     * @param transientVars the "transientVars" from the workflow FunctionProvider
     * @return the userKey of the caller (can be null for anonymous).
     * @see WorkflowUtil#getCallerUser(java.util.Map)
     * @since 6.0
     */
    public static String getCallerKey(Map transientVars)
    {
        if(transientVars == null){
            return null;
        }
        WorkflowContext context = (WorkflowContext) transientVars.get("context");
        if(context == null){
            return null;
        }
        return context.getCaller();
    }

    /**
     * Given a map of transientVars from a Workflow Function, returns the {@link ApplicationUser} object of the caller.
     *
     * @param transientVars the "transientVars" from the workflow FunctionProvider
     * @return the username of the caller (can be null for anonymous).
     * @since 6.0
     */
    public static ApplicationUser getCallerUser(Map transientVars)
    {
        String userKey = getCallerKey(transientVars);
        return ApplicationUsers.byKey(userKey);
    }

    /**
     * Given a map of transientVars from a Workflow Function, returns the {@link User} object of the caller.
     *
     * @param transientVars the "transientVars" from the workflow FunctionProvider
     * @return the username of the caller (can be null for anonymous).
     * @see com.opensymphony.workflow.FunctionProvider#execute(java.util.Map, java.util.Map,
     *      com.opensymphony.module.propertyset.PropertySet)
     * @see com.opensymphony.workflow.WorkflowContext#getCaller()
     * @see #getCallerUser(java.util.Map)
     * @since 4.4
     * @deprecated Use {@link #getCallerUser(java.util.Map)} instead. Since v6.0.
     */
    public static User getCaller(Map transientVars)
    {
        ApplicationUser appUser = getCallerUser(transientVars);
        return appUser != null ? appUser.getDirectoryUser() : null;
    }
}