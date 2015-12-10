package com.atlassian.jira.web.action.admin.translation;

import com.atlassian.jira.bulkedit.operation.BulkMoveOperation;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.option.OptionSetManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.web.action.admin.issuetypes.AbstractManageIssueTypeOptionsAction;
import com.atlassian.jira.web.action.admin.issuetypes.IssueTypeManageableOption;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

@WebSudoRequired
public class ViewTranslations extends AbstractManageIssueTypeOptionsAction
{
    public static final String ISSUECONSTANT_ISSUETYPE = IssueFieldConstants.ISSUE_TYPE;
    public static final String ISSUECONSTANT_PRIORITY = "priority";
    public static final String ISSUECONSTANT_RESOLUTION = "resolution";
    public static final String ISSUECONSTANT_STATUS = "status";
    public static final String ISSUECONSTANT_SUBTASK = "subTask";

    public static final String TRANSLATION_PREFIX = "jira.translation.";

    public static final String STATUS_CONSTANT_NAME = "Status";
    public static final String ISSUETYPE_CONSTANT_NAME = "Issue Type";
    public static final String RESOLUTION_CONSTANT_NAME = "Resolution";
    public static final String SUBTASK_CONSTANT_NAME = "Sub-Task";
    public static final String PRIORITY_CONSTANT_NAME = "Priority";

    private final TranslationManager translationManager;
    private final ConstantsManager constantsManager;
    private final JiraAuthenticationContext authenticationContext;
    private final LocaleManager localeManager;

    private String issueConstantType;
    private String issueConstantName;
    private String issueConstantTranslationPrefix;
    private String redirectPage;
    private String linkName;
    private String selectedLocale;

    private Map params = new HashMap();

    private Collection<IssueConstant> issueConstants;

    public static final String LINKNAME_ISSUETYPE = "issue types";
    public static final String LINKNAME_PRIORITY = "priorities";
    public static final String LINKNAME_RESOLUTION = "resolutions";
    public static final String LINKNAME_STATUS = "statuses";
    public static final String LINKNAME_SUBTASK = "sub-tasks";

    public ViewTranslations(FieldConfigSchemeManager configSchemeManager, IssueTypeSchemeManager issueTypeSchemeManager,
            FieldManager fieldManager, OptionSetManager optionSetManager, IssueTypeManageableOption manageableOptionType,
            BulkMoveOperation bulkMoveOperation, SearchProvider searchProvider, TranslationManager translationManager,
            ConstantsManager constantsManager, JiraAuthenticationContext authenticationContext,
            IssueManager issueManager, LocaleManager localeManager)
    {
        super(configSchemeManager, issueTypeSchemeManager, fieldManager, optionSetManager, manageableOptionType,
                bulkMoveOperation, searchProvider, issueManager);
        this.translationManager = translationManager;
        this.constantsManager = constantsManager;
        this.authenticationContext = authenticationContext;
        this.localeManager = localeManager;
    }

    public String doDefault()
    {
        // Select the current locale when this page first viewed
        if (getSelectedLocale() == null)
        {
            // Select the current (user pref) locale if it is one of the installed locales
            String currentLocale = getUserPreferences().getString(PreferenceKeys.USER_LOCALE);
            if (currentLocale == null)
            {
                currentLocale = getCurrentLocale();
            }

            if (getInstalledLocales().containsKey(currentLocale))
            {
                setSelectedLocale(currentLocale);
            }
            else
            {
                String localeString;
                if(!getInstalledLocales().isEmpty())
                {
                    // Otherwise - select the first locale from the installed locales (user has not yet selected a locale in the preferences)
                    localeString = (String) getInstalledLocales().keySet().iterator().next();
                }
                else
                {
                    //if there's no installed locales (JRA-16912) use the default locale! Not really necessary since
                    //no 'Translate' links should be shown anyway, but just in case a user navigates to this page
                    //by URL.
                    localeString = Locale.getDefault().toString();
                }

                setSelectedLocale(localeString);
            }
        }
        if (getErrorMessages().size() == 0)
        {
            initParamsMap();
        }
        return SUCCESS;
    }

