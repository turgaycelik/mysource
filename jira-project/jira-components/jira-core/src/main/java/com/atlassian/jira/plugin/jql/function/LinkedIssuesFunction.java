package com.atlassian.jira.plugin.jql.function;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.link.LinkCollection;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.permission.IssueLiteralSanitiser;
import com.atlassian.jira.jql.permission.LiteralSanitiser;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.util.JqlIssueSupport;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Returns the issue ids of issues linked to the specified issue which the user can see.
 * <p/>
 * This function can only be used if Issue Linking is enabled.
 * <p/>
 * Function usage:
 * <code>linkedIssues ( issuekey [, linkDescription ]* )</code>
 *
 * @since v4.0
 */
public class LinkedIssuesFunction extends AbstractJqlFunction implements ClauseSanitisingJqlFunction
{
    public static final String FUNCTION_LINKED_ISSUES = "linkedIssues";

    private final JqlIssueSupport jqlIssueSupport;
    private final IssueLinkTypeManager issueLinkTypeManager;
    private final IssueLinkManager issueLinkManager;
    private final PermissionManager permissionManager;

    public LinkedIssuesFunction(final JqlIssueSupport jqlIssueSupport, final IssueLinkTypeManager issueLinkTypeManager,
            final IssueLinkManager issueLinkManager, final PermissionManager permissionManager)
    {
        this.jqlIssueSupport = notNull("jqlIssueSupport", jqlIssueSupport);
        this.issueLinkTypeManager = notNull("issueLinkTypeManager", issueLinkTypeManager);
        this.issueLinkManager = notNull("issueLinkManager", issueLinkManager);
        this.permissionManager = notNull("permissionManager", permissionManager);
    }

    public MessageSet validate(final User searcher, final FunctionOperand operand, final TerminalClause terminalClause)
    {
        MessageSet messageSet = new MessageSetImpl();
        if (!issueLinkManager.isLinkingEnabled())
        {
            messageSet.addErrorMessage(getI18n().getText("jira.jql.function.issue.linking.disabled", getFunctionName()));
            return messageSet;
        }

        final List<String> args = operand.getArgs();
        if (args.isEmpty())
        {
            messageSet.addErrorMessage(getI18n().getText("jira.jql.function.linked.issues.incorrect.usage", getFunctionName()));
            return messageSet;
        }

        // check if the issue argument is actually an issue which the user can see
        String issueArg = args.get(0);
        Collection<Issue> issues = getIssuesForArg(issueArg, searcher, false);
        if (issues == null)
        {
            messageSet.addErrorMessage(getI18n().getText("jira.jql.function.linked.issues.issue.not.found", getFunctionName(), issueArg));
            return messageSet;
        }

        // if more arguments were specified, verify that they are valid link descriptions
        if (args.size() > 1)
        {
            for (int i = 1; i < args.size(); i++)
            {
                String linkDescArg = args.get(i);
                final Map<Direction, Collection<IssueLinkType>> linkTypeMapping = getIssueLinkTypesForArg(linkDescArg);
                if (linkTypeMapping == null)
                {
                    messageSet.addErrorMessage(getI18n().getText("jira.jql.function.linked.issues.link.type.not.found", getFunctionName(), linkDescArg));
                    return messageSet;
                }
            }
        }

        return messageSet;
    }

    public List<QueryLiteral> getValues(final QueryCreationContext queryCreationContext, final FunctionOperand operand, final TerminalClause terminalClause)
    {
        notNull("queryCreationContext", queryCreationContext);
        final List<QueryLiteral> literals = new LinkedList<QueryLiteral>();
        if (!issueLinkManager.isLinkingEnabled())
        {
            return literals;
        }

        final List<String> args = operand.getArgs();
        if (args.isEmpty())
        {
            return literals;
        }

        final String issueArg = args.get(0);
        final Collection<Issue> rootIssues = getIssuesForArg(issueArg, queryCreationContext.getQueryUser(), queryCreationContext.isSecurityOverriden());

        // cant have no issues
        if (rootIssues == null)
        {
            return literals;
        }

        Map<Direction, Collection<IssueLinkType>> linkTypeMapping = null;
        if (args.size() > 1)
        {
            linkTypeMapping = getIssueLinkTypesForArgs(args.subList(1, args.size()));
            if (linkTypeMapping == null)
            {
                return literals;
            }
        }

        final Collection<Issue> linkedIssues = getLinkedIssues(queryCreationContext.getQueryUser(), queryCreationContext.isSecurityOverriden(), rootIssues, linkTypeMapping);
        for (Issue issue : linkedIssues)
        {
            literals.add(new QueryLiteral(operand, issue.getId()));
        }

        return literals;
    }

