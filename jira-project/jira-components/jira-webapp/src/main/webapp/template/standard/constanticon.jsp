<%@ taglib uri="webwork" prefix="ww" %>
<%--
PARAMETERS:
iconurl 	    - the URL for the icon
contextPath		- the the context path of the web app
alt		        - the value of the alt attribute of the img tag for the icon
title			- the value of the title attribute of the img tag for the icon

Note: The 'name' parameter of this template is used as it causes all sorts of webwork parsing problems. Use the alt parameter instead. 
--%>
<ww:if test="./parameters['iconurl']/startsWith('http://') == true || ./parameters['iconurl']/startsWith('https://') == true">
<img src="<ww:property value="./parameters['iconurl']" />" height="16" width="16" border="0" align="absmiddle" alt="<ww:property value="./parameters['alt']" escape="false" />" title="<ww:property value="./parameters['title']" escape="false" />">
</ww:if>
<ww:else>
<img src="<ww:property value="./parameters['contextPath']" /><ww:property value="./parameters['iconurl']" />" height="16" width="16" border="0" align="absmiddle" alt="<ww:property value="./parameters['alt']" escape="false" />" title="<ww:property value="./parameters['title']" escape="false"/>">
</ww:else>
