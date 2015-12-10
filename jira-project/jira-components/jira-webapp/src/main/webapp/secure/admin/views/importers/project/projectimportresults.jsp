<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <title>
        <ww:text name="'admin.project.import.results.title'"/>
    </title>
</head>
<body>
<page:applyDecorator name="jiraform">
    <page:param name="action">ProjectImportResults!ViewNewProject.jspa</page:param>
    <page:param name="columns">1</page:param>
    <page:param name="submitId">ok_submit</page:param>
    <page:param name="submitName"><ww:text name="'admin.common.words.ok'"/></page:param>
    <page:param name="title"><ww:text name="'admin.project.import.results.title'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">restore_project</page:param>
    <page:param name="autoSelectFirst">false</page:param>
    <page:param name="description">
        <!-- This is the case where the import completed AND there were no errors -->
        <ww:if test="/projectImportResults/importCompleted == true  && /projectImportResults/errors/size() == 0">
            <ww:text name="'admin.project.import.results.desc.completed'">
                <ww:param name="'value0'"><ww:property value="/prettyImportDuration" /></ww:param>
            </ww:text>
        </ww:if>
        <!-- This is the case where the import DID complete AND the import was was successful but there were some errors, but these were below the limit-->
        <ww:elseIf test="/projectImportResults/importCompleted == true && /projectImportResults/errors/size() > 0">
            <ww:text name="'admin.project.import.results.desc.completed.with.errors'">
                <ww:param name="'value0'"><ww:property value="/prettyImportDuration" /></ww:param>
            </ww:text>
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">warning</aui:param>
                <aui:param name="'messageHtml'">
                    <p>
                        <ww:text name="'admin.project.import.results.complete.with.errors'"/>
                    </p>
                </aui:param>
            </aui:component>
        </ww:elseIf>
        <!-- This is the case where the import just did not complete and the import was NOT successful-->
        <ww:elseIf test="/projectImportResults/importCompleted == false ">
            <ww:text name="'admin.project.import.results.desc.not.completed'">
                <ww:param name="'value0'"><ww:property value="/prettyImportDuration" /></ww:param>
            </ww:text>
            <ww:if test="/projectImportResults/importedProject != null">
                <p/>
                <ww:text name="'admin.project.import.results.desc.not.completed.delete.project'">
                    <ww:param name="'value0'"><a href="<%=request.getContextPath()%>/secure/project/DeleteProject!default.jspa?pid=<ww:property value="/projectImportResults/importedProject/id"/>"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text>
            </ww:if>

        </ww:elseIf>
    </page:param>

    <tr>
        <td class="jiraformbody" id="projectimport">
            <div id="summary">
                <div id="systemfields">
                    <h3 class="results_header"><ww:text name="'common.concepts.projectsummary'"/></h3>
                    <ul class="results_list">
                        <ww:if test="/projectImportResults/importedProject == null">
                            <li class="results_field first">
                                <div class="field_label"><ww:text name="'admin.project.import.results.no.project.created'"/></div>
                                <div class="field_description">&nbsp;</div>
                            </li>
                        </ww:if>
                        <ww:else>
                            <li class="results_field first">
                                <div class="field_label"><ww:text name="'common.concepts.key'"/>:</div>
                                <div class="field_description"><a href="ProjectImportResults!ViewNewProject.jspa"><ww:property value="/projectImportResults/importedProject/key"/></a>&nbsp;</div>
                            </li>
                            <li class="results_field">
                                <div class="field_label"><ww:text name="'admin.project.import.select.project.proj.desc'"/>:</div>
                                <div class="field_description"><ww:property value="/projectImportResults/importedProject/description"/>&nbsp;</div>
                            </li>
                            <li class="results_field">
                                <div class="field_label"><ww:text name="'admin.project.import.select.project.proj.lead'"/>:</div>
                                <div class="field_description"><ww:property value="/projectImportResults/importedProject/lead/displayName"/>&nbsp;</div>
                            </li>
                            <li class="results_field">
                                <div class="field_label"><ww:text name="'admin.project.import.select.project.proj.url'"/>:</div>
                                <div class="field_description"><ww:property value="/projectImportResults/importedProject/url"/>&nbsp;</div>
                            </li>
                            <li class="results_field">
                                <div class="field_label"><ww:text name="'admin.project.import.select.project.proj.sender.address'"/>:</div>
                                <div class="field_description"><ww:property value="/projectEmail(/projectImportResults/importedProject)" />&nbsp;</div>
                            </li>
                            <li class="results_field">
                                <div class="field_label"><ww:text name="'admin.project.import.select.project.proj.default.assignee'"/>:</div>
                                <div class="field_description"><ww:property value="/assigneeTypeString(/projectImportResults/importedProject/assigneeType)"/>&nbsp;</div>
                            </li>
                            <li class="results_field">
                                <div class="field_label"><ww:text name="'admin.project.import.select.project.proj.components'"/>:</div>
                                <div class="field_description"><ww:property value="/projectImportResults/importedProject/components/size()"/>&nbsp;</div>
                            </li>
                            <li class="results_field last">
                                <div class="field_label"><ww:text name="'admin.project.import.select.project.proj.versions'"/>:</div>
                                <div class="field_description"><ww:property value="/projectImportResults/importedProject/versions/size()"/>&nbsp;</div>
                            </li>
                        </ww:else>
                        <ww:if test="/projectImportResults/errors/size() > 0">
                            <h3 class="results_header"><ww:text name="'panel.errors'"/></h3>
                            <div class="notification"><ww:text name="'admin.project.import.results.view.your.logs'"/>&nbsp;</div>
                            <ul id="results_error_list">
                                <ww:iterator value="/projectImportResults/errors">
                                    <li class="results_field">
                                        <div class="field_error"><img src="<%= request.getContextPath() %>/images/icons/cancel.png" alt="" title="Error"><ww:property value="."/></div>
                                    </li>
                                </ww:iterator>
                            </ul>
                        </ww:if>
                    </ul>
                </div>

                <div id="customfields">
                    <h3 class="results_header"><ww:text name="'admin.common.words.users'"/></h3>
                    <ul>
                        <li class="results_field only">
                            <ww:if test="/projectImportResults/usersCreatedCount == 0 && /projectImportResults/expectedUsersCreatedCount == 0">
                                <div class="field_label"><ww:text name="'admin.project.import.results.no.users.created'"/></div>
                                <div class="field_description">&nbsp;</div>
                            </ww:if>
                            <ww:else>
                                <div class="field_label"><ww:text name="'admin.common.words.users'"/>:</div>
                                <div class="field_description">
                                    <ww:text name="'admin.project.import.results.x.out.of.x'">
                                        <ww:param name="'value0'"><ww:property value="/projectImportResults/usersCreatedCount"/></ww:param>
                                        <ww:param name="'value1'"><ww:property value="/projectImportResults/expectedUsersCreatedCount"/></ww:param>
                                    </ww:text>&nbsp;
                                </div>
                            </ww:else>
                        </li>
                    </ul>
                    <h3 class="results_header"><ww:text name="'admin.projects.project.roles'"/></h3>
                    <ul>
                        <ww:if test="/projectImportResults/roles/size() > 0">
                            <ww:iterator value="/projectImportResults/roles">
                                <li class="results_field first">
                                    <div class="field_label"><ww:property value="."/>:</div>
                                    <div class="field_description"><ww:property value="/projectImportResults/usersCreatedCountForRole(.)"/> <ww:text name="'admin.project.import.results.users'"/>, <ww:property value="/projectImportResults/groupsCreatedCountForRole(.)"/> <ww:text name="'admin.project.import.results.groups'"/>&nbsp;</div>
                                </li>
                            </ww:iterator>
                        </ww:if>
                        <ww:else>
                            <li class="results_field first">
                                <div class="field_label"><ww:text name="'admin.project.import.results.no.members.created'"/></div>
                                <div class="field_description">&nbsp;</div>
                            </li>
                        </ww:else>
                    </ul>
                    <h3 class="results_header"><ww:text name="'admin.project.import.select.project.proj.isssues'"/></h3>
                    <ul>
                        <li class="results_field first">
                            <ww:if test="/projectImportResults/issuesCreatedCount == 0 && /projectImportResults/expectedIssuesCreatedCount == 0">
                                <div class="field_label"><ww:text name="'admin.project.import.results.no.issues.created'"/></div>
                                <div class="field_description">&nbsp;</div>
                            </ww:if>
                            <ww:else>
                                <div class="field_label"><ww:text name="'admin.project.import.results.issues.created'"/>:</div>
                                <div class="field_description">
                                    <ww:text name="'admin.project.import.results.x.out.of.x'">
                                        <ww:param name="'value0'"><ww:property value="/projectImportResults/issuesCreatedCount"/></ww:param>
                                        <ww:param name="'value1'"><ww:property value="/projectImportResults/expectedIssuesCreatedCount"/></ww:param>
                                    </ww:text>&nbsp;
                                </div>
                            </ww:else>
                        </li>
                        <li class="results_field">
                            <ww:if test="/projectImportResults/attachmentsCreatedCount == 0 && /projectImportResults/expectedAttachmentsCreatedCount == 0">
                                <div class="field_label"><ww:text name="'admin.project.import.results.no.attachments.created'"/></div>
                                <div class="field_description">&nbsp;</div>
                            </ww:if>
                            <ww:else>
                                <div class="field_label"><ww:text name="'common.concepts.attachments.files'"/>:</div>
                                <div class="field_description">
                                    <ww:text name="'admin.project.import.results.x.out.of.x'">
                                        <ww:param name="'value0'"><ww:property value="/projectImportResults/attachmentsCreatedCount"/></ww:param>
                                        <ww:param name="'value1'"><ww:property value="/projectImportResults/expectedAttachmentsCreatedCount"/></ww:param>
                                    </ww:text>&nbsp;
                                </div>
                            </ww:else>
                        </li>
                    </ul>
                </div>

            </div>
        </td>
    </tr>
</page:applyDecorator>

</body>
</html>
