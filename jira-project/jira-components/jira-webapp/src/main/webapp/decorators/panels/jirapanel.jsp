<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>

<%--
PARAMETERS: (all are optional)
title			- a title for this form (HTML)
width		    - the width of the border table (HTML)
helpURL		    - the URL of a help link related to this panel
--%>
<decorator:usePage id="p" />

<table class="jiraform jirapanel <decorator:getProperty property="class" /><% if (!p.isPropertySet("width") || (p.isPropertySet("width") && p.getProperty("width").equals("100%"))){%> maxWidth<%}%>"
       <% if (p.isPropertySet("width") && !p.getProperty("width").equals("100%")){%>width="<decorator:getProperty property="width" />"<%}%>
>

<% if (p.isPropertySet("title") && StringUtils.isNotBlank(p.getProperty("title"))) { %>
<tr>
    <td class="jiraformheader">
    <%@ include file="/includes/decorators/helplink.jsp" %>

    <h3 class="formtitle"><decorator:getProperty property="title" /></h3>
        <% if (p.isPropertySet("postTitle") && StringUtils.isNotBlank(p.getProperty("postTitle"))) { %>
            <decorator:getProperty property="postTitle"/>
        <%}%>
    </td>
</tr>
<% } %>
<% if (p.isPropertySet("description") && StringUtils.isNotBlank(p.getProperty("description"))) { %>
<tr>
    <td class="jiraformheader"><decorator:getProperty property="description" /></td>
</tr>
<% } %>
	<% if (p.isPropertySet("instructions") && StringUtils.isNotBlank(p.getProperty("instructions"))) { %>
	<tr>
		<td class="instructions">
        <decorator:getProperty property="instructions" />
        </td>
	</tr>
	<% } %>
<%-- error messages --%>
<ww:if test="hasErrorMessages == 'true'">
<tr>
    <td>
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

    <tr>
    <td class="jiraformbody">
        <decorator:body />
    </td>
    </tr>
<% if (p.isPropertySet("footerHtml")) { %>
	<tr>
		<td class="darkFooter">
            <decorator:getProperty property="footerHtml" />
		</td>
	</tr>
<% }  else if (p.isPropertySet("cancelURI") || p.isPropertySet("buttons")){%>
    <tr>
        <td class="fullyCentered jiraformfooter" >
			<% if (p.isPropertySet("buttons")) { %>
                <decorator:getProperty property="buttons" />
			<% } %>
			<% if (p.isPropertySet("cancelURI")) { %>
                <input id="cancelButton"  type="button"
                       accesskey="<ww:text name="'common.forms.cancel.accesskey'" />"
                       title="<ww:property value="text('common.forms.cancel')"/> (Alt + <ww:text name="'common.forms.cancel.accesskey'" />)"
                       name="<decorator:getProperty property="cancelURI" />"
                       value="<ww:property value="text('common.forms.cancel')"/>"
                       onclick="location.href='<ww:if test="/returnUrl != null"><ww:property value="/returnUrl" /></ww:if><ww:else><decorator:getProperty property="cancelURI" /></ww:else>'"
                />
			<% } %>
        </td>
    </tr>
<% } %>

</table>
