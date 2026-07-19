<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Trang chủ | Skyland Apartment</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/public.css" rel="stylesheet">
</head>
<body class="public-body">

<c:set var="publicActive" value="home" scope="request"/>
<%@ include file="/WEB-INF/views/auth/public-nav.jsp" %>

<%-- Hero --%>
<section class="public-hero">
    <div class="container">
        <div class="row align-items-center">
            <div class="col-lg-7 col-md-10 text-center text-lg-start">
                <p class="mb-2 opacity-75 small text-uppercase fw-semibold">
                    Cổng thông tin công khai
                </p>
                <h1>Không Gian Sống Thông Minh,<br>Quản Lý Tiện Lợi</h1>
                <p class="lead mb-4">
                    Landing page giới thiệu chung cư Skyland. Khách xem thông tin public;
                    đăng nhập để dùng dashboard và chức năng theo vai trò.
                </p>
                <div class="d-flex flex-column flex-sm-row gap-2 justify-content-center justify-content-lg-start">
                    <a href="${pageContext.request.contextPath}/auth?action=login"
                       class="btn btn-light btn-lg rounded-pill px-4 fw-semibold text-primary-custom">
                        Đăng nhập hệ thống <i class="bi bi-box-arrow-in-right ms-1"></i>
                    </a>
                    <a href="#announcements" class="btn btn-outline-light btn-lg rounded-pill px-4">
                        Xem thông báo
                    </a>
                </div>
            </div>
        </div>
    </div>
</section>

<%-- ABOUT --%>
<section id="about" class="public-section alt">
    <div class="container">
        <div class="row g-4 align-items-center">
            <div class="col-lg-6">
                <p class="text-primary-custom fw-semibold small text-uppercase mb-2">About</p>
                <h2 class="section-title">Giới thiệu</h2>
                <p class="section-sub mb-3">
                    Skyland Apartment Management là nền tảng số hóa vận hành chung cư —
                    hỗ trợ cư dân, ban quản lý và nhân viên kỹ thuật trên cùng một hệ thống.
                </p>
                <ul class="list-unstyled mb-0">
                    <li class="d-flex gap-2 mb-2">
                        <i class="bi bi-check-circle-fill text-primary-custom mt-1"></i>
                        <span>Quản lý căn hộ, thành viên hộ và hồ sơ cư dân</span>
                    </li>
                    <li class="d-flex gap-2 mb-2">
                        <i class="bi bi-check-circle-fill text-primary-custom mt-1"></i>
                        <span>Quản lý phí dịch vụ, nước, gửi xe theo tháng (sau đăng nhập)</span>
                    </li>
                    <li class="d-flex gap-2 mb-2">
                        <i class="bi bi-check-circle-fill text-primary-custom mt-1"></i>
                        <span>Gửi / duyệt yêu cầu sửa chữa, chuyển đồ, trông xe</span>
                    </li>
                    <li class="d-flex gap-2">
                        <i class="bi bi-check-circle-fill text-primary-custom mt-1"></i>
                        <span>Thông báo nội quy, bảo trì và lịch thu phí kịp thời</span>
                    </li>
                </ul>
            </div>
            <div class="col-lg-6">
                <div class="row g-3">
                    <div class="col-6">
                        <div class="public-card text-center">
                            <div class="icon-circle mx-auto"><i class="bi bi-shield-check"></i></div>
                            <h3 class="h6 fw-bold">An toàn</h3>
                            <p class="small text-muted mb-0">Đăng nhập và phân quyền theo vai trò</p>
                        </div>
                    </div>
                    <div class="col-6">
                        <div class="public-card text-center">
                            <div class="icon-circle mx-auto"><i class="bi bi-phone"></i></div>
                            <h3 class="h6 fw-bold">Đa thiết bị</h3>
                            <p class="small text-muted mb-0">Responsive mobile / tablet / desktop</p>
                        </div>
                    </div>
                    <div class="col-6">
                        <div class="public-card text-center">
                            <div class="icon-circle mx-auto"><i class="bi bi-people"></i></div>
                            <h3 class="h6 fw-bold">Đa vai trò</h3>
                            <p class="small text-muted mb-0">Admin, Manager, Staff, Resident</p>
                        </div>
                    </div>
                    <div class="col-6">
                        <div class="public-card text-center">
                            <div class="icon-circle mx-auto"><i class="bi bi-buildings"></i></div>
                            <h3 class="h6 fw-bold">Tiện ích</h3>
                            <p class="small text-muted mb-0">Hồ bơi, gym, bãi đỗ xe, an ninh 24/7</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<%-- Thông báo public --%>
