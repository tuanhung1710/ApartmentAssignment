<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>404 Not Found</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container py-5 text-center">
    <h1 class="display-4 text-secondary">404</h1>
    <p class="lead">Không tìm thấy trang yêu cầu.</p>
    <a class="btn btn-primary" href="${pageContext.request.contextPath}/dashboard">Về Dashboard</a>
</div>
</body>
</html>
