<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<footer class="public-footer">
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
                    Hòa Lạc, Thạch Thất, Hà Nội
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
                <h5 class="mb-3">Liên kết nhanh</h5>
                <div class="d-flex flex-column align-items-lg-end gap-1 mb-3">
                    <a href="${pageContext.request.contextPath}/auth?action=home">Trang chủ</a>
                    <a href="${pageContext.request.contextPath}/auth?action=home#about">Giới thiệu</a>
                    <a href="${pageContext.request.contextPath}/auth?action=home#announcements">Thông báo</a>
                    <a href="${pageContext.request.contextPath}/auth?action=login">Đăng nhập</a>
                </div>
                <div class="d-flex justify-content-lg-end">
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
