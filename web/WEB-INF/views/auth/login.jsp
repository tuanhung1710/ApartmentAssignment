<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập | Skyland Apartment</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <style>
        * { box-sizing: border-box; }
        html, body {
            margin: 0;
            padding: 0;
            width: 100%;
            max-width: 100%;
            overflow-x: hidden;
        }
        body {
            font-family: 'Inter', system-ui, -apple-system, sans-serif;
            background-color: #f8f9fa;
            color: #212529;
            display: block !important;
            min-height: 100vh;
        }
        .text-primary-custom { color: #1a4388 !important; }
        .bg-primary-custom { background-color: #1a4388 !important; }
        .btn-primary-custom {
            background-color: #1a4388;
            border-color: #1a4388;
            color: #fff;
        }
        .btn-primary-custom:hover,
        .btn-primary-custom:focus {
            background-color: #123063;
            border-color: #123063;
            color: #fff;
        }
        .btn-outline-primary-custom {
            color: #1a4388;
            border-color: #1a4388;
            font-weight: 500;
        }
        .btn-outline-primary-custom:hover {
            background-color: #1a4388;
            color: #fff;
        }

        /* Navbar full width */
        .navbar {
            width: 100%;
            box-shadow: 0 2px 15px rgba(0,0,0,0.08);
            background: #fff !important;
        }

        /* Hero full width + ảnh tòa nhà */
        .hero-section {
            display: block;
            width: 100%;
            background:
                linear-gradient(to right, rgba(26, 67, 136, 0.85), rgba(26, 67, 136, 0.4)),
                url('https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?ixlib=rb-4.0.3&auto=format&fit=crop&w=1920&q=80') center/cover no-repeat;
            color: #fff;
            padding: 120px 0 160px;
        }
        .hero-title {
            font-weight: 700;
            font-size: 3.5rem;
            line-height: 1.2;
            margin-bottom: 24px;
            color: #fff;
        }
        .hero-subtitle {
            font-size: 1.25rem;
            font-weight: 300;
            margin-bottom: 40px;
            opacity: 0.9;
            max-width: 36rem;
        }

        /* Card login chồng hero */
        .login-section {
            display: block;
            width: 100%;
            margin-top: -100px;
            position: relative;
            z-index: 10;
            padding-bottom: 1rem;
        }
        .login-card {
            border: none;
            border-radius: 16px;
            box-shadow: 0 20px 40px rgba(0,0,0,0.12);
            overflow: hidden;
            background-color: #fff;
            width: 100%;
        }
        .login-image {
            background:
                url('https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80') center/cover no-repeat;
            min-height: 520px;
            height: 100%;
            width: 100%;
        }
        .login-form-container {
            padding: 3.5rem 3rem;
            background: #fff;
            width: 100%;
        }
        .login-avatar {
            width: 60px;
            height: 60px;
        }

        /* Input group focus */
        .login-section .input-group-text {
            background-color: transparent;
            border-right: none;
            color: #6c757d;
        }
        .login-section .form-control.border-start-0 {
            border-left: none;
        }
        .login-section .form-control:focus {
            box-shadow: none;
            border-color: #dee2e6;
        }
        .login-section .input-group:focus-within {
            box-shadow: 0 0 0 0.25rem rgba(26, 67, 136, 0.15);
            border-radius: 0.375rem;
        }
        .login-section .input-group:focus-within .form-control,
        .login-section .input-group:focus-within .input-group-text,
        .login-section .input-group:focus-within .btn {
            border-color: #1a4388;
            color: #1a4388;
        }

        .login-demo {
            border-top: 1px dashed #e5e7eb;
            padding-top: 1rem;
            margin-top: 1.25rem;
        }
        .login-demo code {
            background: #f1f5f9;
            padding: 0.1rem 0.35rem;
            border-radius: 0.25rem;
            color: #334155;
        }

        /* Footer full width tối */
        .footer {
            display: block;
            width: 100%;
            background-color: #111827;
            color: #9ca3af;
            padding: 60px 0 30px;
            margin-top: 80px;
        }
        .footer h5 {
            color: #f3f4f6;
            font-weight: 600;
        }
        .footer a {
            color: #9ca3af;
            text-decoration: none;
            transition: color 0.2s;
        }
        .footer a:hover { color: #fff; }
        .footer .text-primary-custom { color: #60a5fa !important; }

        @media (max-width: 991.98px) {
            .hero-title { font-size: 2.5rem; }
            .hero-section { padding: 80px 0 120px; }
            .login-section { margin-top: -60px; }
            .login-form-container { padding: 2rem; }
            .login-image { min-height: 240px; }
        }
        @media (max-width: 767.98px) {
            .hero-title { font-size: 1.75rem; }
            .hero-subtitle { font-size: 1.05rem; }
            .hero-section { padding: 56px 0 100px; }
            .login-section { margin-top: -48px; }
            .login-form-container { padding: 1.25rem 1rem; }
            .login-section .input-group-lg > .form-control,
            .login-section .input-group-lg > .input-group-text {
                font-size: 1rem;
            }
            .footer { padding: 40px 0 24px; margin-top: 48px; }
        }
        @media (max-width: 399.98px) {
            .hero-title { font-size: 1.5rem; }
            .btn-lg { font-size: 1rem; padding: 0.65rem 1.25rem; }
        }
    </style>
</head>
<body>

    <!-- 1. Navbar -->
    <nav class="navbar navbar-expand-lg navbar-light bg-white sticky-top py-2">
        <div class="container">
            <a class="navbar-brand text-primary-custom fw-bold fs-4 d-flex align-items-center" href="${pageContext.request.contextPath}/auth?action=login">
                <i class="bi bi-buildings-fill fs-3 me-2"></i> Skyland Apartment
            </a>
            <button class="navbar-toggler border-0" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav"
                    aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav ms-auto align-items-center">
                    <li class="nav-item me-4 mb-2 mb-lg-0 mt-2 mt-lg-0">
                        <a class="nav-link fw-medium text-dark" href="tel:19001234">
                            <i class="bi bi-telephone-fill text-danger me-1"></i>
                            Hotline: <span class="fw-bold">1900 1234</span>
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="btn btn-outline-primary-custom px-4 py-2 rounded-pill shadow-sm" href="#login-section">
                            Đăng nhập
                        </a>
                    </li>
                </ul>
            </div>
        </div>
    </nav>

    <!-- 2. Hero -->
    <section class="hero-section">
        <div class="container">
            <div class="row">
                <div class="col-lg-7 col-md-10 text-center text-lg-start">
                    <h1 class="hero-title">Không Gian Sống Thông Minh,<br>Quản Lý Tiện Lợi</h1>
                    <p class="hero-subtitle">
                        Nền tảng số hóa quản lý vận hành chung cư. Mang đến trải nghiệm an toàn,
                        minh bạch và đẳng cấp dành riêng cho cư dân Skyland.
                    </p>
                    <a href="${pageContext.request.contextPath}/auth?action=fee-lookup"
                       class="btn btn-primary-custom btn-lg rounded-pill px-5 py-3 shadow">
                        Tra cứu phí dịch vụ <i class="bi bi-search ms-2"></i>
                    </a>
                </div>
            </div>
        </div>
    </section>

    <!-- 3. Login card -->
    <section id="login-section" class="login-section">
        <div class="container">
            <div class="row justify-content-center">
                <div class="col-xl-10 col-lg-12">
                    <div class="login-card row g-0">

                        <!-- Ảnh trái -->
                        <div class="col-md-5 d-none d-md-block">
                            <div class="login-image"></div>
                        </div>

                        <!-- Form phải -->
                        <div class="col-md-7 col-12 bg-white">
                            <div class="login-form-container">
                                <div class="text-center mb-4 pb-2">
                                    <div class="login-avatar d-inline-flex align-items-center justify-content-center bg-primary-custom text-white rounded-circle mb-3">
                                        <i class="bi bi-person-fill fs-2"></i>
                                    </div>
                                    <h3 class="fw-bold text-dark">Cổng Đăng Nhập</h3>
                                    <p class="text-muted mb-0">Dành cho Cư dân, Ban quản lý và Nhân viên</p>
                                </div>

                                <c:if test="${not empty error}">
                                    <div class="alert alert-danger d-flex align-items-center border-0 rounded-3 shadow-sm mb-4" role="alert">
                                        <i class="bi bi-exclamation-triangle-fill fs-5 me-3"></i>
                                        <div>
                                            <strong>Lỗi đăng nhập!</strong><br>
                                            ${error}
                                        </div>
                                    </div>
                                </c:if>

                                <form method="post" action="${pageContext.request.contextPath}/auth">
                                    <input type="hidden" name="action" value="login">

                                    <div class="mb-4">
                                        <label for="username" class="form-label fw-medium text-dark">Tên đăng nhập</label>
                                        <div class="input-group input-group-lg">
                                            <span class="input-group-text"><i class="bi bi-person"></i></span>
                                            <input type="text" class="form-control border-start-0" id="username" name="username"
                                                   placeholder="Nhập username" required autofocus
                                                   autocomplete="username" value="${username}">
                                        </div>
                                    </div>

                                    <div class="mb-4">
                                        <label for="password" class="form-label fw-medium text-dark">Mật khẩu</label>
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

                                    <div class="d-flex justify-content-between align-items-center mb-4 pb-2">
                                        <div class="form-check">
                                            <input class="form-check-input" type="checkbox" id="remember" name="remember">
                                            <label class="form-check-label text-muted user-select-none" for="remember">
                                                Nhớ tài khoản
                                            </label>
                                        </div>
                                        <a href="#login-section" class="text-primary-custom text-decoration-none fw-medium">
                                            Quên mật khẩu?
                                        </a>
                                    </div>

                                    <div class="d-grid">
                                        <button type="submit" class="btn btn-primary-custom btn-lg fw-bold rounded-3 shadow">
                                            ĐĂNG NHẬP
                                        </button>
                                    </div>
                                </form>

                                <div class="login-demo">
                                    <div class="small text-muted mb-2">
                                        <i class="bi bi-info-circle me-1"></i>
                                        <strong>Tài khoản demo</strong> — mật khẩu <code>123456</code>
                                    </div>
                                    <div class="d-flex flex-wrap gap-2">
                                        <span class="badge text-bg-light border">admin</span>
                                        <span class="badge text-bg-light border">manager</span>
                                        <span class="badge text-bg-light border">staff</span>
                                        <span class="badge text-bg-light border">resident1</span>
                                    </div>
                                </div>

                                <div class="text-center mt-4 pt-2 text-muted small">
                                    Tài khoản cư dân mới? Vui lòng liên hệ
                                    <span class="fw-bold text-dark">Ban Quản Lý</span> để được cấp phát.
                                </div>
                            </div>
                        </div>

                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- 4. Footer -->
    <footer class="footer">
        <div class="container">
            <div class="row g-4">
                <div class="col-lg-5 col-md-6">
                    <h5 class="mb-3">
                        <i class="bi bi-buildings me-2 text-primary-custom"></i>Skyland Apartment
                    </h5>
                    <p class="mb-2 pe-md-4">
                        Dự án Hệ thống quản lí chung cư được thực hiện bởi nhóm 6 (SE2036) với các thành viên:
                        Nguyễn Vũ Tuấn Hùng,
                        Hoàng Quốc Việt, 
                        Đỗ Đức Long,
                        Trần Hồng Minh,
                        Tạ Hoàng Lương.
                    </p>
                </div>
                <div class="col-lg-4 col-md-6">
                    <h5 class="mb-3">Thông tin liên hệ</h5>
                    <p class="mb-2">
                        <i class="bi bi-geo-alt me-2 text-primary-custom"></i>
                        Đại học FPT, Hòa Lạc, Thạch Thất, Hà Nội
                    </p>
                    <p class="mb-2">
                        <i class="bi bi-envelope me-2 text-primary-custom"></i>
                        Email: Group6_SE2036@gmail.com
                    </p>
                    <p class="mb-0">
                        <i class="bi bi-telephone me-2 text-primary-custom"></i>
                        Hotline: 1900 1234 (Hỗ trợ 24/7)
                    </p>
                </div>
                <div class="col-lg-3 col-md-12 text-lg-end">
                    <h5 class="mb-3">Kết nối với chúng tôi</h5>
                    <div class="d-flex justify-content-lg-end mb-3">
                        <a href="#" class="me-3 fs-4" aria-label="Facebook"><i class="bi bi-facebook"></i></a>
                        <a href="#" class="me-3 fs-4" aria-label="YouTube"><i class="bi bi-youtube"></i></a>
                        <a href="#" class="fs-4" aria-label="Instagram"><i class="bi bi-instagram"></i></a>
                    </div>
                </div>
            </div>
            <hr class="border-secondary mt-5 mb-4">
            <div class="row text-center text-md-start">
                <div class="col-md-6 mb-2 mb-md-0">
                    <p class="small mb-0">&copy; 2026 Skyland Apartment Management. All rights reserved.</p>
                </div>
                <div class="col-md-6 text-md-end">
                    <a href="${pageContext.request.contextPath}/auth?action=privacy" class="small me-3">Chính sách bảo mật</a>
                    <a href="${pageContext.request.contextPath}/auth?action=terms" class="small">Điều khoản dịch vụ</a>
                </div>
            </div>
        </div>
    </footer>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            var togglePasswordBtn = document.getElementById('togglePassword');
            var passwordInput = document.getElementById('password');
            var toggleIcon = document.getElementById('toggleIcon');

            if (togglePasswordBtn && passwordInput && toggleIcon) {
                togglePasswordBtn.addEventListener('click', function () {
                    var isPassword = passwordInput.getAttribute('type') === 'password';
                    passwordInput.setAttribute('type', isPassword ? 'text' : 'password');
                    if (isPassword) {
                        toggleIcon.classList.remove('bi-eye-slash');
                        toggleIcon.classList.add('bi-eye');
                    } else {
                        toggleIcon.classList.remove('bi-eye');
                        toggleIcon.classList.add('bi-eye-slash');
                    }
                });
            }

            document.querySelectorAll('a[href^="#"]').forEach(function (anchor) {
                anchor.addEventListener('click', function (e) {
                    var targetId = this.getAttribute('href');
                    if (targetId && targetId !== '#') {
                        e.preventDefault();
                        var targetElement = document.querySelector(targetId);
                        if (targetElement) {
                            targetElement.scrollIntoView({ behavior: 'smooth' });
                        }
                    }
                });
            });
        });
    </script>
</body>
</html>
