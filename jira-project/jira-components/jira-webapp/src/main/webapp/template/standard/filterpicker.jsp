<%--
  --
  -- Required Parameters:
  --   * label     - The description that will be used to identfy the control.
  --   * name      - The name of the attribute to put and pull the result from.

  -- Optional Parameters:
  --   * value     - id of the currently selected filter (optional)
  --   * filtername     - name of the currently selected filter (optional)

--%>

<%@ taglib uri="webwork" prefix="ww" %>
<jsp:include page="/template/standard/controlheader.jsp"/>
<div style="display:none;">
<input name="<ww:property value="parameters['name']"/>" id="filter_<ww:property value="parameters['name']"/>_id" type="text" value="<ww:property value="parameters['value']"/>"/>
</div>
<span id="filter_<ww:property value="parameters['name']"/>_name"><ww:property value="parameters['filtername']"/></span>
<a href="#" id="filter_<ww:property value="parameters['name']"/>_button" onclick="window.open('<%= request.getContextPath() %>/secure/FilterPickerPopup.jspa?field=<ww:property value="parameters['name']"/>','filter_<ww:property value="parameters['name']"/>_window', 'width=800, height=500, resizable, scrollbars=yes').focus(); return false;">
    <ww:if test="parameters['value'] == null || parameters['value'] == ''">
        <ww:text name="'popups.filterpicker.selectfilter'"/>
    </ww:if>
    <ww:else>
        <ww:text name="'popups.filterpicker.changefilter'"/>
    </ww:else>
</a>
<jsp:include page="/template/standard/controlfooter.jsp"/>