    public int getMinimumNumberOfExpectedArguments()
    {
        return 1;
    }

    public JiraDataType getDataType()
    {
        return JiraDataTypes.ISSUE;
    }

    public FunctionOperand sanitiseOperand(final User searcher, final FunctionOperand operand)
    {
        // if the function has no args, just return it as is
        final List<String> args = operand.getArgs();
        if (args.isEmpty())
        {
            return operand;
        }

        // Note: since arguments are currently only represented by strings, we need to convert to QueryLiterals for
        // sanitising. But since the args are strings, its not possible to get the EMPTY literal as an argument - keep
        // this in mind for later.
        QueryLiteral literal = new QueryLiteral(operand, args.get(0));

        final LiteralSanitiser.Result result = createLiteralSanitiser(searcher).sanitiseLiterals(Collections.singletonList(literal));
        if (!result.isModified())
        {
            return operand;
        }
        else
        {
            // Note: as above, we know here that a non-empty literal would never be sanitised into the empty literal
            // so we don't need to worry about the .asString() call below.
            List<String> sanitisedArgs = CollectionUtil.transform(result.getLiterals(), new Function<QueryLiteral, String>()
            {
                public String get(final QueryLiteral input)
                {
                    return input.asString();
                }
            });

            // if sanitising caused the input argument to expand, we can't afford to accommodate for all the results
            // because we can't create a new clause. So, just pick the first one and add it on the front of the rest of
            // the original args
            List<String> newArgs = new ArrayList<String>();
            newArgs.add(sanitisedArgs.get(0));
            if (args.size() > 1)
            {
                newArgs.addAll(args.subList(1, args.size()));
            }
            return new FunctionOperand(operand.getName(), newArgs);
        }
    }

    ///CLOVER:OFF
    LiteralSanitiser createLiteralSanitiser(final User user)
    {
        return new IssueLiteralSanitiser(permissionManager, jqlIssueSupport, user);
    }
    ///CLOVER:ON

    /**
     * @param issueArg the string representing issues, either as an issue key or issue id
     * @param searcher the user performing the search
     * @param overrideSecurity false if permissions should be checked when looking up the issue
     * @return the issues represented by the argument which the user can see; null if none were found.
     */
    Collection<Issue> getIssuesForArg(final String issueArg, final User searcher, final boolean overrideSecurity)
    {
        final Issue issueByKey = overrideSecurity ? jqlIssueSupport.getIssue(issueArg) : jqlIssueSupport.getIssue(issueArg, ApplicationUsers.from(searcher));
        if (issueByKey != null)
        {
            return Collections.singleton(issueByKey);
        }
        else
        {
            Long issueId = getLong(issueArg);
            if (issueId != null)
            {
                final Issue issueById = overrideSecurity ? jqlIssueSupport.getIssue(issueId) : jqlIssueSupport.getIssue(issueId, searcher);
                if (issueById != null)
                {
                    return Collections.singleton(issueById);
                }
            }
        }

        return null;
    }

    /**
     * @param linkDescArg the string which represents either the inward or outward description for a link type
     * @return the matching IssueLinkTypes found or null, but never empty
     */
    Map<Direction, Collection<IssueLinkType>> getIssueLinkTypesForArg(final String linkDescArg)
    {
        final Map<Direction, Collection<IssueLinkType>> result = new HashMap<Direction, Collection<IssueLinkType>>();
        final Collection<IssueLinkType> inwardLinkTypes = issueLinkTypeManager.getIssueLinkTypesByInwardDescription(linkDescArg);
        if (!inwardLinkTypes.isEmpty())
        {
            result.put(Direction.IN, inwardLinkTypes);
        }

        final Collection<IssueLinkType> outwardLinkTypes = issueLinkTypeManager.getIssueLinkTypesByOutwardDescription(linkDescArg);
        if (!outwardLinkTypes.isEmpty())
        {
            result.put(Direction.OUT, outwardLinkTypes);
        }

        return result.isEmpty() ? null : result;
    }

