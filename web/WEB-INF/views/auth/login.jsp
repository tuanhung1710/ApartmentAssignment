<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate">
    <meta http-equiv="Pragma" content="no-cache">
    <meta http-equiv="Expires" content="0">
    <title>Đăng nhập | Skyland Apartment</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/public.css" rel="stylesheet">
    <style>
        .login-page {
            flex: 1;
            display: flex;
            align-items: center;
            padding: 2rem 0 3rem;
            background:
                linear-gradient(135deg, rgba(26, 67, 136, 0.06) 0%, rgba(248, 249, 250, 1) 45%),
                #f8f9fa;
        }
        .login-card {
            border: none;
            border-radius: 16px;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
            overflow: hidden;
            background: #fff;
        }
        .login-side {
            background:
                linear-gradient(160deg, rgba(26, 67, 136, 0.92), rgba(18, 48, 99, 0.88)),
                url('https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?ixlib=rb-4.0.3&auto=format&fit=crop&w=1200&q=80') center/cover no-repeat;
            color: #fff;
            min-height: 100%;
            padding: 2.5rem 2rem;
            display: flex;
            flex-direction: column;
            justify-content: flex-end;
        }
        .login-side h2 {
            font-weight: 700;
            font-size: 1.65rem;
            line-height: 1.3;
        }
        .login-form-wrap {
            padding: 2.5rem 2rem;
        }
        .login-avatar {
            width: 56px;
            height: 56px;
        }
        .login-page .input-group-text {
            background: transparent;
            border-right: none;
            color: #6c757d;
        }
        .login-page .form-control.border-start-0 { border-left: none; }
        .login-page .form-control:focus { box-shadow: none; border-color: #dee2e6; }
        .login-page .input-group:focus-within {
            box-shadow: 0 0 0 0.25rem rgba(26, 67, 136, 0.15);
            border-radius: 0.375rem;
        }
        .login-page .input-group:focus-within .form-control,
        .login-page .input-group:focus-within .input-group-text,
        .login-page .input-group:focus-within .btn {
            border-color: #1a4388;
            color: #1a4388;
        }
        @media (max-width: 767.98px) {
            .login-form-wrap { padding: 1.5rem 1.15rem; }
            .login-page { padding: 1rem 0 2rem; }
        }
    </style>
</head>
<body class="public-body">

<c:set var="publicActive" value="login" scope="request"/>
<%@ include file="/WEB-INF/views/auth/public-nav.jsp" %>

<main class="login-page">
    <div class="container">
        <div class="row justify-content-center">
            <div class="col-xl-9 col-lg-10">
                <div class="login-card row g-0">
                    <div class="col-md-5 d-none d-md-block">
                        <div class="login-side h-100">
                            <p class="small text-uppercase opacity-75 fw-semibold mb-2">Cổng nội bộ</p>
                            <h2 class="mb-3">Đăng nhập để dùng chức năng quản lý &amp; cư dân</h2>
                            <ul class="list-unstyled small mb-0 opacity-90">
                                <li class="mb-2"><i class="bi bi-check2-circle me-2"></i>Resident — căn hộ, phí, yêu cầu</li>
                                <li class="mb-2"><i class="bi bi-check2-circle me-2"></i>Manager / Staff — xử lý vận hành</li>
                                <li><i class="bi bi-check2-circle me-2"></i>Admin — người dùng &amp; hệ thống</li>
                            </ul>
                        </div>
                    </div>

                    <div class="col-md-7 col-12">
                        <div class="login-form-wrap">
                            <div class="text-center mb-4">
                                <div class="login-avatar d-inline-flex align-items-center justify-content-center bg-primary-custom text-white rounded-circle mb-3">
                                    <i class="bi bi-person-fill fs-3"></i>
                                </div>
                                <h1 class="h3 fw-bold text-dark mb-1">Đăng nhập</h1>
                                <p class="text-muted mb-0 small">
                                    Cư dân · Ban quản lý · Nhân viên · Admin
                                </p>
                            </div>

                            <c:if test="${not empty error}">
                                <div class="alert alert-danger d-flex align-items-start border-0 rounded-3 shadow-sm mb-4" role="alert">
                                    <i class="bi bi-exclamation-triangle-fill fs-5 me-3 mt-1"></i>
                                    <div>
                                        <strong>Không đăng nhập được</strong><br>
                                        ${error}
                                    </div>
                                </div>
                            </c:if>

                            <form method="post" action="${pageContext.request.contextPath}/auth" autocomplete="on">
                                <input type="hidden" name="action" value="login">

                                <div class="mb-3">
                                    <label for="username" class="form-label fw-medium">Tên đăng nhập</label>
                                    <div class="input-group input-group-lg">
                                        <span class="input-group-text"><i class="bi bi-person"></i></span>
                                        <input type="text" class="form-control border-start-0" id="username" name="username"
                                               placeholder="Nhập username" required autofocus
                                               autocomplete="username" value="${username}">
                                    </div>
                                </div>

                                <div class="mb-3">
                                    <label for="password" class="form-label fw-medium">Mật khẩu</label>
                                    <div class="input-group input-group-lg">
                                        <span class="input-group-text"><i class="bi bi-lock"></i></span>
                                        <input type="password" class="form-control border-start-0" id="password" name="password"
                                               placeholder="Nhập mật khẩu" required autocomplete="current-password">
                                        <button class="btn border border-start-0 bg-transparent text-muted" type="button"
                                                id="togglePassword" aria-label="Hiện/ẩn mật khẩu">
                                            <i class="bi bi-eye-slash" id="toggleIcon"></i>
                                        </button>
                                    </div>
                                </div>

                                <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-4">
                                    <div class="form-check mb-0">
                                        <input class="form-check-input" type="checkbox" id="remember" name="remember">
                                        <label class="form-check-label text-muted user-select-none" for="remember">
                                            Nhớ tài khoản
                                        </label>
                                    </div>
                                    <a href="${pageContext.request.contextPath}/auth?action=forgot-password"
                                       class="text-primary-custom text-decoration-none fw-medium">
                                        Quên mật khẩu?
                                    </a>
                                </div>

                                <div class="d-grid">
                                    <button type="submit" class="btn btn-primary-custom btn-lg fw-bold rounded-3 shadow-sm">
                                        Đăng nhập
                                    </button>
                                </div>
                            </form>

                            <div class="text-center mt-4 pt-1">
                                <a class="text-decoration-none text-muted small"
                                   href="${pageContext.request.contextPath}/auth?action=home">
                                    <i class="bi bi-arrow-left me-1"></i> Về trang giới thiệu
                                </a>
                            </div>
                            <p class="text-center text-muted small mt-3 mb-0">
                                Chưa có tài khoản? Liên hệ <strong class="text-dark">Ban Quản Lý</strong> để được cấp.
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/views/auth/public-footer.jsp" %>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    // bfcache: reload để AuthFilter đẩy user đã login về /dashboard
    window.addEventListener('pageshow', function (event) {
        var nav = window.performance && window.performance.getEntriesByType
            ? window.performance.getEntriesByType('navigation')[0]
            : null;
        var fromBfCache = event.persisted
            || (nav && nav.type === 'back_forward');
        if (fromBfCache) {
            window.location.reload();
        }
    });

    document.addEventListener('DOMContentLoaded', function () {
        var togglePasswordBtn = document.getElementById('togglePassword');
        var passwordInput = document.getElementById('password');
        var toggleIcon = document.getElementById('toggleIcon');
        if (togglePasswordBtn && passwordInput && toggleIcon) {
            togglePasswordBtn.addEventListener('click', function () {
                var isPassword = passwordInput.getAttribute('type') === 'password';
                passwordInput.setAttribute('type', isPassword ? 'text' : 'password');
                toggleIcon.classList.toggle('bi-eye-slash', !isPassword);
                toggleIcon.classList.toggle('bi-eye', isPassword);
            });
        }
    });
</script>
</body>
</html>
