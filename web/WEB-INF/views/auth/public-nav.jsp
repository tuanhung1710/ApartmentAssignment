<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%--
  Navbar public (Visitor landing).
  publicActive: home | about | login | privacy | terms
  Flow: Landing (About / Announcements) → Login → Dashboard (sau auth).
--%>
<nav class="navbar navbar-expand-lg navbar-light bg-white sticky-top py-2 public-navbar">
    <div class="container">
        <a class="navbar-brand text-primary-custom fw-bold fs-4 d-flex align-items-center"
           href="${pageContext.request.contextPath}/auth?action=home">
            <i class="bi bi-buildings-fill fs-3 me-2"></i> Skyland Apartment
        </a>
        <button class="navbar-toggler border-0" type="button" data-bs-toggle="collapse" data-bs-target="#publicNav"
                aria-controls="publicNav" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="publicNav">
            <ul class="navbar-nav ms-auto align-items-lg-center gap-lg-1">
                <li class="nav-item">
                    <a class="nav-link ${publicActive == 'home' ? 'active' : ''}"
                       href="${pageContext.request.contextPath}/auth?action=home">Trang chủ</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${publicActive == 'about' ? 'active' : ''}"
                       href="${pageContext.request.contextPath}/auth?action=home#about">Giới thiệu</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link"
                       href="${pageContext.request.contextPath}/auth?action=home#announcements">Thông báo</a>
                </li>
                <li class="nav-item ms-lg-2 mt-2 mt-lg-0">
                    <a class="btn ${publicActive == 'login' ? 'btn-primary-custom' : 'btn-outline-primary-custom'} px-4 py-2 rounded-pill shadow-sm"
                       href="${pageContext.request.contextPath}/auth?action=login">
                        <i class="bi bi-box-arrow-in-right me-1"></i> Đăng nhập
                    </a>
                </li>
            </ul>
        </div>
    </div>
</nav>
