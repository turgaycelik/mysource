<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'filtersubscription.title'"/></title>
    <content tag="section">find_link</content>
    <meta name="decorator" content="panel-general" />
</head>
<body>
    <page:applyDecorator id="filter-subscription" name="auiform">
        <page:param name="action">FilterSubscription.jspa</page:param>
        <page:param name="method">post</page:param>
        <page:param name="submitButtonText"><ww:property value="submitName" escape="false" /></page:param>
        <page:param name="submitButtonName"><ww:property value="submitName" escape="false" /></page:param>
        <page:param name="cancelLinkURI"><ww:property value="cancelStr"/></page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'filtersubscription.title'"/></aui:param>
        </aui:component>

        <ww:if test="hasGroupPermission == true">
            <page:applyDecorator name="auifieldgroup">
                <aui:select label="text('filtersubscription.field.recipients')" name="'groupName'" list="groups" listKey="'.'" listValue="'.'" theme="'aui'">
                    <aui:param name="'defaultOptionText'">
                        <ww:text name="'filtersubscription.personal.sub'" />
                    </aui:param>
                    <aui:param name="'defaultOptionValue'" value="''"/>
                </aui:select>
            </page:applyDecorator>
        </ww:if>

        <ww:if test="lastRunStr">
            <page:applyDecorator name="auifieldgroup">
                <aui:component label="text('subscriptions.lastSent')" value="lastRunStr" template="formFieldValue.jsp" theme="'aui'" />
            </page:applyDecorator>
        </ww:if>

        <ww:if test="nextRunStr">
            <page:applyDecorator name="auifieldgroup">
                <aui:component label="text('subscriptions.nextSend')" value="nextRunStr" template="formFieldValue.jsp" theme="'aui'" />
            </page:applyDecorator>
        </ww:if>

        <ui:component name="'cron.editor.name'" label="text('filtersubscription.field.schedule')" template="croneditor.jsp">
            <ui:param name="'cronEditorBean'" value="/cronEditorBean"/>
            <ui:param name="'parameterPrefix'">filter.subscription.prefix</ui:param>
        </ui:component>

        <page:applyDecorator name="auifieldset">
            <page:param name="type">group</page:param>

            <page:applyDecorator name="auifieldgroup">
                <page:param name="type">checkbox</page:param>
                <aui:checkbox id="emailOnEmpty" name="'emailOnEmpty'" label="text('filtersubscription.emailEmptyResults')" fieldValue="true" theme="'aui'"/>
            </page:applyDecorator>
        </page:applyDecorator>

        <aui:component name="'lastRun'" template="hidden.jsp" theme="'aui'" />
        <aui:component name="'nextRun'" template="hidden.jsp" theme="'aui'"/>
        <aui:component name="'subId'" template="hidden.jsp" theme="'aui'"/>
        <aui:component name="'filterId'" template="hidden.jsp" theme="'aui'"/>
    </page:applyDecorator>
</body>
</html>

