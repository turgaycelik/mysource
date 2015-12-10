<%@ taglib prefix="ww" uri="webwork" %>
<%@ page import="com.atlassian.jira.config.properties.ApplicationProperties" %>
<%@ page import="com.atlassian.jira.config.properties.APKeys" %>
<%@ page import="com.opensymphony.util.TextUtils" %>
<%@ page import="com.atlassian.jira.util.velocity.VelocityRequestContextFactory" %>
<%@ page import="com.atlassian.jira.component.ComponentAccessor" %>
<%
    final ApplicationProperties ap = ComponentAccessor.getApplicationProperties();
    final String baseUrl = ComponentAccessor.getComponentOfType(VelocityRequestContextFactory.class).getJiraVelocityRequestContext().getCanonicalBaseUrl();
    final String titlePrefix = ap.getDefaultBackedString(APKeys.JIRA_TITLE);
    response.setContentType("text/xml");
%>
<OpenSearchDescription xmlns="http://a9.com/-/spec/opensearch/1.1/" xmlns:moz="http://www.mozilla.org/2006/browser/search/">
    <ShortName><%=TextUtils.htmlEncode(titlePrefix)%></ShortName>
    <Description><ww:text name="'common.concepts.open.search.description'"/></Description>
    <Image height="16" width="16" type="image/x-icon"><%=baseUrl%>/images/16jira.png</Image>
    <Image height="64" width="64" type="image/png"><%=baseUrl%>/images/64jira.png</Image>
    <Url type="text/html" template="<%=baseUrl%>/secure/QuickSearch.jspa?searchString={searchTerms}"/>
    <Query role="example" searchTerms="JIRA"/>
    <InputEncoding><%=ap.getEncoding()%></InputEncoding>
    <moz:SearchForm><%=baseUrl%>/secure/QuickSearch.jspa</moz:SearchForm>
</OpenSearchDescription>
