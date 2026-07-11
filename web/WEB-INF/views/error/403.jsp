<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>403 Forbidden</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container py-5 text-center">
    <h1 class="display-4 text-danger">403</h1>
    <p class="lead">Bạn không có quyền truy cập trang này.</p>
    <a class="btn btn-primary" href="${pageContext.request.contextPath}/dashboard">Về Dashboard</a>
    <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/auth?action=login">Đăng nhập lại</a>
</div>
</body>
</html>
