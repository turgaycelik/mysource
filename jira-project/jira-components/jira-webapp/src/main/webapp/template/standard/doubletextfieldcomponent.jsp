<%@ page import="webwork.action.CoreActionContext"%>
<%@ taglib uri="webwork" prefix="ww" %>
<%-- Modified controlheader.jsp: --%>
<jsp:include page="/template/standard/duedatecontrolheader.jsp" />
<%-- End modified controlheader.jsp: --%>
<table border="0" width="100%"
    <ww:property value="parameters['style']">
        <ww:if test=".">style="<ww:property value="."/>"</ww:if>
    </ww:property>
cellspacing="0" cellpadding="0">
    <tr valign="top">
        <td>
            <span class="label">
                <ww:property value="parameters['labelPrevious']">
                    <ww:if test="."><ww:property value="."/></ww:if>
                </ww:property>
            </span>
        </td>
        <td>
            <jsp:include page="/template/standard/duedatenextfield.jsp" />
        </td>
        <td>
            <span class="label">
                <ww:property value="parameters['labelNext']">
                    <ww:if test="."><ww:property value="."/></ww:if>
                </ww:property>
            </span>
        </td>
        <td>
            <jsp:include page="/template/standard/duedatepreviousfield.jsp" />
        </td>
        <td align="right">
            <%
                Object value = CoreActionContext.getValueStack().findValue("parameters['firstLineInclude']");
                if (value != null) {
                String include = value.toString();
            %>
            <jsp:include page="<%=include%>"/>
            <%}%>
        </td>
        <%-- Include the help icon on the same line as the text components. --%>
        <td><ww:property value="parameters['helpURL']">
            <ww:if test=".">
            <ww:component template="help.jsp" name="." >
                <ww:param name="'noalign'" value="true" />
                <ww:param name="'helpURLFragment'" value="parameters['helpURLFragment']"/>
            </ww:component>
			</ww:if>
         </ww:property></td>
    </tr>
 </table>

 <%-- include the control footer here, as we don't want a <br> before the description as in the normal control footer --%>
        <ww:property value="parameters['description']">
            <ww:if test=".">
				<div class="description"><ww:property value="." escape="false" /></div>
			</ww:if>
         </ww:property>
	</td>
</tr>