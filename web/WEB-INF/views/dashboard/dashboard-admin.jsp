<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="mb-3">
    <h2 class="h4">Xin chào, ${sessionScope.currentUser.fullName}!</h2>
    <p class="text-muted mb-0">
        Vai trò: <span class="badge text-bg-primary">ADMIN</span>
        · Tổng quan hệ thống
    </p>
</div>

<div class="row g-3">
    <div class="col-md-4">
        <div class="card stat-card h-100">
            <div class="card-body">
                <div class="text-muted small"><i class="bi bi-people me-1"></i> Tổng người dùng</div>
                <div class="stat-value text-primary">${empty totalUsers ? 0 : totalUsers}</div>
            </div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="card stat-card h-100">
            <div class="card-body">
                <div class="text-muted small"><i class="bi bi-lock me-1"></i> User bị khóa</div>
                <div class="stat-value text-danger">${empty lockedUsers ? 0 : lockedUsers}</div>
            </div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="card stat-card h-100">
            <div class="card-body">
                <div class="text-muted small"><i class="bi bi-door-open me-1"></i> Số căn hộ</div>
                <div class="stat-value text-success">${empty totalApartments ? 0 : totalApartments}</div>
            </div>
        </div>
    </div>
</div>

<div class="mt-3">
    <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/admin?action=users">
        Quản lý người dùng
    </a>
    <a class="btn btn-sm btn-outline-secondary" href="${pageContext.request.contextPath}/admin?action=announcements">
        Thông báo
    </a>
</div>
