<%@ page import="com.atlassian.jira.util.BrowserUtils" %>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="decorator" uri="sitemesh-decorator" %>
<%--

Required Attributes:
    * name                                  - must be specified in order to reference the correct decorator
    * id                                    - inherited by all controls within the form. No spaces, all lowercase,
                                                hyphens instead of underscores, must start with a-z

Required Paramaters:
    * action                                - ACTION attribute of the form

Optional Parameters:
    * cssClass                              - CSS classes added after "aui". No spaces, all lowercase, hyphens
                                                instead of underscores, must start with a-z)
    * method (default: post)                - METHOD attribute of the form
    * isMultipart (bool)                    - sets ENCTYPE to enctype="multpart/form-data"
    * useCustomButtons (bool)               - Hides the default div.buttons
                                                NOTE: If useCustomButtons is TRUE you must then specify buttons
                                                      manually using the aui/formSubmit and aui/formCancel components
    * submitButtonText (see notes below)    - i18n Submit button text (absence removes the button)
    * submitButtonName (see notes below)    - the Name attribute of the submit button
    * cancelLinkURI (see notes below)       - Cancel link href (absence removes the link)
    * cancelLinkText                        - i18n Cancel link text (displays if cancelLinkURI is set - defaults to 'AUI.form.cancel.link.text')
    * showHint                              - show JIRA usage hint next to the buttons

Notes:
    You must supply either a submitButtonText or cancelLinkURI. Omitting both will result in a form without buttons.

Code Example:

    Standard form
    -------------
    <page:applyDecorator id="create-user" name="auiform">
        <page:param name="action">CustomFormAction.jspa</page:param>
        <page:param name="submitButtonName">Save</page:param>
        <page:param name="submitButtonText"><ww:text name="'AUI.form.submit.button.text'"/></page:param>
        <page:param name="cancelLinkURI">/browse/HSP-1</page:param>
        ...
    </page:applyDecorator>

    Advanced form
    -------------
    <page:applyDecorator id="create-user" name="auiform">
        <page:param name="action">CustomFormAction.jspa</page:param>
        <page:param name="cssClass">class-1 class-2</page:param>
        <page:param name="method">get</page:param>
        <page:param name="isMultipart">true</page:param>
        <page:param name="useCustomButtons">true</page:param>
        ...
    </page:applyDecorator>

Notes:
    See http://confluence.atlassian.com/display/AUI/Forms for more information

--%>
<decorator:usePage id="p" />
<% request.setAttribute("modifierKey", BrowserUtils.getModifierKey()); %>
<form action="<decorator:getProperty property="action" />"
      class="aui<% if (p.isPropertySet("cssClass")) { %> <decorator:getProperty property="cssClass" /><% } %>"
      <% if (p.getBooleanProperty("isMultipart")) { %>enctype="multipart/form-data"<% } %>
      <% if (p.isPropertySet("id")) { %>id="<decorator:getProperty property="id" />"<% } %>
      method="<decorator:getProperty property="method" default="post" />">
    <div class="form-body">
        <% boolean displayGeneralErrors = (p.isPropertySet("enableFormErrors")) ? p.getBooleanProperty("enableFormErrors") : true; %>
        <% if (displayGeneralErrors) { %>
        <ww:if test="hasErrorMessages == 'true'">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">error</aui:param>
                <aui:param name="'messageHtml'">
                    <ww:iterator value="flushedErrorMessages">
                        <p><ww:property /></p>
                    </ww:iterator>
                </aui:param>
            </aui:component>
        </ww:if>
        <% } %>
        <decorator:body />
        <ww:if test="/returnUrl != null">
            <ww:component name="'returnUrl'" template="hidden.jsp" theme="'aui'"  />
        </ww:if>
        <% if (p.getBooleanProperty("hideToken") || !p.isPropertySet("hideToken")) { %><ww:component name="'atl_token'" template="hidden.jsp" theme="'aui'" value="/xsrfToken" /><% } %>
    </div>
    <% if (!p.getBooleanProperty("useCustomButtons") && (p.isPropertySet("submitButtonText") || p.isPropertySet("cancelLinkURI"))) { %>
    <div class="buttons-container form-footer">
        <div class="buttons">
            <% if (p.isPropertySet("submitButtonText")) { %>
                <aui:component template="formSubmit.jsp" theme="'aui'">
                    <aui:param name="'submitButtonName'"><decorator:getProperty property="submitButtonName" /></aui:param>
                    <aui:param name="'submitButtonText'"><decorator:getProperty property="submitButtonText" /></aui:param>
                    <aui:param name="'submitButtonDisabled'"><decorator:getProperty property="submitButtonDisabled" /></aui:param>
                </aui:component>
            <% } %>
            <% if (p.isPropertySet("cancelLinkURI")) { %>
                <aui:component template="formCancel.jsp" theme="'aui'">
                    <aui:param name="'cancelLinkText'"><decorator:getProperty property="cancelLinkText" /></aui:param>
                    <aui:param name="'cancelLinkURI'"><decorator:getProperty property="cancelLinkURI" /></aui:param>
                    <aui:param name="'cancelLinkCssClass'"><decorator:getProperty property="cancelLinkCssClass" /></aui:param>
                </aui:component>
            <% } %>
        </div>
        <% if (p.getBooleanProperty("showHint")) { %>
        <ww:if test="/inlineDialogMode == true">
            <aui:component template="hint.jsp" theme="'aui'" >
                <% if (p.isPropertySet("hint")) { %>
                <aui:param name="'hint'"><decorator:getProperty property="hint" /></aui:param>
                <% } %>
                <% if (p.isPropertySet("hintTooltip")) { %>
                <aui:param name="'tooltip'"><decorator:getProperty property="hintTooltip" /></aui:param>
                <% } %>
                <% if (p.isPropertySet("hideHintLabel")) { %>
                <aui:param name="'hideLabel'"><decorator:getProperty property="hideHintLabel" /></aui:param>
                <% } %>
            </aui:component>
        </ww:if>
        <% } %>
    </div>
    <% } %>
</form> <!-- // .aui<% if (p.isPropertySet("cssClass")) { %>.<decorator:getProperty property="cssClass" /><% } %><% if (p.isPropertySet("id")) { %> #<decorator:getProperty property="id" /><% } %> -->
