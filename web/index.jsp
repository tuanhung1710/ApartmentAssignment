<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="apartmentmanagement.util.AppConstants" %>
<%
    // Entry: chưa login → Landing; đã login → Dashboard
    // Flow: Landing → Login → Dashboard → Logout → Landing
    Object currentUser = session.getAttribute(AppConstants.SESSION_USER);
    if (currentUser != null) {
        response.sendRedirect(request.getContextPath() + "/dashboard");
    } else {
        response.sendRedirect(request.getContextPath() + "/auth?action=home");
    }
%>