    /**
     * @param linkDescArgs the arguments of link descriptions
     * @return an aggregate map of the directions and all the link types found across all arguments; null if any of the
     * arguments did not resolve, but otherwise should never be empty.
     */
    Map<Direction, Collection<IssueLinkType>> getIssueLinkTypesForArgs(final List<String> linkDescArgs)
    {
        final Set<IssueLinkType> inLinkTypes = new LinkedHashSet<IssueLinkType>();
        final Set<IssueLinkType> outLinkTypes = new LinkedHashSet<IssueLinkType>();

        for (String linkDescArg : linkDescArgs)
        {
            final Map<Direction, Collection<IssueLinkType>> linkTypeMapping = getIssueLinkTypesForArg(linkDescArg);
            if (linkTypeMapping != null)
            {
                final Collection<IssueLinkType> ins = linkTypeMapping.get(Direction.IN);
                if (ins != null)
                {
                    inLinkTypes.addAll(ins);
                }

                final Collection<IssueLinkType> outs = linkTypeMapping.get(Direction.OUT);
                if (outs != null)
                {
                    outLinkTypes.addAll(outs);
                }
            }
            else
            {
                // if any of the arguments screw up, then return null as an error
                return null;
            }
        }

        return MapBuilder.<Direction, Collection<IssueLinkType>>newBuilder()
                .add(Direction.IN, inLinkTypes)
                .add(Direction.OUT, outLinkTypes)
                .toMap();
    }

    /**
     * @param searcher the user performing the search
     * @param overrideSecurity false if permissions should be checked when retrieving links
     * @param rootIssues the root issues to search for links from/to
     * @param linkTypeMappings a mapping from directions to link types to search; null if all linked issues should be retrieved
     * @return The linked issues from the root issues, filtered by what the user can see and also by link type and direction
     * if specified. Never null.
     */
    Collection<Issue> getLinkedIssues(final User searcher, final boolean overrideSecurity, final Collection<Issue> rootIssues, final Map<Direction, Collection<IssueLinkType>> linkTypeMappings)
    {
        Set<Issue> linkedIssues = new LinkedHashSet<Issue>();
        Set<LinkCollection> linkCollections = new LinkedHashSet<LinkCollection>();

        for (Issue issue : rootIssues)
        {
            final LinkCollection collection = overrideSecurity ?
                    issueLinkManager.getLinkCollectionOverrideSecurity(issue)
                    : issueLinkManager.getLinkCollection(issue, searcher);
            linkCollections.add(collection);
        }

        for (LinkCollection linkCollection : linkCollections)
        {
            if (linkTypeMappings != null)
            {
                for (Direction direction : linkTypeMappings.keySet())
                {
                    final Collection<IssueLinkType> linkTypes = linkTypeMappings.get(direction);
                    for (IssueLinkType linkType : linkTypes)
                    {
                        List<Issue> issues = null;
                        switch (direction)
                        {
                            case IN:
                                issues = linkCollection.getInwardIssues(linkType.getName());
                                break;
                            case OUT:
                                issues = linkCollection.getOutwardIssues(linkType.getName());
                                break;
                        }

                        if (issues != null)
                        {
                            linkedIssues.addAll(issues);
                        }
                    }
                }
            }
            else
            {
                linkedIssues.addAll(linkCollection.getAllIssues());
            }
        }

        return linkedIssues;
    }

    private Long getLong(String s)
    {
        try
        {
            return new Long(s);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    enum Direction
    {
        IN("in"), OUT("out");

        private final String name;

        private Direction(String name)
        {
            this.name = name;
        }

        String getName()
        {
            return name;
        }
    }
}
