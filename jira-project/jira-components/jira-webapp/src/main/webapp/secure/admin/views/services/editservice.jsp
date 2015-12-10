
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.services.edit.service'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="services"/>       
</head>

<body>

<page:applyDecorator name="jiraform">
	<page:param name="action">EditService.jspa</page:param>
	<page:param name="submitId">update_submit</page:param>
	<page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>
	<page:param name="cancelURI">ViewServices!default.jspa</page:param>
	<page:param name="width">100%</page:param>
	<page:param name="title"><ww:text name="'admin.services.edit.service'"/>: <ww:property value="service/name" /></page:param>
    <page:param name="helpURL">services</page:param>
    <page:param name="helpURLFragment">#messagehandlers</page:param>
	<page:param name="description">
        <ww:if test="description != ''">
            <p>
                <b><ww:text name="'common.words.description'"/>:</b><br/>
                <ww:property value="text(description)" escape="false" />
            </p>
        </ww:if>
        <p><ww:text name="'admin.services.edit.instructions'"/></p>
        <ww:if test="service/serviceClass == 'com.atlassian.jira.service.services.file.FileService'">
            <ww:text name="'admin.services.edit.file.service.directory'">
                <ww:param><ww:property value="/fileServiceBasePath"/></ww:param>
            </ww:text>
        </ww:if>
    </page:param>
    <page:param name="instructions" >
        <ww:if test="/removedPath != null">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">warning</aui:param>
                <aui:param name="'messageHtml'">
                    <p><ww:text name="'admin.services.edit.removed.path.msg'"/> <strong><ww:property value="/removedPath"/></strong></p>
                </aui:param>
            </aui:component>
         </ww:if>
        <ww:if test="/validMailParameters == false">
            <%@include file="/includes/admin/email/badmailprops.jsp"%>
        </ww:if>
    </page:param>



    <ww:iterator value="/objectConfigurationKeys" >
    <%@ include file="/includes/panels/objectconfiguration_form.jsp"  %>
    </ww:iterator>

	<ui:textfield label="text('admin.services.delay')" name="'delay'" size="'30'">
        <ui:param name="'description'">
            <ww:text name="'admin.services.edit.delay.description'"/><br />
            <ww:text name="'admin.services.edit.delay'"/>
        </ui:param>
    </ui:textfield>

	<ui:component name="'id'" template="hidden.jsp" />
    <ww:iterator value="/hiddenParameters" status="''">
        <ui:component name="./key" value="./value" template="hidden.jsp" />
    </ww:iterator>
    

</page:applyDecorator>

</body>
</html>
