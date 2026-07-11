<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="card shadow-sm">
    <div class="card-header d-flex justify-content-between align-items-center">
        <span><i class="bi bi-person-badge me-1"></i> Thông tin hồ sơ</span>
        <div class="d-flex gap-2">
            <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/profile?action=edit-profile">
                Sửa hồ sơ
            </a>
            <a class="btn btn-sm btn-outline-secondary" href="${pageContext.request.contextPath}/profile?action=change-password">
                Đổi mật khẩu
            </a>
        </div>
    </div>
    <div class="card-body">
        <dl class="row mb-0">
            <dt class="col-sm-3">Username</dt>
            <dd class="col-sm-9">${profile.username}</dd>

            <dt class="col-sm-3">Họ tên</dt>
            <dd class="col-sm-9">${profile.fullName}</dd>

            <dt class="col-sm-3">Email</dt>
            <dd class="col-sm-9">${empty profile.email ? '—' : profile.email}</dd>

            <dt class="col-sm-3">Số điện thoại</dt>
            <dd class="col-sm-9">${empty profile.phone ? '—' : profile.phone}</dd>

            <dt class="col-sm-3">Vai trò</dt>
            <dd class="col-sm-9"><span class="badge text-bg-primary">${profile.role}</span></dd>

            <dt class="col-sm-3">Bộ phận</dt>
            <dd class="col-sm-9">${empty profile.department ? '—' : profile.department}</dd>

            <dt class="col-sm-3">Trạng thái</dt>
            <dd class="col-sm-9">
                <c:choose>
                    <c:when test="${profile.isActive}"><span class="badge text-bg-success">Active</span></c:when>
                    <c:otherwise><span class="badge text-bg-danger">Locked</span></c:otherwise>
                </c:choose>
            </dd>
        </dl>
    </div>
</div>
