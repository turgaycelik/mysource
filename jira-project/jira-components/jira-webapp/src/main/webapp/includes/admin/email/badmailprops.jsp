<%@ page import="com.atlassian.jira.web.util.HelpUtil" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<% HelpUtil helpUtil = new HelpUtil();
   HelpUtil.HelpPath helpPath = helpUtil.getHelpPath("decodeparameters");
%>
<aui:component template="auimessage.jsp" theme="'aui'">
    <aui:param name="'messageType'">warning</aui:param>
    <aui:param name="'messageHtml'">
        <p>
            <ww:text name="'admin.mailservers.mail.bad.props'">
                <ww:param name="'value1'"><a href="<%=helpPath.getUrl()%>"/></ww:param>
                <ww:param name="'value2'"></a></ww:param>
            </ww:text>
        </p>
    </aui:param>
</aui:component>