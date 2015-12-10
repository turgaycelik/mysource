<ww:property value="/constantsManager/issueType(string('type'))">
<ww:component name="'issuetype'" template="constanticon.jsp">
  <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
  <ww:param name="'iconurl'" value="./string('iconurl')" />
  <ww:param name="'alt'"><ww:property value="/nameTranslation(.)" /></ww:param>
  <ww:param name="'title'"><ww:property value="/nameTranslation(.)"/> - <ww:property value="/descTranslation(.)" /></ww:param>
</ww:component>
</ww:property>
