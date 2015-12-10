<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<ww:property value="/manageableOption" >
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/issue_types_section"/>
    <meta name="admin.active.tab" content="issue_type_schemes"/>
	<title><ww:text name="'admin.issuesettings.confirmdeletescheme.title'">
	    <ww:param name="'value0'"><ww:property value="titleSingle" /></ww:param>
	    <ww:param name="'value1'"><ww:property value="/configScheme/name" /></ww:param>
	</ww:text></title>

</head>
<body>

<page:applyDecorator name="jiraform">
    <page:param name="title"><ww:text name="'admin.issuesettings.confirmdeletescheme.title'">
	    <ww:param name="'value0'"><ww:property value="titleSingle" /></ww:param>
	    <ww:param name="'value1'"><ww:property value="/configScheme/name" /></ww:param>
	</ww:text></page:param>
    <page:param name="submitId">delete_submit</page:param>
    <page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
    <page:param name="cancelURI">ManageIssueTypeSchemes!default.jspa</page:param>
    <page:param name="action">DeleteOptionScheme.jspa</page:param>

    <ui:component name="'schemeId'" template="hidden.jsp" theme="'single'" />

    <tr><td colspan="2">
        <ww:text name="'admin.issuesettings.confirmdeletescheme.about.to.delete'">
            <ww:param name="'value0'"><ww:property value="titleSingle" /></ww:param>
            <ww:param name="'value1'"><strong><ww:property value="/configScheme/name" /></strong></ww:param>
        </ww:text>
        <ww:property value="/configScheme/associatedProjects">
            <ww:if test=".">
                <ww:if test="./size() == 1">
                    <ww:text name="'admin.issuesettings.confirmdeletescheme.one.project'">
                        <ww:param name="'value0'"><ww:property value="./iterator()/next()/string('name')" /></ww:param>
                    </ww:text>
                </ww:if>
                <ww:else>
                    <ww:if test="./size() == 0">
                        <ww:text name="'admin.issuesettings.confirmdeletescheme.no.projects'"/>
                    </ww:if>
                    <ww:else>
                        <ww:text name="'admin.issuesettings.confirmdeletescheme.multiple.projects'">
                            <ww:param name="'value0'"><ww:property value="./size()" /></ww:param>
                            <ww:param name="'value1'">
                                <ww:iterator value="." status="'status'">
                                    <ww:if test="@status/first == true">(</ww:if><strong><ww:property value="./string('name')" /></strong><ww:if test="@status/last == true">)</ww:if><ww:else>, </ww:else>
                                </ww:iterator>
                            </ww:param>
                        </ww:text>
                    </ww:else>
                </ww:else>
            </ww:if>
        </ww:property>
    </td></tr>
</page:applyDecorator>

</body>
</html>
</ww:property>