<section id="announcements" class="public-section alt">
    <div class="container">
        <h2 class="section-title mb-1">Thông báo</h2>
        <p class="section-sub">Nội dung đã công bố — xem không cần đăng nhập</p>

        <c:choose>
            <c:when test="${empty announcements}">
                <div class="public-card">
                    <p class="text-muted mb-0">
                        <i class="bi bi-inbox me-1"></i>
                        Chưa có thông báo công khai. Vui lòng quay lại sau.
                    </p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="row g-3">
                    <c:forEach var="a" items="${announcements}">
                        <div class="col-12 col-md-6">
                            <article class="announcement-item h-100">
                                <div class="d-flex justify-content-between gap-2 flex-wrap">
                                    <span class="cat">
                                        <c:choose>
                                            <c:when test="${a.category == 'MOVE_RULE'}">Nội quy chuyển đồ</c:when>
                                            <c:when test="${a.category == 'FEE'}">Phí dịch vụ</c:when>
                                            <c:when test="${a.category == 'MAINTENANCE'}">Bảo trì</c:when>
                                            <c:otherwise>Chung</c:otherwise>
                                        </c:choose>
                                    </span>
                                    <c:if test="${not empty a.publishedAt}">
                                        <span class="small text-muted">
                                            <t:rt value="${a.publishedAt}" mode="full"/>
                                        </span>
                                    </c:if>
                                </div>
                                <h3 class="h6 fw-bold mt-1 mb-1">${a.title}</h3>
                                <p class="content-text">${a.content}</p>
                            </article>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</section>

