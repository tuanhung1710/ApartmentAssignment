<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="card shadow-sm">
    <div class="card-header d-flex flex-column flex-sm-row justify-content-between align-items-sm-center gap-2">
        <span><i class="bi bi-person-badge me-1"></i> Thông tin hồ sơ</span>
        <div class="d-flex flex-wrap gap-2">
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
            <dt class="col-12 col-sm-3">Username</dt>
            <dd class="col-12 col-sm-9">${profile.username}</dd>

            <dt class="col-12 col-sm-3">Họ tên</dt>
            <dd class="col-12 col-sm-9">${profile.fullName}</dd>

            <dt class="col-12 col-sm-3">Email</dt>
            <dd class="col-12 col-sm-9 text-break">${empty profile.email ? '—' : profile.email}</dd>

            <dt class="col-12 col-sm-3">Số điện thoại</dt>
            <dd class="col-12 col-sm-9">${empty profile.phone ? '—' : profile.phone}</dd>

            <dt class="col-12 col-sm-3">Vai trò</dt>
            <dd class="col-12 col-sm-9"><span class="badge text-bg-primary">${profile.role}</span></dd>

            <dt class="col-12 col-sm-3">Bộ phận</dt>
            <dd class="col-12 col-sm-9">${empty profile.department ? '—' : profile.department}</dd>

            <c:if test="${profile.role == 'RESIDENT'}">
                <dt class="col-12 col-sm-3">Căn hộ</dt>
                <dd class="col-12 col-sm-9">
                    <c:choose>
                        <c:when test="${not empty myApartment}">
                            <span class="badge text-bg-success">${myApartment}</span>
                        </c:when>
                        <c:otherwise>
                            <span class="text-muted">Chưa gán căn hộ</span>
                        </c:otherwise>
                    </c:choose>
                </dd>
            </c:if>

            <dt class="col-12 col-sm-3">Trạng thái</dt>
            <dd class="col-12 col-sm-9">
                <c:choose>
                    <c:when test="${profile.isActive}"><span class="badge text-bg-success">Active</span></c:when>
                    <c:otherwise><span class="badge text-bg-danger">Locked</span></c:otherwise>
                </c:choose>
            </dd>
        </dl>
        <p class="form-text text-muted mt-3 mb-0">
            Username và vai trò không thể tự đổi. Liên hệ Admin nếu cần hỗ trợ.
        </p>
    </div>
</div>
