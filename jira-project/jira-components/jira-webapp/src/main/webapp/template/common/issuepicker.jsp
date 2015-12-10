<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ taglib uri="webwork" prefix="ww" %>

<%
    // Only include extra web resources (css, js) if Ajax Issue Picker turned on
    WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
    webResourceManager.requireResource("jira.webresources:autocomplete");
%>

<div class="ajax_autocomplete" id="<ww:property value="parameters['name']"/>_container">
      <input type="text" name="<ww:property value="parameters['name']"/>" id="<ww:property value="parameters['name']"/>"
      <ww:property value="parameters['size']">
         <ww:if test=".">size="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['maxlength']">
         <ww:if test=".">maxlength="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['nameValue']">
         <ww:if test=".">value="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['disabled']">
         <ww:if test="{parameters['disabled']}">DISABLED</ww:if>
      </ww:property>
      <ww:property value="parameters['readonly']">
         <ww:if test="{parameters['readonly']}">READONLY</ww:if>
      </ww:property>
      <ww:property value="parameters['onkeyup']">
         <ww:if test=".">onkeyup="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['tabindex']">
         <ww:if test=".">tabindex="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['onchange']">
         <ww:if test=".">onchange="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['style']">
         <ww:if test=".">style="<ww:property value="."/>"</ww:if>
      </ww:property>
      />
    <a class="popup-trigger" href="#" title="Quick link to recently viewed issues">[<ww:text name="'linkissue.picker.selectissue'"/>]</a>
    <div class="ajax_results" id="<ww:property value="parameters['name']"/>_results"></div>
    <ww:if test="applicationProperties/option('jira.ajax.autocomplete.issuepicker.enabled') == true">
        <div class="small"><ww:text name="'linkissue.picker.desc'"/></div>
    </ww:if>
    <ww:if test="parameters['sameProjectMessage'] == true">
        <div class="small"><ww:text name="'linkissue.picker.desc.same.project'">
            <ww:param name="'value0'"><ww:property value="parameters['selectedProjectKey']"/></ww:param>
        </ww:text></div>
    </ww:if>
</div>

<fieldset rel="<ww:property value="parameters['name']"/>" class="hidden issue-picker-params">
    <input type="hidden" title="fieldId" value="<ww:property value="parameters['name']" />">
    <input type="hidden" title="fieldName" value="<ww:property value="parameters['name']"/>">
    <input type="hidden" title="currentIssueKey" value="<ww:property value="parameters['currentIssue']"/>">
    <ww:property value="parameters['currentJQL']">
        <ww:if test=".">
            <input type="hidden" title="currentJQL" value="<ww:property value="."/>">
        </ww:if>
    </ww:property>
    <input type="hidden" title="singleSelectOnly" value="<ww:property value="parameters['singleSelectOnly']"><ww:if test="."><ww:property value="."/></ww:if><ww:else>false</ww:else></ww:property>">
    <input type="hidden" title="showSubTasks" value="<ww:property value="parameters['showSubTasks']"><ww:if test="."><ww:property value="."/></ww:if><ww:else>true</ww:else></ww:property>">
    <input type="hidden" title="showSubTaskParent" value="<ww:property value="parameters['showSubTasksParent']"><ww:if test="."><ww:property value="."/></ww:if><ww:else>true</ww:else></ww:property>">
    <input type="hidden" title="currentProjectId" value="<ww:property value="parameters['selectedProjectId']"><ww:if test="."><ww:property value="."/></ww:if></ww:property>">
    <input type="hidden" title="issuePickerEnabled" value="<ww:if test="applicationProperties/option('jira.ajax.autocomplete.issuepicker.enabled') == true">true</ww:if><ww:else>false</ww:else>">
</fieldset>
