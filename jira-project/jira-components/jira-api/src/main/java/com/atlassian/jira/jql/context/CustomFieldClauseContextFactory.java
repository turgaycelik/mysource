package com.atlassian.jira.jql.context;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.query.clause.TerminalClause;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Generates the ClauseContext for a custom field. This takes into account what context the custom field
 * has been configured against, whether the custom field is visible in the field configuration scheme,
 * and if the user has permission to see the project that the field has been configured against.
 *
 * @since v4.0
 */
public class CustomFieldClauseContextFactory implements ClauseContextFactory
{
    private final CustomField customField;
    private final FieldConfigSchemeClauseContextUtil fieldConfigSchemeClauseContextUtil;
    private final ContextSetUtil contextSetUtil;

    public CustomFieldClauseContextFactory(final CustomField customField, FieldConfigSchemeClauseContextUtil fieldConfigSchemeClauseContextUtil, final ContextSetUtil contextSetUtil)
    {
        this.customField = customField;
        this.fieldConfigSchemeClauseContextUtil = fieldConfigSchemeClauseContextUtil;
        this.contextSetUtil = contextSetUtil;
    }

    public ClauseContext getClauseContext(final User searcher, final TerminalClause terminalClause)
    {
        final List<FieldConfigScheme> fieldConfigSchemes = customField.getConfigurationSchemes();
        final Set<ClauseContext> ctxs = new HashSet<ClauseContext>();

        // Lets run through each configured Custom Field configuration scheme
        boolean globalContextSeen = false;
        for (FieldConfigScheme fieldConfigScheme : fieldConfigSchemes)
        {
            final ClauseContext configContext = fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(searcher, fieldConfigScheme);

            // The contexts for the FieldConfigScheme will never be explicit, so all we can hope to get from the above
            // call is Implicit contexts or the Global context. If we get the global context, then we don't really care 
            // about implicit contexts, as they aren't really more specific, and adversely affect the creation of the
            // SearchContext later on
            if (configContext.containsGlobalContext())
            {
                globalContextSeen = true;
                break;
            }

            ctxs.add(configContext);
        }

        if (globalContextSeen || ctxs.isEmpty())
        {
            return ClauseContextImpl.createGlobalClauseContext();
        }
        else
        {
            final ClauseContext returnContext;
            if (ctxs.size() == 1)
            {
                returnContext = ctxs.iterator().next();
            }
            else
            {
                // We always union the contexts together, this is so that we will get the most specific context possible.
                // We want the global context to be removed if there is a more specific context.
                returnContext =  contextSetUtil.union(ctxs);            
            }

            return returnContext.getContexts().isEmpty() ? ClauseContextImpl.createGlobalClauseContext() : returnContext;
        }
    }
}
