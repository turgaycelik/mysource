package com.atlassian.jira.instrumentation;

/**
 * An enum of Instrumentation names
 */
public enum InstrumentationName
{
    /**
     * This is an {@link com.atlassian.instrumentation.operations.OpCounter} of web requests
     */
    WEB_REQUESTS("web.requests"),

    /**
     * This is an {@link com.atlassian.instrumentation.operations.OpCounter} of SOAP requests
     */
    SOAP_REQUESTS("web.soap.requests"),

    /**
     * This is an {@link com.atlassian.instrumentation.operations.OpCounter} of XMLRPC requests
     */
    XMLRPC_REQUESTS("web.xmlrpc.requests"),

    /**
     * This is an {@link com.atlassian.instrumentation.operations.OpCounter} of REST requests
     */
    REST_REQUESTS("web.rest.requests"),

    /**
     * This is a {@link com.atlassian.instrumentation.operations.OpCounter} of database read requests
     */
    DB_READS("db.reads"),

    /**
     * This is a {@link com.atlassian.instrumentation.operations.OpCounter} of database write requests
     */
    DB_WRITES("db.writes"),


    /**
     * This is a {@link com.atlassian.instrumentation.operations.OpCounter} of database connections taken from the connection pool
     */
    DB_CONNECTIONS("db.conns"),

    /**
     * This is a {@link com.atlassian.instrumentation.Gauge} of database connections borrowed
     */
    DB_CONNECTIONS_BORROWED("db.conns.borrowed"),

    /**
     * This is a {@link com.atlassian.instrumentation.operations.OpCounter} of issue index read requests
     */
    ISSUE_INDEX_READS("issue.index.reads"),

    /**
     * This is a {@link com.atlassian.instrumentation.operations.OpCounter} of issue index write requests
     */
    ISSUE_INDEX_WRITES("index.writes"),

    /**
     * The number of 500 IOExceptions that are thrown
     */
    FIVE_HUNDREDS("five.hundreds"),

    /**
     * The number of concurrent requests being processed in this instance of JIRA
     */
    CONCURRENT_REQUESTS("concurrent.requests"),

    /**
     * The number of http session objects in this instance of JIRA
     */
    HTTP_SESSION_OBJECTS("http.session.objects"),
    /**
     * The number of http session in this instance of JIRA
     */
    HTTP_SESSIONS("http.sessions"),

    /**
     * This indicates the number of issues in JIRA
     */
    TOTAL_ISSUES("entity.issues.total"),

    /**
     * This indicates the number of projects in JIRA
     */
    TOTAL_PROJECTS("entity.projects.total"),

    /**
     * This indicates the number of custom fields in JIRA
     */
    TOTAL_CUSTOMFIELDS("entity.customfields.total"),

    /**
     * This indicates the number of workflows in JIRA
     */
    TOTAL_WORKFLOWS("entity.workflows.total"),

    /**
     * This indicates the number of users in JIRA
     */
    TOTAL_USERS("entity.users.total"),

    /**
     * This indicates the number of groups in JIRA
     */
    TOTAL_GROUPS("entity.groups.total"),

    /**
     * Replicated Index Operations applied in the latest invocation.
     */
    CLUSTER_REPLICATED_INDEX_OPERATIONS_LATEST("replicated.index.operations.latest"),

    /**
     * Total Replicated Index Operations applied.
     */
    CLUSTER_REPLICATED_INDEX_OPERATIONS_TOTAL("replicated.index.operations.total"),

    /**
     * The number of active connections in the pool.
     */
    DBCP_ACTIVE("dbcp.numActive"),

    /**
     * The number of idle connections in the pool.
     */
    DBCP_IDLE("dbcp.numIdle"),

    /**
     * The maximum number of connections in the pool.
     */
    DBCP_MAX("dbcp.maxActive"),

    /**
     * The number of Lucene searchers opened.
     */
    SEARCHER_LUCENE_OPEN("searcher.lucene.open"),

    /**
     * The number of JIRA searchers opened.
     */
    SEARCHER_JIRA_OPEN("searcher.jira.open.lazy"),

    /**
     * The number of Lucene searchers closed.
     */
    SEARCHER_LUCENE_CLOSE("searcher.lucene.close"),

    /**
     * The number of JIRA searchers closed.
     */
    SEARCHER_JIRA_CLOSE("searcher.jira.close"),

    /**
     * number of manual workflow transitions occurred.
     */
    WORKFLOW_MANUAL_TRANSITION("workflow.manual.transition"),

    /**
     * * number of automatic workflow transitions occurred.
     */
    WORKFLOW_AUTOMATIC_TRANSITION("workflow.automatic.transition");

    private final String instrumentName;

    InstrumentationName(String instrumentName)
    {
        this.instrumentName = instrumentName;
    }

    public String getInstrumentName()
    {
        return instrumentName;
    }
}
