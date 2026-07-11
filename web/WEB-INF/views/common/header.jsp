<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<header class="topbar">
    <div>
        <h1 class="h5 mb-0">${empty pageTitle ? 'Dashboard' : pageTitle}</h1>
    </div>
    <div class="d-flex align-items-center gap-2">
        <c:if test="${not empty sessionScope.currentUser}">
            <span class="text-muted small d-none d-md-inline">
                ${sessionScope.currentUser.fullName}
            </span>
            <span class="badge text-bg-primary badge-role">${sessionScope.currentUser.role}</span>
        </c:if>
    </div>
</header>
