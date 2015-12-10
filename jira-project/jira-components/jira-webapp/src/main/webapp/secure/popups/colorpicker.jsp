
<%
   //File gratiously provided by JiveSoftware.
%>

<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.util.I18nHelper"%>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager"%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%
    WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
    webResourceManager.requireResource("jira.webresources:lookandfeel");
    I18nHelper i18nBean = ComponentManager.getInstance().getJiraAuthenticationContext().getI18nHelper();
%>
<%! // global vars, methods
        static final String[] hex = {"00", "33", "66", "99", "cc", "ff"};
        static final String[] bigHex = {"000000","111111","222222","333333","444444","555555","666666","777777","888888","999999","aaaaaa","bbbbbb","cccccc","dddddd","eeeeee","ffffff"};
%>

<p>


<ul id="colorpicker-params" style="display:none">
    <li class="defaultcolor"><ww:property value="$defaultColor"/></li>
    <li class="openerelem"><ww:property value="$element" /></li>
</ul>

<font size="-1">Click to choose a color.</font>

<p>
<form id="picker" class="ajs-dirty-warning-exempt">
<input type="hidden" name="formAction" value="">
<table cellpadding="0" cellspacing="1" border="1" align="center">
<%  for (int i=0; i<hex.length; i++) { %>
<tr>
<%      for (int j=0; j<hex.length; j++) { %>

<%          for (int k=0; k<hex.length; k++) { %>
    <td bgcolor="#<%= hex[i] %><%= hex[j] %><%= hex[k] %>"
     ><a class="colorpicker-option" href="#" title="#<%= hex[i] %><%= hex[j] %><%= hex[k] %>"
     ><img src="<%= request.getContextPath() %>/images/border/spacer.gif" width="10" height="15" alt="#<%= hex[i] %><%= hex[j] %><%= hex[k] %>" border="0"></a></td>
<%          } %>

<%      } %>
</tr>
<%  } %>
<tr>
    <td colspan="<%= hex.length * hex.length %>" align="center">
    <table cellpadding="0" cellspacing="1" border="1" align="center" width="100%">
    <tr>
<%  for (int i=0; i<bigHex.length; i++) { %>
    <td bgcolor="#<%= bigHex[i] %>"
     ><a class="colorpicker-option" href="#" title="#<%= bigHex[i] %>"
     ><img src="<%= request.getContextPath() %>/images/border/spacer.gif" width="12" height="15" alt="#<%= bigHex[i] %>" border="0"></a></td>
<%  } %>
    </tr>
    </table>
    </td>
</tr>
<tr>
    <td colspan="<%= hex.length * hex.length %>">
    <table width="100%">
    <tr>
    	<td><input type="text" size="10" id="colorVal" name="colorVal" value="<ww:property value='$defaultColor'/>"></td>
    	<td align="right">

             <input type="submit" class="colorpicker-ok aui-button" value="<%=i18nBean.getText("admin.common.words.ok")%>" name="<%=i18nBean.getText("admin.common.words.ok")%>">
             <input type="submit" class="colorpicker-cancel aui-button" value="<%=i18nBean.getText("admin.common.words.cancel")%>" name="<%=i18nBean.getText("admin.common.words.cancel")%>">

        </td>
    </tr>
    </table>
    </td>
</tr>
</table>
</form>

