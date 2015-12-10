<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/issue_types_section"/>
    <meta name="admin.active.tab" content="issue_type_schemes"/>
	<title><ww:text name="'admin.issuesettings.associate'"/></title>
</head>

<body>
    <ww:bean name="'org.apache.commons.lang.ClassUtils'" id="classUtils" />

<ww:if test="/allProjects/empty == true">
    <page:applyDecorator name="jirapanel">
        <%--<page:param name="helpURL">configcustomfield</page:param>--%>
        <%--<page:param name="helpURLFragment">#Managing+multiple+configuration+schemes</page:param>--%>
		<page:param name="title"><ww:text name="'admin.issuesettings.associate'"/></page:param>
		<page:param name="instructions">
            <ww:text name="'admin.issuesettings.associate.no.projects.available'">
                <ww:param name="'value0'"><ww:property value="configScheme/name" /></ww:param>
            </ww:text>
        </page:param>
		<page:param name="width">100%</page:param>
    	<page:param name="cancelURI">ManageIssueTypeSchemes!default.jspa</page:param>
	</page:applyDecorator>
</ww:if>
<ww:else>
    <page:applyDecorator name="jiraform">
        <%--<page:param name="helpURL">configcustomfield</page:param>--%>
        <%--<page:param name="helpURLFragment">#Managing+multiple+configuration+schemes</page:param>--%>
		<page:param name="title"><ww:text name="'admin.issuesettings.associate'"/></page:param>
		<page:param name="instructions">
            <p><ww:text name="'admin.issuesettings.associate.instructions'">
                <ww:param name="'value0'"><strong><ww:property value="configScheme/name" /></strong></ww:param>
            </ww:text></p>
            <ww:if test="/default == true">
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">info</aui:param>
                    <aui:param name="'messageHtml'">
                        <p><ww:text name="'admin.issuesettings.associate.only.unassociated.displayed'"/></p>
                    </aui:param>
                </aui:component>
            </ww:if>
        </page:param>
		<page:param name="action"><ww:property value="@classUtils/shortClassName(/class)" />.jspa</page:param>
		<page:param name="width">100%</page:param>
    	<page:param name="cancelURI">ManageIssueTypeSchemes!default.jspa</page:param>
		<page:param name="submitId">associate_submit</page:param>
		<page:param name="submitName"><ww:text name="'admin.projects.schemes.associate'"/></page:param>

        <ui:component template="multihidden.jsp" >
            <ui:param name="'fields'">schemeId,fieldId</ui:param>
        </ui:component>

        <ui:component label="text('admin.issuesettings.scheme.name')" value="configScheme/name" template="textlabel.jsp" />
        <ww:if test="configScheme/description && configScheme/description != ''">
            <ui:component label="text('common.words.description')" value="configScheme/description" template="textlabel.jsp" />
        </ww:if>

        <ui:select label="text('common.concepts.projects')" name="'projects'" template="selectmultiple.jsp"
                   list="/allProjects" listKey="'id'" listValue="'name'" >
           <ui:param name="'size'">5</ui:param>
           <ui:param name="'optionTitle'">description</ui:param>
           <ui:param name="'description'"><ww:text name="'admin.issuesettings.associate.apply.for.all'"/></ui:param>
        </ui:select>

	</page:applyDecorator>
</ww:else>

</body>
</html>
