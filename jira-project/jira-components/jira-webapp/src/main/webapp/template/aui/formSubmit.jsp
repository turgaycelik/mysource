<%@ page import="com.atlassian.jira.util.BrowserUtils" %>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%
    request.setAttribute("modifierKey", BrowserUtils.getModifierKey());
%>
<%--

Required Parameters:
    * submitButtonText          - i18n text that shows on the button and used as the value in the POST/GET

Optional Parameters:
    * submitButtonCssClass      - CSS classes added to class="aui-button"
    * submitButtonDisabled      - If 'true', disable the button, otherwise enable it

Code Example:
    <aui:component theme="'aui'" template="formSubmit.jsp">
        <aui:param name="'submitButtonText'"><ww:text name="'AUI.form.submit.button.text'"/></aui:param>
        <aui:param name="'submitButtonCssClass'">custom</aui:param>
    </aui:component>

Note:
    This component is specifically coded to be the single, primary submit button for a form. If you have multiple
    buttons or need to change the accesskey/id/name/title/type of the button hide this one on the main form (set the
    hideDefaultButtons to true on the auiform decorator) and then add custom buttons using the formButton component

--%>
<input
    <ww:if test="parameters['submitButtonHideAccessKey'] != 'true'">
        accesskey="<ww:text name="'AUI.form.submit.button.accesskey'"/>"
    </ww:if>
    <ww:property value="parameters['submitButtonCssClass']">
        <ww:if test=".">class="aui-button <ww:property value="."/>"</ww:if>
        <ww:else>class="aui-button"</ww:else>
    </ww:property>
    <ww:if test="parameters['id']">
        id="<ww:property value="parameters['id']" />"
    </ww:if>
    <ww:else>
        id="<ww:property value="@jira.sitemesh.decorator.computed.id" />submit"
    </ww:else>
    <ww:if test="parameters['submitButtonDisabled'] == 'true'">
        disabled="disabled"
    </ww:if>
    name="<ww:property value="parameters['submitButtonName']" />"
    title="<ww:text name="'AUI.form.submit.button.tooltip'"><ww:param name="'value0'"><ww:text name="'AUI.form.submit.button.accesskey'"/></ww:param><ww:param name="'value1'"><ww:property value="@modifierKey"/></ww:param></ww:text>"
    type="submit"
    <ww:property value="parameters['submitButtonText']">
        <ww:if test=".">value="<ww:property value="."/>"</ww:if>
        <ww:else><ww:text name="'AUI.form.submit.button.text'"/></ww:else>
    </ww:property>
    />