    public void doValidation()
    {
        // Check that a name and description translation pair has been provided

        Map parameters = ActionContext.getParameters();

        // move the params into our param map
        params = parameters;

        // Extract the translations from the parameters
        for (IssueConstant issueConstant : getIssueConstants())
        {
            String issueConstantNameKey = TRANSLATION_PREFIX + getIssueConstantName() + "." + issueConstant.getId() + ".name";
            String issueConstantDescriptionKey = TRANSLATION_PREFIX + getIssueConstantName() + "." + issueConstant.getId() + ".desc";
            if (parameters.containsKey(issueConstantNameKey) && parameters.containsKey(issueConstantDescriptionKey))
            {
                String[] values = (String[]) parameters.get(issueConstantNameKey);
                String nameTranslation = values[0];
                values = (String[]) parameters.get(issueConstantDescriptionKey);
                String descTranslation = values[0];
                if (TextUtils.stringSet(nameTranslation) && !TextUtils.stringSet(descTranslation) || (!TextUtils.stringSet(nameTranslation) && TextUtils.stringSet(descTranslation)))
                {
                    addError(getNameKey(issueConstant), getText("admin.errors.translation.name.description.pair.must.be.provided"));
                    log.error("A name/description translation pair must be provided.");
                }
            }
        }
    }

    private void initParamsMap()
    {
        params.clear();
        for (IssueConstant issueConstant : getIssueConstants())
        {
            final String issueConstantNameKey = getNameKey(issueConstant);
            final String issueConstantDescriptionKey = getDescKey(issueConstant);
            if (hasTranslatedValue(issueConstant))
            {
                params.put(issueConstantNameKey, issueConstant.getNameTranslation(getSelectedLocale()));
                params.put(issueConstantDescriptionKey, issueConstant.getDescTranslation(getSelectedLocale()));
            }
        }
    }

    @RequiresXsrfCheck
    protected String doExecute()
    {
        Map parameters = ActionContext.getParameters();

        // move the params into our param map
        params = parameters;

        // Extract the translations from the parameters

        for (IssueConstant issueConstant : getIssueConstants())
        {
            // Create key to search parameters
            String issueConstantNameKey = getNameKey(issueConstant);
            String issueConstantDescriptionKey = getDescKey(issueConstant);
            if (parameters.containsKey(issueConstantNameKey) && parameters.containsKey(issueConstantDescriptionKey))
            {
                String[] values = (String[]) parameters.get(issueConstantNameKey);
                String nameTranslation = values[0];
                values = (String[]) parameters.get(issueConstantDescriptionKey);
                String descTranslation = values[0];
                Locale locale = localeManager.getLocale(getSelectedLocale());
                if (TextUtils.stringSet(nameTranslation) && TextUtils.stringSet(descTranslation))
                {
                    translationManager.setIssueConstantTranslation(issueConstant, getIssueConstantTranslationPrefix(), locale, nameTranslation, descTranslation);
                }
                else
                {
                    translationManager.deleteIssueConstantTranslation(issueConstant, getIssueConstantTranslationPrefix(), locale);
                }
            }
        }

        return getRedirect("ViewTranslations!default.jspa?issueConstantType=" + getIssueConstantType() + "&selectedLocale=" + getSelectedLocale());
    }

    public String getIssueConstantName()
    {
        if (issueConstantName == null)
        {
            if (ISSUECONSTANT_ISSUETYPE.equals(getIssueConstantType()))
            {
                issueConstantName = getText("admin.issue.constant.issuetype");
            }
            else if (ISSUECONSTANT_PRIORITY.equals(getIssueConstantType()))
            {
                issueConstantName = getText("admin.issue.constant.priority");
            }
            else if (ISSUECONSTANT_RESOLUTION.equals(getIssueConstantType()))
            {
                issueConstantName = getText("admin.issue.constant.resolution");
            }
            else if (ISSUECONSTANT_STATUS.equals(getIssueConstantType()))
            {
                issueConstantName = getText("admin.issue.constant.status");
            }
            else if (ISSUECONSTANT_SUBTASK.equals(getIssueConstantType()))
            {
                issueConstantName = getText("admin.issue.constant.subtask");
            }
        }

        return issueConstantName;
    }

