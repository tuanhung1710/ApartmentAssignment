<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>403 | Không có quyền</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/app.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container py-5 text-center" style="max-width: 520px;">
    <div class="display-3 text-danger mb-2"><i class="bi bi-shield-lock"></i></div>
    <h1 class="h3">403 — Không có quyền truy cập</h1>
    <p class="text-muted">Tài khoản của bạn không được phép mở trang này. Hãy dùng đúng role hoặc quay lại Dashboard.</p>
    <div class="d-flex gap-2 justify-content-center">
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/dashboard">Về Dashboard</a>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/auth?action=login">Đăng nhập lại</a>
    </div>
</div>
</body>
</html>
