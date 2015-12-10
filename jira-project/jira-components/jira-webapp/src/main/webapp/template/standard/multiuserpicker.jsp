<%@ taglib uri="webwork" prefix="ww" %>
<jsp:include page="/template/standard/controlheader.jsp" />

<script language="JavaScript">
    function openMultiWindow(element)
    {
        var vWinUsers = window.open('<%= request.getContextPath() %>/secure/popups/UserPickerBrowser.jspa?formName=jiraform&multiSelect=true&element=' + element, 'UserPicker', 'status=yes,resizable=yes,top=100,left=200,width=700,height=750,scrollbars=yes');
        vWinUsers.opener = self;
        vWinUsers.focus();
    }
</script>

<textarea <ww:property value="parameters['name']">
             <ww:if test=".">id="<ww:property value="."/>" name="<ww:property value="."/>"</ww:if>
             <ww:else>id="multiuserpicker" name="multiuserpicker"</ww:else>
          </ww:property>
          <ww:property value="parameters['cols']">
             <ww:if test=".">cols="<ww:property value="."/>"</ww:if>
             <ww:else>cols="40"</ww:else>
          </ww:property>
          <ww:property value="parameters['rows']">
             <ww:if test=".">rows="<ww:property value="."/>"</ww:if>
             <ww:else>rows="3"</ww:else>
          </ww:property>
          <ww:property value="parameters['style']">
             <ww:if test=".">style="<ww:property value="."/>"</ww:if>
             <ww:else>style="width: 30%;"</ww:else>
          </ww:property>
          <ww:property value="parameters['cssClass']">
            class="ajs-dirty-warning-exempt<ww:if test="."> <ww:property value="."/></ww:if>"
          </ww:property>
        ><ww:property value="parameters['nameValue']"/></textarea>

<ww:if test="hasPermission('pickusers') == true">
    <a href="javascript:openMultiWindow('<ww:property value="parameters['name']"><ww:if test="."><ww:property value="."/></ww:if><ww:else>multiuserpicker</ww:else></ww:property>');"><span class="aui-icon icon-userpicker" title="<ww:text name="'user.picker.select.users'"/>"></span></a>
</ww:if>
<ww:else>
    <span class="aui-icon icon-userpicker-disabled" title="<ww:text name="'user.picker.no.permission'"/>"></span>
</ww:else>

<jsp:include page="/template/standard/controlfooter.jsp" />
