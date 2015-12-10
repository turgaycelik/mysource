<%@ page import="com.atlassian.jira.util.BrowserUtils"%>
<%@ page import="org.apache.commons.lang.StringUtils"%>
<%@ page import="org.apache.log4j.Logger" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>

<%--
PARAMETERS: (all are optional)
action 			- the URI to submit this form too
submitName		- the name of the submit button
submitId		- the id of the submit button (ALWAYS USE THIS!)
submitClassName - class name of submit button
cancelURI		- the location to redirect to for the cancel button (no cancel button if this isn't present)
leftButtons	    - any other buttons to put to the left of the submit button
buttons			- any other buttons to put next to the submit button
autoSelectFirst	- unless this is present and "false", the first element of the form will be selected automatically using JavaScript
title			- a title for this form (HTML)
notable		    - if this is specified, JIRA form will not output a border table (HTML)
width		    - the width of the border table (HTML)
multipart		- if this parameter is present, the form will be a multipart form
helpURL		    - the URL of a help link related to this form
helpURLFragment - the #fragment identifier to append to the helpURL. Value must include the #
columns         - the number of columns the underlying form will have
method          - the method of the form to submit (get or post)
messageFooter   - any HTML content you want to appear in the bottom table row - under the buttons
enableFormErrors - disable general errors displayed by hasErrorMessages
suppressAtlToken - disable the xsrf token, by default it is enabled
--%>
<decorator:usePage id="p" />

<%!
    private static final Logger log = Logger.getLogger("jiraform.jsp");
%>

<%
    // Set some defaults
    String columns = (p.isPropertySet("columns")) ? p.getProperty("columns") : "2";
    boolean displayGeneralErrors = (p.isPropertySet("enableFormErrors")) ? p.getBooleanProperty("enableFormErrors") : true;
    int columnsInt = Integer.parseInt(columns);
    request.setAttribute("modifierKey", BrowserUtils.getModifierKey());
%>

<% if (StringUtils.isNotBlank(p.getProperty("labelWidth"))) { %>
<style type="text/css"> .fieldLabelArea {width: <decorator:getProperty property="labelWidth" />;}</style>
<% } %>

<% if (p.isPropertySet("action")) { %>
<form action="<decorator:getProperty property="action" />" method="<decorator:getProperty property="method" default="post" />" name="<decorator:getProperty property="formName" default="jiraform" />" <% if (p.isPropertySet("onsubmit")) { %>onsubmit="<decorator:getProperty property="onsubmit"/>" <% } else { %>onsubmit="if (this.submitted) return false; this.submitted = true; return true"<% } %> <% if (p.isPropertySet("multipart")) { %> ENCTYPE="multipart/form-data"<% } %><% if (p.isPropertySet("autocomplete")) { %> autocomplete="<decorator:getProperty property="autocomplete"/>"<% } %> <% if (p.isPropertySet("class")) { %> class="<decorator:getProperty property="class"/>"<% } %>>
<% } %>
	<% if (!p.isPropertySet("notable")) { %>
	<table class="jiraform<% if (!p.isPropertySet("width") || (p.isPropertySet("width") && p.getProperty("width").equals("100%"))){%> maxWidth<%}%>" <% if (p.isPropertySet("jiraformId")) { %>id="<decorator:getProperty property="jiraformId" />"<%}%>
    <% if (p.isPropertySet("width") && !p.getProperty("width").equals("100%")){%>width="<decorator:getProperty property="width" />"<%}%>>
	<% } %>

	<% if (p.isPropertySet("title") && StringUtils.isNotBlank(p.getProperty("title"))) { %>
	<tr class="titlerow">
		<td colspan="<%=columns%>" class="jiraformheader">
        <% if (p.isPropertySet("pretitle")) { %> <decorator:getProperty property="pretitle" />
        <% } if (p.isPropertySet("localHelpAction")) {
            String action = p.getProperty("localHelpAction");
        %>
        <ww:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Links.helpLink'">
            <ww:param name="'isLocal'" value="true"/>
            <ww:param name="'url'"><%=action%></ww:param>
            <ww:param name="'title'">this</ww:param>
            <ww:param name="'extraAttributes'">style="float:right;"</ww:param>
        </ww:soy>
        <%
        }
        if (p.isPropertySet("helpURL")) {
            String helpUrl = "'" + p.getProperty("helpURL") + "'";
            String helpURLFragment = "";
            if (p.isPropertySet("helpURLFragment"))
                helpURLFragment = p.getProperty("helpURLFragment"); %>
            <ww:component template="help.jsp" name="<%= helpUrl %>" >
                <ww:param name="'helpURLFragment'"><%= helpURLFragment %></ww:param>
            </ww:component>
        <% } %>
        <h3 class="formtitle"><decorator:getProperty property="title" /><% if (p.isPropertySet("wizard") && "true".equalsIgnoreCase(p.getProperty("wizard"))) { %> (<ww:text name="'admin.common.phrases.step.x.of.x'">
           <ww:param name="'value0'"><ww:property value="/currentStep" /></ww:param>
           <ww:param name="'value1'"><ww:property value="/totalSteps" /></ww:param>
        </ww:text>)<%}%></h3>
        <% if (p.isPropertySet("postTitle") && StringUtils.isNotBlank(p.getProperty("postTitle"))) { %>
            <decorator:getProperty property="postTitle"/>
        <%}%>
        </td>
	</tr>
	<% } %>
	<% if (StringUtils.isNotBlank(p.getProperty("description"))) { %>
	<tr class="descriptionrow">
		<td colspan="<%=columns%>" class="jiraformheader">
            <div class="desc-wrap"><decorator:getProperty property="description" /></div>
        </td>
	</tr>
	<% } %>
	<% if (StringUtils.isNotBlank(p.getProperty("instructions"))) { %>
	<tr>
		<td colspan="<%=columns%>" class="instructions">
        <decorator:getProperty property="instructions" />
        </td>
	</tr>
	<% } %>

	<%-- formwide error messages --%>
	<% if (displayGeneralErrors) { %>
    <ww:if test="hasErrorMessages == 'true'">
    <tr>
		<td colspan="<%=columns%>">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">error</aui:param>
                <aui:param name="'titleText'"><ww:text name="'panel.errors'"/></aui:param>
                <aui:param name="'messageHtml'">
                    <ul>
                        <ww:iterator value="flushedErrorMessages">
                            <li><ww:property /></li>
                        </ww:iterator>
                    </ul>
                </aui:param>
            </aui:component>
		</td>
    </tr>
    </ww:if>
     <% } %>
    <tr class="hidden"><td colspan="<%=columns%>"></td></tr>
	<decorator:body />

<% if (p.isPropertySet("wizard") && "true".equalsIgnoreCase(p.getProperty("wizard"))) { %>
	<tr class="buttons-container wizard-buttons">
		<td colspan="<%=columns%>">
            <div class="wizardInfo secondary-text">
                <decorator:getProperty property="title" /><br />
                <ww:text name="'admin.common.phrases.step.x.of.x'">
                    <ww:param name="'value0'"><ww:property value="/currentStep" /></ww:param>
                    <ww:param name="'value1'"><ww:property value="/totalSteps" /></ww:param>
                </ww:text>
            </div>
            <div class="buttons-container">
                <div class="buttons">
                <ww:if test="/currentStep == /totalSteps">
                    <% if (!p.isPropertySet("finishButton"))
                       { %>
                        <ww:property value="text('common.forms.finish')" id="nextLabel" />

                    <% }
                       else
                       {
                           request.setAttribute("nextLabel", p.getProperty("finishButton"));
                       }
                    %>
                </ww:if>
                <ww:else>
                    <ww:property value="text('common.forms.next.with.arrows')" id="nextLabel"   />
                </ww:else>

                    <input id="currentStep" type="hidden" name="currentStep" value="<ww:property value="/currentStep" />" />
                    <input class="hiddenButton" id="hiddennextButton"  type="submit" name="nextBtn" value="<ww:property value="@nextLabel" />" accesskey="N" />

                    <input class="aui-button" id="previousButton" type="submit" name="previousBtn" value="<ww:text name="'common.forms.previous.with.arrows'"/>" accesskey="P" <ww:if test="/currentStep == 1">disabled="disabled"</ww:if> />
                    <input class="aui-button" id="nextButton"  type="submit" name="nextBtn" value="<ww:property value="@nextLabel" />" accesskey="<ww:text name="'common.forms.submit.accesskey'"/>"
                           title="<ww:text name="'common.forms.submit.tooltip'">
                            <ww:param name="'value0'"><ww:text name="'common.forms.submit.accesskey'"/></ww:param>
                            <ww:param name="'value1'"><ww:property value="@modifierKey"/></ww:param>
                        </ww:text>" />

                    <% if (p.isPropertySet("buttons")) { %>
                        <decorator:getProperty property="buttons" />
                    <% } %>

                    <% if (p.isPropertySet("cancelURI")) { %>
                        <a href="<ww:if test="/returnUrlForCancelLink != null"><ww:if test="/returnUrlForCancelLink/startsWith('/') == true"><%= request.getContextPath() %></ww:if><ww:property value="/returnUrlForCancelLink" /></ww:if><ww:else><decorator:getProperty property="cancelURI" /></ww:else>"
                           id="cancelButton"
                           class="aui-button aui-button-link"
                           accesskey="<ww:text name="'common.forms.cancel.accesskey'" />"
                           title="<ww:property value="text('common.forms.cancel')"/> (<ww:property value="@modifierKey"/> + <ww:text name="'common.forms.cancel.accesskey'" />)"
                           name="<decorator:getProperty property="cancelURI" />"><ww:property value="text('common.forms.cancel')"/>
                        </a>
                    <% } %>
                </div>
            </div>
		</td>
	</tr>
<% } else if (p.isPropertySet("cancelURI") || p.isPropertySet("buttons") || p.isPropertySet("submitName") || p.isPropertySet("leftButtons")){%>
	<tr class="buttons-container">
    <%
        if (columnsInt > 1)
        {
    %>
        <td class="jiraformfooter">&nbsp;</td>
        <td colspan="<%=columnsInt - 1%>">
    <%
        } else {
    %>
        <td class="jiraformfooter" >
    <%
        }
    %>
            <div class="buttons-container">
                <div class="buttons">
                <% if (p.isPropertySet("leftButtons")) { %>
                    <decorator:getProperty property="leftButtons" />
                <% } %>

                <% if (StringUtils.isNotBlank(p.getProperty("submitName"))) { %>
                    <input type="submit" name="<decorator:getProperty property="submitName" />" value="<decorator:getProperty property="submitName" />"
                       <% if (StringUtils.isBlank(p.getProperty("submitId"))) { %>
                            id="<decorator:getProperty property="submitName" />"
                       <%       log.info("i18n problem: id defined by localised value: " + p.getProperty("submitName"));
                           } else {%>
                            id="<decorator:getProperty property="submitId" />"
                        <% } %>

                    <% if (StringUtils.isBlank(p.getProperty("submitAccessKey"))) { %>
                       accesskey="<ww:text name="'common.forms.submit.accesskey'"/>"
                       title="<ww:text name="'common.forms.submit.tooltip'">
                       <ww:param name="'value0'"><ww:text name="'common.forms.submit.accesskey'"/></ww:param>
                       <ww:param name="'value1'"><ww:property value="@modifierKey"/></ww:param>
                       </ww:text>"
                    <% } else {%>
                       accesskey="<decorator:getProperty property="submitName" />"
                       title="<decorator:getProperty property="submitName" /> (<ww:property value="@modifierKey"/> + <decorator:getProperty property="submitAccessKey" />)"
                    <%}%>
                        <% if (p.isPropertySet("onclicksubmit")) { %>onclick="<decorator:getProperty property="onclicksubmit" />"<% } %>
                        class="aui-button <% if (p.isPropertySet("submitClassName")) { %><decorator:getProperty property="submitClassName" /><% } %>"
                        />
                <% } %>

                <% if (p.isPropertySet("buttons")) { %>
                    <decorator:getProperty property="buttons" />
                <% } %>

                <% if (p.isPropertySet("cancelURI")) { %>
                    <a href="<ww:if test="/returnUrlForCancelLink != null"><ww:if test="/returnUrlForCancelLink/startsWith('/') == true"><%= request.getContextPath() %></ww:if><ww:property value="/returnUrlForCancelLink" /></ww:if><ww:else><decorator:getProperty property="cancelURI" /></ww:else>"
                       id="cancelButton"
                       class="aui-button aui-button-link"
                       accesskey="<ww:text name="'common.forms.cancel.accesskey'" />"
                       title="<ww:property value="text('common.forms.cancel')"/> (<ww:property value="@modifierKey"/> + <ww:text name="'common.forms.cancel.accesskey'" />)"
                       name="<decorator:getProperty property="cancelURI" />"><ww:property value="text('common.forms.cancel')"/>
                    </a>
                <% } %>
                </div>
            </div>
		</td>
	</tr>
<% } %>
    <%-- Forward the return url --%>
<ww:if test="/returnUrl != null">
    <ww:component name="'returnUrl'" template="hidden.jsp" theme="'single'"  />
</ww:if>
        <% if (p.isPropertySet("messageFooter")) { %>
            <tr>
                <td colspan="<%=columns%>">
                      <decorator:getProperty property="messageFooter" />
                </td>
            </tr>
        <% } %>
    <% if (!p.isPropertySet("notable")) { %>
	</table>
	<% } %>
<% if (p.isPropertySet("action")) { %>
<% if (!p.isPropertySet("suppressAtlToken")) { %>
    <ww:component name="'atl_token'" value="/xsrfToken" template="hidden.jsp"/>
<% } %>
</form>
	<%--
	This enables the first element of the form to be automatically selected.

	Always on unless autoSelectFirst=false
	--%>
	<% if (!p.isPropertySet("autoSelectFirst") || !p.getProperty("autoSelectFirst").equals("false")) { %>
    <script language="javascript" type="text/javascript">
        jQuery(function () {
            AJS.$("form[name='<decorator:getProperty property="formName" default="jiraform" />'] :input:visible:first").focus();
        });
	</script>
	<% } %>
<% } %>
