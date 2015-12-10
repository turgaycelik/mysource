<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<html>
<head>
    <title><ww:text name="'portal.edit.and.share.page'"/></title>
    <content tag="section">home_link</content>
</head>
<body class="page-type-issuenav">
<ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
    <ui:param name="'mainContent'">
        <h1><ww:text name="'portal.edit.and.share.page'"/></h1>
    </ui:param>
    <ui:param name="'helpContent'">
        <ww:component template="help.jsp" name="'portlets.dashboard_pages'">
            <ww:param name="'helpURLFragment'">#editing_dashboards</ww:param>
        </ww:component>
    </ui:param>
</ui:soy>

<ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanel'">
    <ui:param name="'content'">
        <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanelContent'">
            <ui:param name="'content'">

            <page:applyDecorator id="edit-dashboard" name="auiform">
                <page:param name="action">EditPortalPage.jspa</page:param>
                <page:param name="cancelLinkURI">ConfigurePortalPages!default.jspa</page:param>
                <page:param name="submitButtonName">update_submit</page:param>
                <page:param name="submitButtonText"><ww:text name="'common.forms.update'"/></page:param>

                <%--Used for warnings of dashboard share options--%>
                <div id="share_warning"></div>

                <page:applyDecorator name="auifieldgroup">
                    <aui:textfield label="text('common.words.name')" name="'portalPageName'" theme="'aui'">
                        <aui:param name="'mandatory'">true</aui:param>
                    </aui:textfield>
                </page:applyDecorator>

                <page:applyDecorator name="auifieldgroup">
                    <aui:textarea label="text('common.concepts.description')" name="'portalPageDescription'" rows="3" theme="'aui'" />
                </page:applyDecorator>


                <page:applyDecorator name="auifieldgroup">
                    <ww:component template="formFieldLabel.jsp" label="text('common.favourites.favourite')" theme="'aui'"/>
                    <ww:component name="'favourite'" template="favourite-new.jsp" theme="'aui'">
                        <ww:param name="'enabled'"><ww:property value="./favourite" /></ww:param>
                        <ww:param name="'fieldId'">favourite</ww:param>
                        <ww:param name="'entityType'">PortalPage</ww:param>
                    </ww:component>
                </page:applyDecorator>

                <ww:if test="/showShares == true">
                    <ww:component name="'shares'" label="text('common.sharing.shares')" template="edit-share-types.jsp" theme="'aui'">
                        <ww:param name="'shareTypeList'" value="/shareTypes"/>
                        <ww:param name="'noJavaScriptMessage'">
                           <ww:text name="'common.sharing.no.share.javascript'"/>
                        </ww:param>
                        <ww:param name="'editEnabled'" value="/editEnabled"/>
                        <ww:param name="'dataString'" value="/jsonString"/>
                        <ww:param name="'submitButtonId'">update_submit</ww:param>
                    </ww:component>
                </ww:if>
                <ui:component name="'pageId'" template="hidden.jsp" theme="'single'" />
            </page:applyDecorator>

            </ui:param>
        </ui:soy>
    </ui:param>
</ui:soy>
</body>
</html>
