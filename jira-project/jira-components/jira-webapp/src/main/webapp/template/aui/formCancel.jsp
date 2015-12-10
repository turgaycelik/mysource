<%--

Required Parameters:
    * cancelLinkText            - i18n text that shows on the Cancel link
    * cancelLinkURI             - Path used for the Cancel link

Optional Parameters:
    * cancelLinkCssClass        - CSS class added to the cancel link (class doesn't display if left empty)

Note:
    The ID of the link is hardcoded to inherit the form ID and then add "cancel", eg.  id="custom-form-cancel"
    The title is also hardcoded to grab the default AUI form cancel text

Code Example:
    <aui:component id="'custom-cancel'" theme="'aui'" template="formCancel.jsp">
        <aui:param name="'cancelLinkText'"><ww:text name="'AUI.form.cancel.link.text'"/></aui:param>
        <aui:param name="'cancelLinkURI'">/browse/HSP-1</aui:param>
        <aui:param name="'cancelLinkCssClass'">class-1 class-2</aui:param>
    </aui:component>

--%>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<a
    <ww:if test="parameters['cancelLinkHideAccessKey'] != 'true'">
    accesskey="<ww:text name="'AUI.form.cancel.link.accesskey'" />"
    </ww:if>
    <ww:property value="parameters['cancelLinkCssClass']">
        <ww:if test=".">class="aui-button aui-button-link cancel <ww:property value="."/>"</ww:if>
        <ww:else>class="aui-button aui-button-link cancel"</ww:else>
    </ww:property>
    href="<ww:if test="/returnUrlForCancelLink != null"><ww:if test="/returnUrlForCancelLink/startsWith('/') == true"><%= request.getContextPath() %></ww:if><ww:property value="/returnUrlForCancelLink" /></ww:if><ww:else><ww:property value="parameters['cancelLinkURI']"><ww:if test="."><ww:property value="." escape="false"/></ww:if></ww:property></ww:else>"
    id="<ww:property value="@jira.sitemesh.decorator.computed.id" />cancel"
    title="<ww:text name="'AUI.form.cancel.link.tooltip'"><ww:param name="'value0'"><ww:text name="'AUI.form.cancel.link.accesskey'"/></ww:param><ww:param name="'value1'"><ww:property value="@modifierKey"/></ww:param></ww:text>"
><ww:property value="parameters['cancelLinkText']"><ww:if test="."><ww:property value="."/></ww:if><ww:else><ww:text name="'AUI.form.cancel.link.text'"/></ww:else></ww:property></a>