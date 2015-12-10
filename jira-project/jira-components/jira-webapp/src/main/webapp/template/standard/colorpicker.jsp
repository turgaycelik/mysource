<%--
  -- WebWork, Web Application Framework
  --
  -- Distributable under LGPL license.
  -- See terms of license at opensource.org
  --
  --
  -- textlabel.jsp
  --
  -- Required Parameters:
  --   * label      - The description that will be used to identfy the control.
  --   * name       - The name of the attribute to put and pull the result from.
  --
  -- Optional Parameters:
  --   * labelposition   - determines were the label will be place in relation
  --                       to the control.  Default is to the left of the control.
  --
  --%>
<%@ taglib uri="webwork" prefix="ww" %>
<jsp:include page="/template/standard/controlheader.jsp"/>

<table cellpadding="0" cellspacing="0" border="0">
    <tr>
        <td style="padding:0px">
            <input type="text"
                   name="<ww:property value="parameters['name']"/>"
            <ww:property value="parameters['size']">
                   <ww:if test=".">size="<ww:property value="."/>"
            </ww:if>
            </ww:property>
            <ww:property value="parameters['maxlength']">
                   <ww:if test=".">maxlength="<ww:property value="."/>"
            </ww:if>
            </ww:property>
            <ww:property value="parameters['nameValue']">
                   <ww:if test=".">value="<ww:property value="."/>"
            </ww:if>
            </ww:property>
            <ww:property value="parameters['disabled']">
                   <ww:if test="{parameters['disabled']}">DISABLED</ww:if>
            </ww:property>
            <ww:property value="parameters['readonly']">
                   <ww:if test="{parameters['readonly']}">READONLY</ww:if>
            </ww:property>
            <ww:property value="parameters['onkeyup']">
                   <ww:if test=".">onkeyup="<ww:property value="."/>"
            </ww:if>
            </ww:property>
            <ww:property value="parameters['tabindex']">
                   <ww:if test=".">tabindex="<ww:property value="."/>"
            </ww:if>
            </ww:property>
            <ww:property value="parameters['onchange']">
                   <ww:if test=".">onchange="<ww:property value="."/>"
            </ww:if>
            </ww:property>
            <ww:property value="parameters['style']">
                   <ww:if test=".">style="<ww:property value="."/>"
            </ww:if>
            </ww:property>
                    >
        </td>
        <td style="padding:0px">&nbsp;</td>
        <td style="padding:0px">
            <table cellpadding="0" cellspacing="1" border="1">
                <td id="<ww:property value="parameters['name']" />-rep" style="padding:0px;height:18px;width:18px;background-color:<ww:property value="parameters['nameValue']"/>">
                    <a id="colorpicker-<ww:property value="parameters['name']" />" href="#"
                       onClick="window.open('<%= request.getContextPath() %>/secure/popups/<ww:url value="'colorpicker.jsp'"><ww:param name="'defaultColor'" value="parameters['nameValue']"/><ww:param name="'element'" value="parameters['name']" /></ww:url>', 'colorpicker', 'menubar=yes,location=no,personalbar=no,scrollbar=yes,width=580,height=300,resizable');">
                        <img src="<%= request.getContextPath() %>/images/border/spacer.gif" width="15" height="15" border="0">
                    </a></td>
            </table>
        </td>
    </tr>
</table>

<%@ include file="/template/standard/controlfooter.jsp" %>