    public String getIssueConstantTranslationPrefix()
    {
        if (issueConstantTranslationPrefix == null)
        {
            if (ISSUECONSTANT_ISSUETYPE.equals(getIssueConstantType()))
            {
                issueConstantTranslationPrefix = TranslationManagerImpl.JIRA_ISSUETYPE_TRANSLATION_PREFIX;
            }
            else if (ISSUECONSTANT_PRIORITY.equals(getIssueConstantType()))
            {
                issueConstantTranslationPrefix = TranslationManagerImpl.JIRA_PRIORITY_TRANSLATION_PREFIX;
            }
            else if (ISSUECONSTANT_RESOLUTION.equals(getIssueConstantType()))
            {
                issueConstantTranslationPrefix = TranslationManagerImpl.JIRA_RESOLUTION_TRANSLATION_PREFIX;
            }
            else if (ISSUECONSTANT_STATUS.equals(getIssueConstantType()))
            {
                issueConstantTranslationPrefix = TranslationManagerImpl.JIRA_STATUS_TRANSLATION_PREFIX;
            }
            else if (ISSUECONSTANT_SUBTASK.equals(getIssueConstantType()))
            {
                issueConstantTranslationPrefix = TranslationManagerImpl.JIRA_ISSUETYPE_TRANSLATION_PREFIX;
            }
        }

        return issueConstantTranslationPrefix;
    }

    public String getRedirectPage()
    {
        if (redirectPage == null)
        {
            if (ISSUECONSTANT_ISSUETYPE.equals(getIssueConstantType()))
            {
                redirectPage = "ViewIssueTypes.jspa";
            }
            else if (ISSUECONSTANT_PRIORITY.equals(getIssueConstantType()))
            {
                redirectPage = "ViewPriorities.jspa";
            }
            else if (ISSUECONSTANT_RESOLUTION.equals(getIssueConstantType()))
            {
                redirectPage = "ViewResolutions.jspa";
            }
            else if (ISSUECONSTANT_STATUS.equals(getIssueConstantType()))
            {
                redirectPage = "ViewStatuses.jspa";
            }
            else if (ISSUECONSTANT_SUBTASK.equals(getIssueConstantType()))
            {
                redirectPage = "ManageSubTasks.jspa";
            }
        }
        return redirectPage;
    }

    public Collection<IssueConstant> getIssueConstants()
    {
        if (issueConstants == null)
        {
            issueConstants = newArrayList();
            if (ISSUECONSTANT_ISSUETYPE.equals(getIssueConstantType()))
            {
                for (GenericValue issueTypeGV : constantsManager.getIssueTypes())
                {
                    IssueType issueType = constantsManager.getIssueTypeObject(issueTypeGV.getString("id"));
                    issueConstants.add(issueType);
                }
            }
            else if (ISSUECONSTANT_PRIORITY.equals(getIssueConstantType()))
            {
                for (Priority priority : constantsManager.getPriorityObjects())
                {
                    issueConstants.add(priority);
                }
            }
            else if (ISSUECONSTANT_RESOLUTION.equals(getIssueConstantType()))
            {
                for (GenericValue resolutionGV : constantsManager.getResolutions())
                {
                    Resolution resolution = constantsManager.getResolutionObject(resolutionGV.getString("id"));
                    issueConstants.add(resolution);
                }
            }
            else if (ISSUECONSTANT_STATUS.equals(getIssueConstantType()))
            {
                for (GenericValue statusGV : constantsManager.getStatuses())
                {
                    Status status = constantsManager.getStatusObject(statusGV.getString("id"));
                    issueConstants.add(status);
                }
            }
            else if (ISSUECONSTANT_SUBTASK.equals(getIssueConstantType()))
            {
                for (GenericValue issueTypeGV : constantsManager.getSubTaskIssueTypes())
                {
                    IssueType issueType = constantsManager.getIssueTypeObject(issueTypeGV.getString("id"));
                    issueConstants.add(issueType);
                }
            }
        }
        return issueConstants;
    }

