<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<jsp:include page="/template/aui/formFieldLabel.jsp" />
<select class="aui-field-issuepicker hidden"  multiple="multiple" name="<ww:property value="parameters['name']"/>" id="<ww:property value="parameters['name']"/>"
        <ww:property value="parameters['currentJQL']">
            <ww:if test=".">data-ajax-options.data.current_jql="<ww:property value="."/>"</ww:if> 
        </ww:property>
        data-popup-link-message="[Select Issue]"
        data-popup-width="620"
        data-popup-height="500"
        data-remove-on-un-select="true"
        data-ajax-options.query="true"
        data-ajax-options.url="<%= request.getContextPath() %>/rest/api/1.0/issues/picker"
        data-ajax-options.data.current-issue-key="<ww:property value="parameters['currentIssue']"/>"
        data-ajax-options.data.show-sub-tasks="<ww:property value="parameters['showSubTasks']"><ww:if test="."><ww:property value="."/></ww:if><ww:else>true</ww:else></ww:property>"
        data-ajax-options.data.show-sub-task-parent="<ww:property value="parameters['showSubTasksParent']"><ww:if test="."><ww:property value="."/></ww:if><ww:else>true</ww:else></ww:property>"
        data-ajax-options.data.current-project-id="<ww:property value="parameters['selectedProjectId']"><ww:if test="."><ww:property value="."/></ww:if></ww:property>">
    <ww:property value="parameters['currentValue']">
        <ww:if test=".">
            <ww:iterator value=".">
                <option value="<ww:property value="./key"/>" selected="selected"><ww:property value="./key"/> - <ww:property value="./summary"/></option>
            </ww:iterator>
        </ww:if>
    </ww:property>
</select>
<div class="description"><ww:text name="'linkissue.picker.desc'"/></div>
<jsp:include page="/template/aui/formFieldIcon.jsp" />
<jsp:include page="/template/aui/formFieldError.jsp" />