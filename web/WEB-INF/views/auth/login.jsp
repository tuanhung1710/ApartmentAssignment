<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Đăng nhập | Chung cư TienHung</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/app.css" rel="stylesheet">
</head>
<body class="login-page">
<div class="card login-card">
    <div class="card-body p-4 p-md-5">
        <div class="text-center mb-4">
            <div class="display-6 text-primary mb-2"><i class="bi bi-buildings"></i></div>
            <h1 class="h4 mb-1">Apartment Management</h1>
            <p class="text-muted small mb-0">Hệ thống quản lý căn hộ &amp; yêu cầu</p>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-danger py-2">${error}</div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/auth">
            <input type="hidden" name="action" value="login">
            <div class="mb-3">
                <label class="form-label" for="username">Username</label>
                <input type="text" class="form-control" id="username" name="username"
                       value="${username}" required autofocus autocomplete="username">
            </div>
            <div class="mb-3">
                <label class="form-label" for="password">Mật khẩu</label>
                <input type="password" class="form-control" id="password" name="password"
                       required autocomplete="current-password">
            </div>
            <button type="submit" class="btn btn-primary w-100">
                <i class="bi bi-box-arrow-in-right me-1"></i> Đăng nhập
            </button>
        </form>

        <hr class="my-4">
        <div class="small text-muted">
            <strong>Demo:</strong> admin / manager / staff / resident1 — mật khẩu <code>123456</code>
        </div>
    </div>
</div>
</body>
</html>
