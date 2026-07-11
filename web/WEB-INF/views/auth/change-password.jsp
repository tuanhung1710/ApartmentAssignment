<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="card shadow-sm" style="max-width: 520px;">
    <div class="card-header">Đổi mật khẩu</div>
    <div class="card-body">
        <form method="post" action="${pageContext.request.contextPath}/profile">
            <input type="hidden" name="action" value="change-password">

            <div class="mb-3">
                <label class="form-label" for="oldPassword">Mật khẩu hiện tại</label>
                <input type="password" class="form-control" id="oldPassword" name="oldPassword" required>
            </div>
            <div class="mb-3">
                <label class="form-label" for="newPassword">Mật khẩu mới</label>
                <input type="password" class="form-control" id="newPassword" name="newPassword"
                       minlength="6" required>
            </div>
            <div class="mb-3">
                <label class="form-label" for="confirmPassword">Xác nhận mật khẩu mới</label>
                <input type="password" class="form-control" id="confirmPassword" name="confirmPassword"
                       minlength="6" required>
            </div>

            <div class="d-flex gap-2">
                <button type="submit" class="btn btn-primary">Đổi mật khẩu</button>
                <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/profile">Hủy</a>
            </div>
        </form>
    </div>
</div>
