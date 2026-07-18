<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="card shadow-sm" style="max-width: 640px;">
    <div class="card-header">Cập nhật hồ sơ</div>
    <div class="card-body">
        <form method="post" action="${pageContext.request.contextPath}/profile">
            <input type="hidden" name="action" value="update-profile">

            <div class="mb-3">
                <label class="form-label">Username</label>
                <input type="text" class="form-control" value="${profile.username}" disabled>
            </div>
            <div class="mb-3">
                <label class="form-label" for="fullName">Họ tên <span class="text-danger">*</span></label>
                <input type="text" class="form-control" id="fullName" name="fullName"
                       value="${profile.fullName}" required>
            </div>
            <div class="mb-3">
                <label class="form-label" for="email">Email</label>
                <input type="email" class="form-control" id="email" name="email"
                       maxlength="100" value="${profile.email}"
                       placeholder="email@example.com">
                <div class="form-text">Bạn tự cập nhật · Admin chỉ xem, không sửa được</div>
            </div>
            <div class="mb-3">
                <label class="form-label" for="phone">Số điện thoại</label>
                <input type="text" class="form-control" id="phone" name="phone"
                       inputmode="numeric" maxlength="10" minlength="10"
                       pattern="0[0-9]{9}" title="Đúng 10 chữ số, bắt đầu bằng 0"
                       value="${profile.phone}" placeholder="0912345678">
                <div class="form-text">Đúng 10 số VN (0xxxxxxxxx) · Admin chỉ xem, không sửa được</div>
            </div>
            <div class="mb-3">
                <label class="form-label">Vai trò</label>
                <input type="text" class="form-control" value="${profile.role}" disabled>
                <div class="form-text">Không thể tự đổi role.</div>
            </div>

            <div class="d-flex gap-2">
                <button type="submit" class="btn btn-primary">Lưu</button>
                <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/profile">Hủy</a>
            </div>
        </form>
    </div>
</div>
