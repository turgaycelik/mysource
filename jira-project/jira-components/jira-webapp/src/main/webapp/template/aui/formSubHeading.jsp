<%--

Required Parameters:
    * text                      - The text to be used in the heading

Optional Parameters:
    * cssClass                  - CSS classes added to the heading (class doesn't display if left empty)

Code Example:
    <aui:component theme="'aui'" template="formHeading.jsp">
        <aui:param name="'text'"><ww:text name="'my.custom.form.subheading.text'"/></aui:param>
        <aui:param name="'cssClass'">custom</aui:param>
    </aui:component>

Notes:
    See http://confluence.atlassian.com/display/AUI/Forms for more information

--%>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<h3<ww:property value="parameters['cssClass']"><ww:if test=".">class="<ww:property value="."/>"</ww:if></ww:property>><ww:property value="parameters['text']"/></h3>