<%@ include file="/libs/foundation/global.jsp" %>
<%@include file="/libs/cq/cloudserviceconfigs/components/configpage/init.jsp" %>
<%@page session="false" contentType="text/html"
        pageEncoding="utf-8"
        import="com.day.cq.i18n.I18n,com.ft.aem.poet.models.PoetConfigSlingModel" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %>
<sling:defineObjects/>

<%
    PoetConfigSlingModel poetConfig = resource.adaptTo(PoetConfigSlingModel.class);
    I18n i18n = new I18n(slingRequest.getResourceBundle(slingRequest.getLocale()));
    String token = "";
    String uri = "";
    boolean abandon = true;
    Map<String, String> mappings = new HashMap<>();
    if (poetConfig != null) {
        token = poetConfig.getToken();
        if (token != null) {
            uri = poetConfig.getUri();
            int tokenLength = token.length();
            int endIndex = 3;
            if (token.length() < 4) {
                endIndex = token.length() - 1;
            }
            token = token.substring(0, endIndex);
            int extraChars = tokenLength - endIndex;
            for (int i = 1; i <= extraChars; i++) {
                token += "x";
            }
        }
        mappings = poetConfig.getMappings();
        abandon = poetConfig.getAbandonOnFailure();
    }

%>
<div>
    <div>
        <h3><%= i18n.get("Poet Configuration")%>
        </h3>
        <ul>
            <li>
                <div class="li-bullet">
                    <strong><%= i18n.get("Frost API Key")%>: </strong>
                    <%= token != null && !token.isEmpty() ? token : "not set" %>
                </div>
                <div class="li-bullet">
                    <strong><%= i18n.get("Fail Immediately on Frost Error?")%>: </strong>
                    <%= abandon %>
                </div>
                <div class="li-bullet">
                    <strong><%= i18n.get("Frost Base URI")%>: </strong>
                    <%= uri != null && !uri.isEmpty() ? uri : "not set" %>
                </div>
                <div class="li-bullet">
                    <strong><%= i18n.get("Resource Type Mappings")%>: </strong>
                </div>
                <div>
                    <table>
                        <tr>
                            <td><b><%= i18n.get("Resource Type")%>
                            </b></td>
                            <td><b><%= i18n.get("Path from jcr:content to parsys node of content")%>
                            </b></td>
                        </tr>
                        <%
                            for (Map.Entry<String, String> entry : mappings.entrySet()) {
                        %>
                        <tr>
                            <td><%= entry.getKey() %>
                            </td>
                            <td><%= entry.getValue() %>
                            </td>
                        </tr>
                        <%
                            }

                        %>
                    </table>
                </div>

            </li>
        </ul>
    </div>
</div>