<%@ taglib uri="webwork" prefix="ww" %>
<ww:property value="/componentManager/fileIconBean/fileIcon(parameters['filename'], parameters['mimetype'])">
    <ww:if test=". != null">
        <img src="<%= request.getContextPath() %>/images/icons/attach/<ww:property value="icon" />" height="16" width="16"
             border="0" alt="<ww:property value="altText" />">        
        </ww:if>
    <ww:else>
        <img src="<%= request.getContextPath() %>/images/icons/attach/file.gif" height="16" width="16" border="0" alt="File">
    </ww:else>
</ww:property>