<%-- FAQ --%>
<section id="faq" class="public-section">
    <div class="container">
        <h2 class="section-title">FAQ</h2>
        <p class="section-sub">Câu hỏi thường gặp</p>
        <div class="accordion" id="faqAccordion">
            <div class="accordion-item faq-item border-0">
                <h3 class="accordion-header">
                    <button class="accordion-button rounded-3" type="button" data-bs-toggle="collapse"
                            data-bs-target="#faq1" aria-expanded="true" aria-controls="faq1">
                        Chưa đăng nhập xem được gì?
                    </button>
                </h3>
                <div id="faq1" class="accordion-collapse collapse show" data-bs-parent="#faqAccordion">
                    <div class="accordion-body text-muted">
                        Trang giới thiệu (About) và <strong>thông báo công khai</strong>.
                        Xem phí, yêu cầu và dashboard theo vai trò cần đăng nhập.
                    </div>
                </div>
            </div>
            <div class="accordion-item faq-item border-0">
                <h3 class="accordion-header">
                    <button class="accordion-button collapsed rounded-3" type="button" data-bs-toggle="collapse"
                            data-bs-target="#faq2" aria-expanded="false" aria-controls="faq2">
                        Sau khi đăng nhập vào đâu?
                    </button>
                </h3>
                <div id="faq2" class="accordion-collapse collapse" data-bs-parent="#faqAccordion">
                    <div class="accordion-body text-muted">
                        Hệ thống chuyển tới <strong>Dashboard</strong> theo vai trò:
                        Resident Dashboard hoặc Admin / Manager / Staff Dashboard.
                    </div>
                </div>
            </div>
            <div class="accordion-item faq-item border-0">
                <h3 class="accordion-header">
                    <button class="accordion-button collapsed rounded-3" type="button" data-bs-toggle="collapse"
                            data-bs-target="#faq3" aria-expanded="false" aria-controls="faq3">
                        Tài khoản lấy ở đâu?
                    </button>
                </h3>
                <div id="faq3" class="accordion-collapse collapse" data-bs-parent="#faqAccordion">
                    <div class="accordion-body text-muted">
                        Ban Quản Lý cấp sau thủ tục nhận căn / hợp đồng. Không mở đăng ký public trên web.
                    </div>
                </div>
            </div>
            <div class="accordion-item faq-item border-0">
                <h3 class="accordion-header">
                    <button class="accordion-button collapsed rounded-3" type="button" data-bs-toggle="collapse"
                            data-bs-target="#faq4" aria-expanded="false" aria-controls="faq4">
                        Quên mật khẩu thì làm sao?
                    </button>
                </h3>
                <div id="faq4" class="accordion-collapse collapse" data-bs-parent="#faqAccordion">
                    <div class="accordion-body text-muted">
                        Vào <strong>Quên mật khẩu</strong> trên form đăng nhập, xác thực bằng email/SĐT đã đăng ký
                        và nhập mã OTP demo để đặt mật khẩu mới. Hotline hỗ trợ: 1900 1234.
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<%-- Liên hệ --%>
<section id="contact" class="public-section alt">
    <div class="container">
        <h2 class="section-title">Liên hệ</h2>
        <p class="section-sub">Ban Quản Lý Skyland luôn sẵn sàng hỗ trợ</p>
        <div class="row g-3 g-lg-4">
            <div class="col-md-4">
                <div class="public-card">
                    <div class="icon-circle"><i class="bi bi-geo-alt"></i></div>
                    <h3 class="h6 fw-bold">Địa chỉ</h3>
                    <p class="small text-muted mb-0">Đại học FPT, Hòa Lạc, Thạch Thất, Hà Nội</p>
                </div>
            </div>
            <div class="col-md-4">
                <div class="public-card">
                    <div class="icon-circle"><i class="bi bi-telephone"></i></div>
                    <h3 class="h6 fw-bold">Hotline</h3>
                    <p class="small text-muted mb-0">
                        <a class="text-decoration-none text-primary-custom" href="tel:19001234">1900 1234</a> (24/7)
                    </p>
                </div>
            </div>
            <div class="col-md-4">
                <div class="public-card">
                    <div class="icon-circle"><i class="bi bi-envelope"></i></div>
                    <h3 class="h6 fw-bold">Email</h3>
                    <p class="small text-muted mb-0">Group6_SE2036@gmail.com</p>
                </div>
            </div>
        </div>

        <div class="text-center mt-4 pt-2">
            <a href="${pageContext.request.contextPath}/auth?action=login"
               class="btn btn-primary-custom btn-lg rounded-pill px-4 shadow-sm">
                Đăng nhập để dùng chức năng <i class="bi bi-arrow-right ms-1"></i>
            </a>
        </div>
    </div>
</section>

<%@ include file="/WEB-INF/views/auth/public-footer.jsp" %>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    document.querySelectorAll('a[href*="#"]').forEach(function (anchor) {
        anchor.addEventListener('click', function (e) {
            var href = this.getAttribute('href') || '';
            var hashIdx = href.indexOf('#');
            if (hashIdx < 0) {
                return;
            }
            var hash = href.substring(hashIdx);
            if (!hash || hash === '#') {
                return;
            }
            var pathPart = href.substring(0, hashIdx);
            if (pathPart && pathPart.indexOf('action=home') < 0 && pathPart.charAt(0) !== '#') {
                return;
            }
            var target = document.querySelector(hash);
            if (target) {
                e.preventDefault();
                target.scrollIntoView({ behavior: 'smooth' });
            }
        });
    });
</script>
</body>
</html>
