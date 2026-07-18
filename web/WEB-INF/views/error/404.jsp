<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>404 | Không tìm thấy</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/app.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container py-5 text-center" style="max-width: 520px;">
    <div class="display-3 text-secondary mb-2"><i class="bi bi-question-circle"></i></div>
    <h1 class="h3">404 — Không tìm thấy trang</h1>
    <p class="text-muted">Đường dẫn không tồn tại hoặc module chưa được triển khai (TV2–TV5).</p>
    <a class="btn btn-primary" href="${pageContext.request.contextPath}/dashboard">Về Dashboard</a>
</div>
</body>
</html>
