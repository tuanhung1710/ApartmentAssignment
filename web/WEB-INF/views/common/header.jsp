<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<header class="topbar">
    <div class="d-flex align-items-center gap-2 min-w-0">
        <%-- Hamburger: chỉ hiện < lg (mobile / tablet dọc) --%>
        <button class="btn btn-outline-secondary btn-sm d-lg-none flex-shrink-0"
                type="button"
                data-bs-toggle="offcanvas"
                data-bs-target="#sidebarOffcanvas"
                aria-controls="sidebarOffcanvas"
                aria-label="Mở menu">
            <i class="bi bi-list fs-5"></i>
        </button>
        <h1 class="h5 mb-0 text-truncate">${empty pageTitle ? 'Dashboard' : pageTitle}</h1>
    </div>
    <div class="d-flex align-items-center gap-2 flex-shrink-0">
        <c:if test="${not empty sessionScope.currentUser}">
            <div class="dropdown">
                <button class="btn btn-sm btn-outline-secondary dropdown-toggle d-flex align-items-center gap-2"
                        type="button" data-bs-toggle="dropdown" aria-expanded="false">
                    <i class="bi bi-person-circle"></i>
                    <span class="d-none d-md-inline text-truncate" style="max-width: 10rem;">
                        ${sessionScope.currentUser.fullName}
                    </span>
                    <span class="badge text-bg-primary badge-role">${sessionScope.currentUser.role}</span>
                </button>
                <ul class="dropdown-menu dropdown-menu-end">
                    <li>
                        <a class="dropdown-item" href="${pageContext.request.contextPath}/profile">
                            <i class="bi bi-person me-2"></i> Hồ sơ
                        </a>
                    </li>
                    <li>
                        <a class="dropdown-item" href="${pageContext.request.contextPath}/profile?action=change-password">
                            <i class="bi bi-key me-2"></i> Đổi mật khẩu
                        </a>
                    </li>
                    <li><hr class="dropdown-divider"></li>
                    <li>
                        <a class="dropdown-item text-danger" href="${pageContext.request.contextPath}/auth?action=logout">
                            <i class="bi bi-box-arrow-right me-2"></i> Đăng xuất
                        </a>
                    </li>
                </ul>
            </div>
        </c:if>
    </div>
</header>
