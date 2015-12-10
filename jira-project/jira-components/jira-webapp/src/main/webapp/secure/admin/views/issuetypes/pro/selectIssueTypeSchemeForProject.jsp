<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ page import="com.atlassian.jira.ComponentManager"%>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager"%>

<html>
<head>
    <title>
        <ww:text name="'admin.projects.issuetypescheme.select.for.project'">
            <ww:param name="'value0'"><ww:property value="/project/string('name')" /></ww:param>
        </ww:text>
    </title>
    <%
        WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
        webResourceManager.requireResource("jira.webresources:selectissuetypescheme");
    %>
</head>
<body>

	<page:applyDecorator name="jiraform">
        <%--<page:param name="helpURL">configcustomfield</page:param>--%>
        <%--<page:param name="helpURLFragment">#Managing+multiple+configuration+schemes</page:param>--%>
		<page:param name="title"><ww:text name="'admin.projects.issuetypescheme.select.for.project'">
        <ww:param name="'value0'"><ww:property value="/project/string('name')" /></ww:param>
    </ww:text></page:param>
		<page:param name="instructions">
            <p><ww:text name="'admin.projects.issuetypescheme.instructions'">
                <ww:param name="'value0'"><strong><ww:property value="/project/string('name')" /></strong></ww:param>
            </ww:text></p>
            <p>
                <ww:if test="/subTaskIssues > 0">,
                    <ww:text name="'admin.projects.issuetypescheme.there.are.current.issues.long'">
                        <ww:param name="'value0'"><strong><ww:property value="/standardIssues" /></strong></ww:param>
                        <ww:param name="'value1'"><strong><ww:property value="/subTaskIssues" /></strong></ww:param>
                    </ww:text>
                </ww:if>
                <ww:else>
                    <ww:text name="'admin.projects.issuetypescheme.there.are.current.issues.short'">
                        <ww:param name="'value0'"><strong><ww:property value="/standardIssues" /></strong></ww:param>
                    </ww:text>
                </ww:else>
                <ww:text name="'admin.projects.issuetypescheme.current.scheme.is'">
                    <ww:param name="'value0'"><strong><ww:property value="/currentIssueTypeScheme/name" /></strong></ww:param>
                </ww:text>
            </p>
            <div id="choose-section" class="tools">
            <ui:radio label="''"
                      name="'createType'"
                      list="/typeOptions"
                      theme="'single'">
                <ui:param name="'noTable'" value="'true'" />
            </ui:radio>
            </div>
        </page:param>
		<page:param name="action">SelectIssueTypeSchemeForProject.jspa</page:param>
		<page:param name="width">100%</page:param>
		<page:param name="labelWidth">20%</page:param>
    	<page:param name="cancelURI"><%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="/project/string('key')"/>/issuetypes</page:param>
		<page:param name="submitId">ok_submit</page:param>
		<page:param name="submitName"> <ww:text name="'admin.common.words.ok'"/> </page:param>


        <ui:component template="multihidden.jsp" >
            <ui:param name="'fields'">projectId,fieldId</ui:param>
        </ui:component>

        <tbody id="chooseScheme">
            <ui:select label="text('admin.projects.issue.type.scheme')" name="'schemeId'"
                       list="/allSchemes" listKey="'id'" listValue="'name'" >
               <ui:param name="'size'"><ww:property value="/allSchemes/size()" /></ui:param>
               <ui:param name="'summary'">description</ui:param>
               <ui:param name="'optionTitle'">description</ui:param>
               <ui:param name="'showOptionId'">true</ui:param>
            </ui:select>
        </tbody>

        <tbody id="chooseProject">

            <ui:select label="text('common.concepts.project')" name="'sameAsProjectId'"
                       list="/allProjects" listKey="'/configSchemeForProject(./name)/id'" listValue="'name'" >
               <ui:param name="'headerrow'"><ww:text name="'common.words.pleaseselect'" /></ui:param>
               <ui:param name="'optionTitle'">description</ui:param>
            </ui:select>

            <ui:component label="text('admin.projects.issue.type.scheme')" value="/configScheme/name" template="textlabel.jsp" name="'issueTypeSchemeLabel'" />
        </tbody>

        <tbody id="createScheme">
        <ww:if test="/existingAutoCreatedScheme">
            <tr><td colspan="2">
                <div class="warningBox halfWidth centered">
                    <ww:text name="'admin.projects.issuetypescheme.already.exists'">
                        <ww:param name="'value0'"><strong><ww:property value="defaultNameForNewScheme" /></strong></ww:param>
                    </ww:text>
                    <a href="<ww:url page="/secure/admin/ConfigureOptionSchemes!default.jspa">
                        <ww:param name="'fieldId'" value="'issuetype'" />
                        <ww:param name="'projectId'" value="/projectId" />
                        <ww:param name="'schemeId'" value="/existingAutoCreatedScheme/id" />
                        <ww:param name="'returnUrl'" >/plugins/servlet/project-config/<ww:property value="/project/string('key')"/>/issuetypes</ww:param>
                     </ww:url>"><ww:text name="'admin.projects.issuetypescheme.edit.scheme'"/></a>.
                </div>
            </td></tr>

        </ww:if>
        <ww:else>
            <ui:component label="text('common.concepts.project')" value="/project/string('name')" template="textlabel.jsp" >
            </ui:component>

            <ui:component label="text('admin.projects.issue.type.scheme')" value="/defaultNameForNewScheme" template="textlabel.jsp" >
                <ui:param name="'description'"><ww:text name="'admin.projects.issuetypescheme.note'"/></ui:param>
            </ui:component>

            <ui:select label="text('admin.projects.issuetypescheme.issue.types.for.scheme')" name="'selectedOptions'" template="selectmultiple.jsp"
                       list="/allOptions" listKey="'id'" listValue="'name'" >
                <ui:param name="'optionIcon'">imagePath</ui:param>
                <ui:param name="'optionTitle'">description</ui:param>
                <ui:param name="'description'"><ww:text name="'admin.projects.issuetypescheme.select.issue.types'"/></ui:param>
            </ui:select>
        </ww:else>
        </tbody>

        <tbody id="optionsForScheme">

            <tr>
                <td class="fieldLabelArea">
                    <ww:text name="'admin.projects.issuetypescheme.issue.types.for.scheme'"/>:
                </td>
                <td class="fieldValueArea">
                    <ww:iterator value="/allSchemes" status="'status'">
                        <ul class="imagebacked" id="<ww:property value="id" />" style="display: none; width: 100%;">
                            <ww:iterator value="/options(.)" status="'status'">
                                <li>
                                    <img class="icon jira-icon-image" src="<ww:url value="imagePath" />" alt="" />
                                    <ww:property value="./name" /> <ww:if test="./subTask == true"><span class="smallgrey">(<ww:text name="'admin.projects.issuetypescheme.sub.task'"/>)</span></ww:if><br />
                                </li>
                            </ww:iterator>
                        </ul>
                    </ww:iterator>
                </td>
            </tr>

        </tbody>

    </page:applyDecorator>
</body>
</html>

