<%@ page import="com.atlassian.jira.component.ComponentAccessor" %>
<%@ page import="com.atlassian.jira.util.BuildUtilsInfo" %>
<%@ page import="com.atlassian.jira.util.JiraUtils" %>
<%@ page import="com.atlassian.seraph.util.RedirectUtils" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%-- TODO: SEAN I think these can be removed now - or at least moved into the i18n JS stuff Spud added --%>
<%
    request.setAttribute("loginLink", RedirectUtils.getLinkLoginURL(request));
    request.setAttribute("isPublicMode", JiraUtils.isPublicMode());
%>

<fieldset class="hidden parameters">
    <input type="hidden" title="loggedInUser" value="<ww:property value="/loggedInUser/name"/>">
    <input type="hidden" title="ajaxTimeout" value="<ww:text name="'common.forms.ajax.timeout'"/>">
    <input type="hidden" title="JiraVersion" value="<%= ComponentAccessor.getComponent(BuildUtilsInfo.class).getVersion() %>" />
    <input type="hidden" title="ajaxUnauthorised" value="<ww:text name="'common.forms.ajax.unauthorised.alert'"/>">
    <input type="hidden" title="baseURL" value="<%=request.getScheme() + "://" +request.getServerName() + ':' + request.getServerPort() + request.getContextPath()%>">
    <input type="hidden" title="ajaxCommsError" value="<ww:text name="'common.forms.ajax.commserror'"/>">
    <input type="hidden" title="ajaxServerError" value="<ww:text name="'common.forms.ajax.servererror'"/>">
    <input type="hidden" title="ajaxErrorCloseDialog" value="<ww:text name="'common.forms.ajax.error.dialog'"/>">
    <input type="hidden" title="ajaxErrorDialogHeading" value="<ww:text name="'common.forms.ajax.error.dialog.heading'"/>">

    <input type="hidden" title="dirtyMessage" value="<ww:text name="'common.forms.dirty.message'"/>">
    <input type="hidden" title="dirtyDialogMessage" value="<ww:text name="'common.forms.dirty.dialog.message'"/>">
    <input type="hidden" title="keyType" value="<ww:text name="'keyboard.shortcuts.type'"/>">
    <input type="hidden" title="keyThen" value="<ww:text name="'keyboard.shortcuts.then'"/>">
    <input type="hidden" title="dblClickToExpand" value="<ww:text name="'tooltip.dblclick.expand'"/>">
    <input type="hidden" title="actions" value="<ww:text name="'common.words.actions'"/>">
    <input type="hidden" title="removeItem" value="<ww:text name="'admin.common.words.remove'"/>">
    <input type="hidden" title="workflow" value="<ww:text name="'opsbar.more.transitions'"/>">
    <input type="hidden" title="labelNew" value="<ww:text name="'label.new'"/>">
    <input type="hidden" title="issueActionsHint" value="<ww:text name="'issueactions.start.typing'"/>">
    <input type="hidden" title="closelink" value="<ww:text name="'admin.common.words.close'"/>">
    <input type="hidden" title="dotOperations" value="<ww:text name="'common.words.operations'"/>">
    <input type="hidden" title="dotLoading" value="<ww:text name="'common.concepts.loading'"/>">
    <input type="hidden" title="frotherSuggestions" value="<ww:text name="'common.words.suggestions'"/>">
    <input type="hidden" title="frotherNomatches" value="<ww:text name="'common.concepts.no.matches'"/>">
    <input type="hidden" title="multiselectVersionsError" value="<ww:text name="'jira.ajax.autocomplete.versions.error'"/>">
    <input type="hidden" title="multiselectComponentsError" value="<ww:text name="'jira.ajax.autocomplete.components.error'"/>">
    <input type="hidden" title="multiselectGenericError" value="<ww:text name="'jira.ajax.autocomplete.error'"/>">
</fieldset>