    public String getLinkName()
    {
        if (linkName == null)
        {
            if (ISSUECONSTANT_ISSUETYPE.equals(getIssueConstantType()))
            {
                linkName = LINKNAME_ISSUETYPE;
            }
            else if (ISSUECONSTANT_PRIORITY.equals(getIssueConstantType()))
            {
                linkName = LINKNAME_PRIORITY;
            }
            else if (ISSUECONSTANT_RESOLUTION.equals(getIssueConstantType()))
            {
                linkName = LINKNAME_RESOLUTION;
            }
            else if (ISSUECONSTANT_STATUS.equals(getIssueConstantType()))
            {
                linkName = LINKNAME_STATUS;
            }
            else if (ISSUECONSTANT_SUBTASK.equals(getIssueConstantType()))
            {
                linkName = LINKNAME_SUBTASK;
            }
        }

        return linkName;
    }

    public String getCurrentLocale()
    {
        return authenticationContext.getLocale().toString();
    }

    /**
     * Returns a name for the selected locale in the user's language for display purposes.
     */
    public String getSelectedLocaleDisplayName()
    {
        final Locale selectedLocale = localeManager.getLocale(getSelectedLocale());
        return selectedLocale.getDisplayName(authenticationContext.getLocale());
    }

    public String getNameKey(IssueConstant issueConstant)
    {
        String fieldName = TRANSLATION_PREFIX + getIssueConstantName() + "." + issueConstant.getId() + ".name";
        return fieldName;
    }

    public String getDescKey(IssueConstant issueConstant)
    {
        String fieldName = TRANSLATION_PREFIX + getIssueConstantName() + "." + issueConstant.getId() + ".desc";
        return fieldName;
    }

    public String getTranslatedName(IssueConstant issueConstant)
    {
        Object param = params.get(getNameKey(issueConstant));
        if (param instanceof String[])
        {
            return ((String[])param)[0];
        }
        else if (param instanceof String)
        {
            return (String) param;
        }
        else
        {
            return null;
        }
    }

    public String getTranslatedDesc(IssueConstant issueConstant)
    {
        Object param = params.get(getDescKey(issueConstant));
        if (param instanceof String[])
        {
            return ((String[])param)[0];
        }
        else if (param instanceof String)
        {
            return (String) param;
        }
        else
        {
            return null;
        }
    }

    private boolean hasTranslatedValue(IssueConstant issueConstant)
    {
        return translationManager.hasLocaleTranslation(issueConstant, getSelectedLocale());
    }

    // Retrieve the installed locales
    public Map getInstalledLocales()
    {
        return translationManager.getInstalledLocales();
    }

    public String getIssueConstantType()
    {
        return issueConstantType;
    }

    public void setIssueConstantType(String issueConstantType)
    {
        this.issueConstantType = issueConstantType;
    }

    public boolean isIssueConstantTypeStatus()
    {
        return ISSUECONSTANT_STATUS.equals(getIssueConstantType());
    }

    public String getSelectedLocale()
    {
        return selectedLocale;
    }

    public void setSelectedLocale(String selectedLocale)
    {
        this.selectedLocale = selectedLocale;
    }

    public String getActionType()
    {
        return "translate";
    }

    public boolean isTranslatable()
    {
        //JRA-16912: Only show the 'Translate' link if there's any installed languages to translate to!
        return !translationManager.getInstalledLocales().isEmpty();
    }
}
