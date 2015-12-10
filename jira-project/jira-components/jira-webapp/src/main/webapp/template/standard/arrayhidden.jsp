<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<tr class="hidden"><td>
<ww:iterator value="parameters['nameValue']">
<ww:if test=".">
<input type="hidden"
       id="<ww:property value="parameters['name']"/>"
       name="<ww:property value="parameters['name']"/>"
       value="<ww:property value="."/>"
/>
</ww:if>
</ww:iterator>
</td></tr>
