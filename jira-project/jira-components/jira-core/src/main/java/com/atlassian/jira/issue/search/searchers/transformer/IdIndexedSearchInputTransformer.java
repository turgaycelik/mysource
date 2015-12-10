package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.util.DefaultIndexedInputHelper;
import com.atlassian.jira.issue.search.searchers.util.IndexedInputHelper;
import com.atlassian.jira.issue.transport.ActionParams;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.NonInjectableComponent;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A SearchInputTransformer that handles any field that is represented by its id in the Lucene document, and hence
 * is represented by a select list in the Issue Navigator. May or may not also have flag functions.
 *
 * @since v4.0
 */
@NonInjectableComponent
public abstract class IdIndexedSearchInputTransformer<T> implements SearchInputTransformer
{
    private final ClauseNames clauseNames;
    private final String urlParameterName;
    protected final FieldFlagOperandRegistry fieldFlagOperandRegistry;
    protected final JqlOperandResolver operandResolver;
    protected final IndexInfoResolver<T> indexInfoResolver;
    private final NavigatorStructureChecker navigatorStructureChecker;
    private volatile IndexedInputHelper indexedInputHelper;
    private volatile DefaultIndexedInputHelper defaultIndexedInputHelper;

    // This constructor assumes that clauseNames.getPrimaryName is the same as the urlParameterName, if they differ
    // do not use this constructor.
    public IdIndexedSearchInputTransformer(ClauseNames id, IndexInfoResolver<T> indexInfoResolver, JqlOperandResolver operandResolver,
            FieldFlagOperandRegistry fieldFlagOperandRegistry)
    {
        this(id, id.getPrimaryName(), indexInfoResolver, operandResolver, fieldFlagOperandRegistry);
    }

    public IdIndexedSearchInputTransformer(ClauseNames clauseNames, String urlParameterName, IndexInfoResolver<T> indexInfoResolver,
            JqlOperandResolver operandResolver, FieldFlagOperandRegistry fieldFlagOperandRegistry)
    {
        this.clauseNames = clauseNames;
        this.urlParameterName = urlParameterName;
        this.fieldFlagOperandRegistry = fieldFlagOperandRegistry;
        this.operandResolver = operandResolver;
        this.indexInfoResolver = indexInfoResolver;

        this.navigatorStructureChecker = createNavigatorStructureChecker();
    }

    IdIndexedSearchInputTransformer(ClauseNames clauseNames, String urlParameterName, IndexInfoResolver<T> indexInfoResolver,
            JqlOperandResolver operandResolver, FieldFlagOperandRegistry fieldFlagOperandRegistry,
            final NavigatorStructureChecker navigatorStructureChecker)
    {
        this.clauseNames = clauseNames;
        this.urlParameterName = urlParameterName;
        this.fieldFlagOperandRegistry = fieldFlagOperandRegistry;
        this.operandResolver = operandResolver;
        this.indexInfoResolver = indexInfoResolver;

        this.navigatorStructureChecker = navigatorStructureChecker;
    }

    public void populateFromParams(final User user, final FieldValuesHolder fieldValuesHolder, final ActionParams actionParams)
    {
        fieldValuesHolder.put(urlParameterName, actionParams.getValuesForKey(urlParameterName));
    }

    ///CLOVER:OFF
    public void validateParams(final User user, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final I18nHelper i18nHelper, final ErrorCollection errors)
    {
        // Currently doesn't do nudda (data entered through select lists)
    }
    ///CLOVER:ON

    public void populateFromQuery(final User user, final FieldValuesHolder fieldValuesHolder, final Query query, final SearchContext searchContext)
    {
        final Collection<String> valuesAsStrings = getIndexedInputHelper().getAllNavigatorValuesForMatchingClauses(user, clauseNames, query);
        fieldValuesHolder.put(urlParameterName, valuesAsStrings);
    }

    public boolean doRelevantClausesFitFilterForm(final User user, final Query query, final SearchContext searchContext)
    {
        return navigatorStructureChecker.checkSearchRequest(query);
    }

    /**
     * For this implementation we expect that the fieldValuesHolder will contain a list of
     * strings or nothing at all, if not then this will throw an exception.
     *
     * @param user the user performing the search
     * @param fieldValuesHolder contains values populated by the searchers
     * @return Clause that represents the raw values.
     * @throws IllegalArgumentException if the value in the field values holder keyed by the
     * searcher id is not a list that contains strings.
     */
    public Clause getSearchClause(final User user, final FieldValuesHolder fieldValuesHolder)
    {
        Set<String> constants = getValuesFromHolder(fieldValuesHolder);
        if (constants != null && constants.size() > 0)
        {
            return getClauseForValues(constants);
        }
        return null;
    }

    ///CLOVER:OFF
    NavigatorStructureChecker<T> createNavigatorStructureChecker()
    {
        return new NavigatorStructureChecker<T>(clauseNames, true, fieldFlagOperandRegistry, operandResolver);
    }
    ///CLOVER:ON

    /**
     * @return the {@link com.atlassian.jira.issue.search.searchers.util.IndexedInputHelper} which might be specialised for this particular searcher
     */
    abstract IndexedInputHelper createIndexedInputHelper();

    Clause getClauseForValues(Set<String> values)
    {
        // We only want to generate clauses with the name being the primary name
        return getIndexedInputHelper().getClauseForNavigatorValues(clauseNames.getPrimaryName(), values);
    }

    /**
     * We should never be populating the FieldValuesHolder with anything but Strings for the searchers that use this
     * InputTransformer, so we've added a runtime check to make sure that assumption is correct.
     *
     * @param fieldValuesHolder the field values holder
     * @return the values for this searcher as a list of Strings. Could be null.
     */
    Set<String> getValuesFromHolder(final FieldValuesHolder fieldValuesHolder)
    {
        final List<?> list = (List<?>) fieldValuesHolder.get(urlParameterName);
        if (list == null)
        {
            return null;
        }

        for (Object o : list)
        {
            if (!(o instanceof String))
            {
                throw new IllegalArgumentException("Why are we putting non-String values in the FieldValuesHolder for searcher '" + urlParameterName + "'???");
            }
        }

        // We have checked every element in the list is of type String, so it is safe to cast
        @SuppressWarnings ({ "unchecked" }) final Set<String> strings = new LinkedHashSet<String>((List<String>)list);
        return strings;
    }

    /**
     * @return the {@link com.atlassian.jira.issue.search.searchers.util.DefaultIndexedInputHelper} always
     */
    protected DefaultIndexedInputHelper getDefaultIndexedInputHelper()
    {
        if (defaultIndexedInputHelper == null)
        {
            defaultIndexedInputHelper = new DefaultIndexedInputHelper<T>(indexInfoResolver, operandResolver, fieldFlagOperandRegistry);
        }
        return defaultIndexedInputHelper;
    }

    /**
     * @return the {@link com.atlassian.jira.issue.search.searchers.util.IndexedInputHelper} which might be specialised for this particular searcher
     */
    protected IndexedInputHelper getIndexedInputHelper()
    {
        if (indexedInputHelper == null)
        {
            this.indexedInputHelper = createIndexedInputHelper();
        }
        return indexedInputHelper;
    }
}
