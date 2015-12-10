<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib prefix="decorator" uri="sitemesh-decorator" %>
<%@ page import="com.atlassian.jira.web.util.HelpUtil" %>

<% HelpUtil helpUtil = new HelpUtil();
   HelpUtil.HelpPath externalHelpPath = helpUtil.getHelpPath("dbconfig.index");
   HelpUtil.HelpPath migrationHelpPath = helpUtil.getHelpPath("dbconfig.switchdbs");
   HelpUtil.HelpPath moreLanguagesHelpPath = helpUtil.getHelpPath("dbconfig.server.language.from.tac");

    // don't show ANYTHING to the user if they come here looking for trouble
    if (com.atlassian.jira.util.JiraUtils.isSetup())
    {
%>
<%--
Leave this as a raw HTML. Do not use response.getWriter() or response.getOutputStream() here as this will fail
on Orion. Let the application server figure out how it want to output this text.
--%>
JIRA has already been set up.
<%
}
else
{
%>
<html>
<head>
    <title><ww:text name="'setup.title'"/></title>
    <ww:if test="/instantSetupAlreadyChosen == false">
        <meta name="ajs-instant-setup" content="not-chosen" />
    </ww:if>
    <ww:else>
        <meta name="ajs-instant-setup" content="<ww:property value="/instantSetup"/>" />
    </ww:else>
</head>

<body class="jira-setup-page-database">

<ww:if test="databaseConnectionTestWorked == 'true'">
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">success</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:property value="text('setup.database.connection.test.successful')"/></p>
        </aui:param>
    </aui:component>
</ww:if>

<page:applyDecorator id="jira-setupwizard" name="auiform">
    <page:param name="action">SetupDatabase.jspa</page:param>
    <page:param name="hideToken">true</page:param>
    <page:param name="useCustomButtons">true</page:param>

    <ww:if test="showNonEmptyDatabaseUpgradeDocumentationMessage == 'true'">
        <p><ww:property value="./nonEmptyDatabaseUpgradeDocumentationMessage" escape="false"/></p>
    </ww:if>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'setupdb.title.instant.setup'"/></aui:param>
    </aui:component>

    <p><ww:text name="'setupdb.subtitle'" value0="productName"/></p>

    <img class="setup-banner" src="<%=request.getContextPath()%>/images/setup/setup-welcome-banner.png"/>

    <page:applyDecorator name="auifieldgroup">
        <page:param name="description">
            <ww:text name="'setupdb.server.language.description'" value0="productName"/>
        </page:param>
         <aui:select id="'server-language'" label="text('setup.choose.language')" name="'serverLanguage'" list="installedLocales" listKey="'key'" listValue="'value'" theme="'aui'">
             <aui:param name="'selectedValue'" value="defaultServerLanguage" />
             <aui:param name="'class'" value="long-item" />
         </aui:select>
    </page:applyDecorator>

    <page:applyDecorator name="auifieldset">
        <page:param name="type">group</page:param>
        <page:param name="legend"><ww:text name="'setupdb.option.label'" /></page:param>
        <page:applyDecorator name="auifieldgroup">
            <page:param name="type">radio</page:param>
            <aui:radio id="'database-internal'" label="text('setupdb.internal.label')" list="null" name="'databaseOption'" theme="'aui'">
                <aui:param name="'customValue'">INTERNAL</aui:param>
                <ww:if test="databaseOption == 'INTERNAL'">
                    <aui:param name="'checked'">true</aui:param>
                </ww:if>
            </aui:radio>

        </page:applyDecorator>
        <page:applyDecorator name="auifieldgroup">
            <page:param name="type">radio</page:param>
            <page:param name="description">
                <ww:text name="'setupdb.external.desc.line1'">
                    <ww:param name="'value0'"><a href="<%=migrationHelpPath.getUrl()%>" target="_blank"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text>
                <br />
                <ww:text name="'setupdb.external.desc.line2'">
                    <ww:param name="'value0'"><a href="<%=externalHelpPath.getUrl()%>" target="_blank"></ww:param>
                    <ww:param name="'value1'" value="productName"/>
                    <ww:param name="'value2'"></a></ww:param>
                </ww:text>
            </page:param>
            <aui:radio id="'database-external'" label="text('setupdb.external.label')" list="null" name="'databaseOption'" theme="'aui'">
                <aui:param name="'customValue'">EXTERNAL</aui:param>
                <ww:if test="databaseOption == 'EXTERNAL'">
                    <aui:param name="'checked'">true</aui:param>
                </ww:if>
            </aui:radio>
        </page:applyDecorator>
    </page:applyDecorator>

    <div id="setup-db-external" class="hidden">

        <page:applyDecorator name="auifieldgroup">
            <aui:select label="text('setupdb.databasetype.label')" name="'databaseType'" list="externalDatabases" listKey="'key'" listValue="'value'" theme="'aui'" />
            <div class="description" ></div>
            <div class="descriptions hidden">
                <ww:iterator value="databaseMessages">
                    <div class="<ww:property value="./type"/>" style="margin-top: 5px; font-size:13px">
                        <ww:if test="./hasMessage == 'true'">
                            <div class="aui-message warning">
                                <span class="aui-icon icon-warning"></span>
                                <ww:property value="./message" escape="false"/>
                                <%--<ww:text name="./messageKey">--%>
                                    <%--<ww:param name="'value0'"><ww:property value="./displayName"/></ww:param>--%>
                                    <%--<ww:param name="'value1'"><a href="<ww:property value="@helpPath/url"/>" title="<ww:property value="@helpPath/title"/>" alt="<ww:property value="@helpPath/alt"/>" target="_blank"></ww:param>--%>
                                    <%--<ww:param name="'value2'"></a></ww:param>--%>
                                    <%--<ww:param name="'value3'"></a></ww:param>--%>
                                <%--</ww:text>--%>
                            </div>
                        </ww:if>
                    </div>
                </ww:iterator>
            </div>

        </page:applyDecorator>



        <page:applyDecorator name="auifieldgroup">
            <page:param name="description"><ww:text name="'setupdb.jdbcHostname.desc'" /></page:param>
            <aui:textfield label="text('setupdb.jdbcHostname')" name="'jdbcHostname'" theme="'aui'" />
        </page:applyDecorator>
        <page:applyDecorator name="auifieldgroup">
            <page:param name="description"><ww:text name="'setupdb.jdbcPort.desc'" /></page:param>
            <aui:textfield label="text('setupdb.jdbcPort')" name="'jdbcPort'" theme="'aui'" />
        </page:applyDecorator>
        <div class="db-option-mssql db-option-postgres72 db-option-mysql setup-fields">
            <page:applyDecorator name="auifieldgroup">
                <page:param name="description"><ww:text name="'setupdb.jdbcDatabase.desc'" /></page:param>
                <aui:textfield label="text('setupdb.jdbcDatabase')" name="'jdbcDatabase'" theme="'aui'" />
            </page:applyDecorator>
        </div>
        <div class="db-option-oracle10g setup-fields hidden">
            <page:applyDecorator name="auifieldgroup">
                <page:param name="description"><ww:text name="'setupdb.jdbcSid.desc'" /></page:param>
                <aui:textfield label="text('setupdb.jdbcSid')" name="'jdbcSid'" theme="'aui'" />
            </page:applyDecorator>
        </div>
        <page:applyDecorator name="auifieldgroup">
            <page:param name="description"><ww:text name="'setupdb.jdbcUsername.desc'" /></page:param>
            <aui:textfield label="text('setupdb.jdbcUsername')" name="'jdbcUsername'" theme="'aui'" />
        </page:applyDecorator>
        <page:applyDecorator name="auifieldgroup">
            <page:param name="description"><ww:text name="'setupdb.jdbcPassword.desc'" /></page:param>
            <aui:password label="text('setupdb.jdbcPassword')" name="'jdbcPassword'" theme="'aui'" value="jdbcPassword" />
        </page:applyDecorator>
        <div class="db-option-mssql db-option-postgres72 setup-fields hidden">
            <page:applyDecorator name="auifieldgroup">
                <page:param name="description"><ww:text name="'setupdb.schemaName.desc'" /></page:param>
                <aui:textfield label="text('setupdb.schemaName')" name="'schemaName'" theme="'aui'" />
            </page:applyDecorator>
        </div>

    </div>

    <aui:component name="'testingConnection'" value="'false'" template="hidden.jsp" theme="'aui'" />

    <aui:component name="'language'" value="language" template="hidden.jsp" theme="'aui'" />
    <aui:component name="'changingLanguage'" value="'false'" template="hidden.jsp" theme="'aui'" />

    <page:applyDecorator name="auifieldgroup">
        <page:param name="type">buttons-container</page:param>
        <page:applyDecorator name="auifieldgroup">
            <page:param name="type">buttons</page:param>
            <aui:component theme="'aui'" template="formSubmit.jsp">
                <aui:param name="'id'">jira-setupwizard-submit</aui:param>
                <aui:param name="'submitButtonName'">next</aui:param>
                <aui:param name="'submitButtonText'"><ww:text name="'common.words.next'"/></aui:param>
                <aui:param name="'submitButtonCssClass'">aui-button-primary</aui:param>
            </aui:component>
            <aui:component name="'testConnection'" template="formButton.jsp" theme="'aui'">
                <aui:param name="'cssClass'">hidden</aui:param>
                <aui:param name="'id'">test-connection</aui:param>
                <aui:param name="'text'"><ww:text name="'setupdb.testconnection'" /></aui:param>
            </aui:component>
            <div class="hidden throbber-message">
                <span id="submit-throbber" class="hidden">
                    <span class="aui-icon aui-icon-wait"></span>
                    <ww:text name="'setupdb.database.being.setup'"/>
                </span>
                <span id="test-connection-throbber" class="hidden">
                    <span class="aui-icon aui-icon-wait"></span>
                    <ww:text name="'setupdb.database.test.connection'"/>
                </span>
            </div>
        </page:applyDecorator>
    </page:applyDecorator>
</page:applyDecorator>
</body>
</html>
<% } %>
