<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.component.ComponentAccessor" %>
<%@ page import="com.atlassian.plugin.webresource.UrlMode" %>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<html>
<head>
    <ww:if test="/issueValid == true && /hasIssuePermission('work', /issue) == true && /timeTrackingFieldHidden(/issueObject) == false && /workflowAllowsEdit(/issueObject) == true">
        <title>
            <ww:if test="/editMode"><ww:text name="'logwork.edit.title'"/></ww:if>
            <ww:else><ww:text name="'logwork.title'"/></ww:else>
        </title>
        <meta name="decorator" content="issueaction" />
        <%
            KeyboardShortcutManager keyboardShortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
            keyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issuenavigation);
        %>
        <link rel="index" href="<ww:url value="/issuePath" atltoken="false"/>" />
    </ww:if>
    <ww:else>
        <title><ww:text name="'common.words.error'"/></title>
        <meta name="decorator" content="message" />
    </ww:else>
</head>
<body>
<ww:if test="/issueValid == true && /hasIssuePermission('work', /issue) == true && /timeTrackingFieldHidden(/issueObject) == false && /workflowAllowsEdit(/issueObject) == true">
    <page:applyDecorator id="log-work" name="auiform">
        <page:param name="action"><ww:property value="/actionName"/>.jspa</page:param>
        <page:param name="submitButtonName">Log</page:param>
        <page:param name="showHint">true</page:param>
        <ww:property value="/hint('log_work')">
            <ww:if test=". != null">
                <page:param name="hint"><ww:property value="./text" escape="false" /></page:param>
                <page:param name="hintTooltip"><ww:property value="./tooltip" escape="false" /></page:param>
            </ww:if>
        </ww:property>
        <page:param name="submitButtonText"><ww:text name="'common.forms.log'"/></page:param>
        <page:param name="cancelLinkURI"><ww:if test="/issueValid == true"><ww:url value="/issuePath" atltoken="false"/></ww:if></page:param>

        <aui:component template="issueFormHeading.jsp" theme="'aui/dialog'">
            <aui:param name="'title'"><ww:if test="/editMode"><ww:text name="'logwork.edit.title'"/></ww:if><ww:else><ww:text name="'logwork.title'"/></ww:else></aui:param>
            <aui:param name="'subtaskTitle'"><ww:text name="'logwork.title.subtask'"/></aui:param>
            <aui:param name="'issueKey'"><ww:property value="/issueObject/key" escape="false"/></aui:param>
            <aui:param name="'issueSummary'"><ww:property value="/issueObject/summary" escape="false"/></aui:param>
            <aui:param name="'cameFromSelf'" value="/cameFromIssue"/>
            <aui:param name="'cameFromParent'" value="/cameFromParent"/>
        </aui:component>

        <aui:component name="'worklogId'" template="hidden.jsp" theme="'aui'" value="/worklogId" />
        <aui:component name="'id'" template="hidden.jsp" theme="'aui'"/>

        <page:applyDecorator name="auifieldset">
            <page:param name="legend"><ww:text name="'logwork.time.entry'"/></page:param>

            <page:applyDecorator name="auifieldgroup">
                <page:param name="description">
                    <ww:text name="'logwork.timespent.desc.short'"/>
                </page:param>
                <aui:textfield id="'time-logged'" label="text('common.concepts.time.spent')" mandatory="'true'" name="'timeLogged'" size="'short'" theme="'aui'" />
                <span class="aui-form example"><ww:text name="'logwork.example'"><ww:param value="'3w 4d 12h'"/></ww:text></span>
                <ww:component name="'loggingwork_local'" template="help.jsp" theme="'aui'"/>
            </page:applyDecorator>

            <page:applyDecorator name="auifieldgroup">
                <aui:component id="'date-logged'" label="text('logwork.date.started')" mandatory="'true'" name="'startDate'" template="datepicker.jsp" theme="'aui'">
                    <aui:param name="'size'">medium</aui:param>
                    <aui:param name="'iconText'"><ww:text name="'date.picker.select.date'"/></aui:param>
                    <aui:param name="'iconURI'">#</aui:param>
                    <aui:param name="'iconCssClass'">icon-date</aui:param>
                    <aui:param name="'iconTitle'"><ww:text name="'date.picker.select.date'"/></aui:param>
                    <aui:param name="'showsTime'" value="'true'" />
                </aui:component>
            </page:applyDecorator>

            <page:applyDecorator name="auifieldset">
                <page:param name="type">group</page:param>
                <page:param name="legend"><ww:text name="'common.concepts.remaining.estimate'"/></page:param>

                <%--         Radio 1           --%>
                <page:applyDecorator name="auifieldgroup">
                    <page:param name="type">radio</page:param>
                    <page:param name="description"><ww:text name="'logwork.bullet1.autoadjust.desc'"/></page:param>

                    <%-- Set the checked state of the radio --%>
                    <ww:if test="adjustEstimate == 'auto'"><ww:property id="adjust-estimate-auto-checked" value="'true'"/></ww:if>
                    <aui:radio checked="@adjust-estimate-auto-checked" id="'adjust-estimate-auto'" label="text('logwork.bullet1.adjust.automatically')" list="null" name="'adjustEstimate'" theme="'aui'" value="'auto'"/>
                </page:applyDecorator>

                <%--         Radio 2           --%>
                <page:applyDecorator name="auifieldgroup">
                    <page:param name="type">radio</page:param>

                    <%-- Conditionally set the content of the label to a variable we can insert into the component attribute --%>
                    <ww:if test="estimate==null"><ww:property id="label-estimate-leave" value="text('logwork.bullet2.leave.unset')"/></ww:if>
                    <ww:else><ww:property id="label-estimate-leave" value="text('logwork.bullet2.use.existing.estimate', estimate)"/></ww:else>
                    <%-- Set the checked state of the radio --%>
                    <ww:if test="adjustEstimate == 'leave'"><ww:property id="adjust-estimate-leave-checked" value="'true'"/></ww:if>
                    <aui:radio checked="@adjust-estimate-leave-checked" id="'adjust-estimate-leave'" label="@label-estimate-leave" list="null" name="'adjustEstimate'" theme="'aui'" value="'leave'"/>
                </page:applyDecorator>

                <%--         Radio 3           --%>
                <page:applyDecorator name="auifieldgroup">
                    <page:param name="type">radio</page:param>

                    <%-- Set the checked state of the radio --%>
                    <ww:if test="adjustEstimate == 'new'"><ww:property id="adjust-estimate-new-checked" value="'true'"/></ww:if>
                    <aui:radio checked="@adjust-estimate-new-checked" id="'adjust-estimate-new'" label="text('logwork.bullet3.set.to')" list="null" name="'adjustEstimate'" theme="'aui'" value="'new'"/>
                    <aui:textfield id="'adjust-estimate-new-value'" label="''" name="'newEstimate'" size="'short'" theme="'aui'" value="/newEstimate"/>
                    <span class="aui-form example"><ww:text name="'logwork.example'"><ww:param value="'3w 4d 12h'"/></ww:text></span>
                </page:applyDecorator>

                <%--         Radio 4           --%>
                <ww:if test="createWorklog">
                    <page:applyDecorator name="auifieldgroup">
                        <page:param name="type">radio</page:param>

                        <ww:if test="adjustEstimate == 'manual'"><ww:property id="adjust-estimate-manual-checked" value="true"/></ww:if>
                        <aui:radio checked="@adjust-estimate-manual-checked" id="'adjust-estimate-manual'" label="text('logwork.bullet4.reduce.by')" list="null" name="'adjustEstimate'" theme="'aui'" value="'manual'"/>
                        <aui:textfield id="'adjust-estimate-manual-value'" label="''" name="'adjustmentAmount'" size="'short'" theme="'aui'" value="/adjustmentAmount"/>
                        <span class="aui-form example"><ww:text name="'logwork.example'"><ww:param value="'3w 4d 12h'"/></ww:text></span>
                    </page:applyDecorator>
                </ww:if>
            </page:applyDecorator>

            <page:applyDecorator name="auifieldgroup">
                <%--@declare id="comment"--%><label for="comment"><ww:text name="'logwork.workdesc'"/></label>
                <ww:property value="/workDescriptionEditHtml" escape="false"/>
                <ww:property id="groupsNotPresent" value="groupLevels/empty"/>
                <ww:property id="rolesNotPresent" value="roleLevels/empty"/>
                <div class="security-level">
                    <fieldset class="hidden parameters">
                        <input type="hidden" title="securityLevelViewableByAll" value="<ww:text name="'security.level.viewable.by.all'"/>">
                        <input type="hidden" title="securityLevelViewableRestrictedTo" value="<ww:text name="'security.level.restricted.to'"/>">
                    </fieldset>
                    <a class="drop" href="#"><span class="icon <ww:if test="/commentLevel == null">icon-unlocked</ww:if><ww:else>icon-locked</ww:else>"></span><span class="icon drop-menu"></span></a>
                    <select name="commentLevel" id="commentLevel">
                        <option value=""><ww:text name="'comment.constants.allusers'"/></option>
                        <ww:if test="@rolesNotPresent == false">
                            <optgroup label="<ww:text name="'common.words.project.roles'"/>">
                                <!-- TODO: keep value on error -->
                                <ww:iterator value="roleLevels">
                                    <option value="role:<ww:property value="./id"/>" <ww:if test="/levelSelected('role:' + ./id) == true">selected</ww:if>><ww:property value="./name"/></option>
                                 </ww:iterator>
                            </optgroup>
                        </ww:if>
                        <ww:if test="@groupsNotPresent == false">
                            <optgroup label="<ww:text name="'common.words.groups'"/>">
                                <ww:iterator value="groupLevels">
                                    <option value="group:<ww:property value="."/>" <ww:if test="/levelSelected('group:' + .) == true">selected</ww:if>><ww:property value="."/></option>
                                </ww:iterator>
                            </optgroup>
                        </ww:if>
                    </select>
                    <span class="current-level"><ww:property value="/selectedLevelName" escape="false" /></span>
                </div>
            </page:applyDecorator>
        </page:applyDecorator>

        <ww:if test="/onDemand == false">
        <img src="<%= ComponentAccessor.getWebResourceUrlProvider().getStaticResourcePrefix(UrlMode.AUTO) %>/images/icons/marketplace-ico.png" width="16" height="16" border="0" style="padding-left: 5px"/>
        <ww:text name="'logwork.marketplace.link'">
            <ww:param name="'value0'"><a href="<%=request.getContextPath()%>/plugins/servlet/upm/marketplace/featured?category=Worklog+%26+Time-tracking&source=time_tracking_log" style="vertical-align: text-bottom"></ww:param>
            <ww:param name="'value1'"></a></ww:param>
        </ww:text>
        </ww:if>
    </page:applyDecorator>
</ww:if>
<ww:else>
    <div class="form-body">
        <header>
            <h1><ww:text name="'common.words.error'"/></h1>
        </header>
        <%@ include file="/includes/issue/generic-errors.jsp" %>
    </div>
</ww:else>
</body>
</html>