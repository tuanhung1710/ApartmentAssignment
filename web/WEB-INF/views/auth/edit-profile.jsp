<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="card shadow-sm w-100" style="max-width: 640px;">
    <div class="card-header">Cập nhật hồ sơ</div>
    <div class="card-body">
        <form method="post" action="${pageContext.request.contextPath}/profile">
            <input type="hidden" name="action" value="update-profile">

            <%-- Chỉ hiển thị, không gửi lên server --%>
            <div class="mb-3">
                <label class="form-label">Username</label>
                <input type="text" class="form-control" value="${profile.username}" readonly>
            </div>

            <div class="mb-3">
                <label class="form-label" for="fullName">Họ tên <span class="text-danger">*</span></label>
                <input type="text" class="form-control" id="fullName" name="fullName"
                       value="${profile.fullName}" required>
            </div>

            <div class="row g-3">
                <%-- Email chỉ hiển thị — không name → không submit; server cũng bỏ qua param email --%>
                <div class="col-12 col-md-6">
                    <label class="form-label" for="email">Email</label>
                    <input type="email" class="form-control" id="email"
                           value="${profile.email}" readonly>
                    <div class="form-text">Email do Ban Quản Lý cấp, không thể tự đổi.</div>
                </div>
                <div class="col-12 col-md-6">
                    <label class="form-label" for="phone">Số điện thoại</label>
                    <input type="text" class="form-control" id="phone" name="phone"
                           value="${profile.phone}" placeholder="9–11 chữ số" maxlength="11">
                </div>
            </div>

            <div class="row g-3 mt-0">
                <div class="col-12 col-md-6">
                    <label class="form-label">Vai trò</label>
                    <input type="text" class="form-control" value="${profile.role}" readonly>
                    <div class="form-text">Không thể tự đổi role.</div>
                </div>
                <div class="col-12 col-md-6">
                    <label class="form-label">Bộ phận</label>
                    <input type="text" class="form-control"
                           value="${empty profile.department ? '—' : profile.department}" readonly>
                </div>
            </div>

            <div class="d-flex flex-column flex-sm-row gap-2 mt-4">
                <button type="submit" class="btn btn-primary">Lưu</button>
                <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/profile">Hủy</a>
            </div>
        </form>
    </div>
</div>
