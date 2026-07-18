
<%@ tag description="Realtime datetime display (VN)" pageEncoding="UTF-8" %>
<%@ attribute name="value" required="true" type="java.util.Date" rtexprvalue="true" %>
<%@ attribute name="cssClass" required="false" rtexprvalue="true" %>
<%@ attribute name="mode" required="false" rtexprvalue="true" %>
<%
    java.util.Date v = value;
    String modeVal = (mode == null) ? "" : mode.trim();
    String text;
    String title;
    if ("history".equalsIgnoreCase(modeVal)) {
        text = apartmentmanagement.util.DateTimeUtil.formatHistory(v);
        title = apartmentmanagement.util.DateTimeUtil.formatHistoryTitle(v);
    } else {
        text = apartmentmanagement.util.DateTimeUtil.formatRealtime(v);
        title = apartmentmanagement.util.DateTimeUtil.formatTitle(v);
    }
    String cls = (cssClass == null || cssClass.isEmpty()) ? "text-muted" : cssClass;
%>
<span class="rt-time <%= cls %>" title="<%= title %>"><%= text %></span>
