<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chính sách bảo mật | Skyland Apartment</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <style>
        * { box-sizing: border-box; }
        html, body { margin: 0; padding: 0; width: 100%; overflow-x: hidden; }
        body {
            font-family: 'Inter', system-ui, -apple-system, sans-serif;
            background-color: #f8f9fa;
            color: #212529;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
        }
        .text-primary-custom { color: #1a4388 !important; }
        .btn-outline-primary-custom {
            color: #1a4388;
            border-color: #1a4388;
            font-weight: 500;
        }
        .btn-outline-primary-custom:hover {
            background-color: #1a4388;
            color: #fff;
        }
        .navbar {
            width: 100%;
            box-shadow: 0 2px 15px rgba(0,0,0,0.08);
            background: #fff !important;
        }
        .legal-main { flex: 1; padding: 2.5rem 0 3rem; }
        .legal-card {
            background: #fff;
            border-radius: 16px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.08);
            padding: 2.5rem;
        }
        .legal-card h1 {
            font-weight: 700;
            color: #1a4388;
            font-size: 1.85rem;
            margin-bottom: 0.5rem;
        }
        .legal-meta {
            color: #6c757d;
            font-size: 0.9rem;
            margin-bottom: 1.75rem;
            padding-bottom: 1rem;
            border-bottom: 1px solid #e9ecef;
        }
        .legal-card h2 {
            font-size: 1.15rem;
            font-weight: 600;
            color: #1e293b;
            margin-top: 1.75rem;
            margin-bottom: 0.75rem;
        }
        .legal-card p, .legal-card li {
            color: #475569;
            line-height: 1.7;
            font-size: 0.98rem;
        }
        .legal-card ul { padding-left: 1.25rem; }
        .legal-card li { margin-bottom: 0.4rem; }
        .footer {
            width: 100%;
            background-color: #111827;
            color: #9ca3af;
            padding: 40px 0 24px;
            margin-top: auto;
        }
        .footer a {
            color: #9ca3af;
            text-decoration: none;
        }
        .footer a:hover { color: #fff; }
        @media (max-width: 767.98px) {
            .legal-card { padding: 1.5rem; }
        }
    </style>
</head>
<body>

<nav class="navbar navbar-expand-lg navbar-light bg-white sticky-top py-2">
    <div class="container">
        <a class="navbar-brand text-primary-custom fw-bold fs-4 d-flex align-items-center"
           href="${pageContext.request.contextPath}/auth?action=home">
            <i class="bi bi-buildings-fill fs-3 me-2"></i> Skyland Apartment
        </a>
        <div class="ms-auto d-flex flex-wrap gap-2">
            <a class="btn btn-link text-primary-custom text-decoration-none"
               href="${pageContext.request.contextPath}/auth?action=home">Trang chủ</a>
            <a class="btn btn-outline-primary-custom px-4 py-2 rounded-pill"
               href="${pageContext.request.contextPath}/auth?action=login">
                <i class="bi bi-box-arrow-in-right me-1"></i> Đăng nhập
            </a>
        </div>
    </div>
</nav>

<main class="legal-main">
    <div class="container" style="max-width: 860px;">
        <div class="legal-card">
            <h1><i class="bi bi-shield-lock me-2"></i>Chính sách bảo mật</h1>
            <p class="legal-meta mb-0">
                Cập nhật lần cuối: 13/07/2026 · Áp dụng cho hệ thống quản lý Skyland Apartment
            </p>

            <p>
                Skyland Apartment (“chúng tôi”) cam kết bảo vệ thông tin cá nhân của cư dân,
                ban quản lý và nhân viên khi sử dụng hệ thống quản lý căn hộ. Chính sách này
                giải thích loại dữ liệu chúng tôi thu thập, mục đích sử dụng và cách bảo vệ dữ liệu.
            </p>

            <h2>1. Phạm vi áp dụng</h2>
            <p>
                Chính sách áp dụng cho mọi người dùng truy cập và sử dụng hệ thống quản lý
                Skyland Apartment (web app), bao gồm tài khoản cư dân, staff, manager và admin.
            </p>

            <h2>2. Dữ liệu chúng tôi thu thập</h2>
            <ul>
                <li><strong>Thông tin tài khoản:</strong> username, họ tên, email, số điện thoại, vai trò (role).</li>
                <li><strong>Thông tin vận hành:</strong> căn hộ được gán, yêu cầu hỗ trợ/sửa chữa, lịch sử xử lý.</li>
                <li><strong>Thông tin phí (nếu có):</strong> kỳ phí, trạng thái thanh toán, ghi chú liên quan.</li>
                <li><strong>Dữ liệu kỹ thuật cơ bản:</strong> thời điểm đăng nhập, phiên làm việc (session) để duy trì trạng thái đăng nhập.</li>
            </ul>
            <p class="mb-0">
                Chúng tôi <strong>không</strong> thu thập dữ liệu thanh toán thẻ ngân hàng trực tiếp trên form đăng nhập.
                Mật khẩu được lưu trữ phục vụ xác thực nội bộ theo quy trình của hệ thống.
            </p>

            <h2>3. Mục đích sử dụng dữ liệu</h2>
            <ul>
                <li>Xác thực đăng nhập và phân quyền theo vai trò.</li>
                <li>Quản lý hồ sơ cư dân, căn hộ, yêu cầu và phí dịch vụ.</li>
                <li>Liên hệ hỗ trợ kỹ thuật / ban quản lý khi cần.</li>
                <li>Cải thiện trải nghiệm sử dụng và bảo mật hệ thống.</li>
                <li>Phục vụ demo, học tập, đánh giá môn học PRJ301 (môi trường giáo dục).</li>
            </ul>

            <h2>4. Cơ sở và thời gian lưu trữ</h2>
            <p>
                Dữ liệu được lưu trên cơ sở dữ liệu nội bộ của dự án (SQL Server) trong thời gian
                người dùng còn tài khoản hoạt động, hoặc cho đến khi ban quản lý/admin xóa hoặc
                vô hiệu hóa tài khoản. Dữ liệu demo có thể được reset theo nhu cầu môn học.
            </p>

            <h2>5. Chia sẻ thông tin</h2>
            <p>
                Chúng tôi không bán hay cho thuê dữ liệu cá nhân. Thông tin chỉ được chia sẻ trong
                phạm vi cần thiết giữa các vai trò nội bộ (ví dụ: staff/manager xem yêu cầu của cư dân
                để xử lý), hoặc khi pháp luật yêu cầu.
            </p>

            <h2>6. Bảo mật</h2>
            <ul>
                <li>Truy cập hệ thống yêu cầu đăng nhập; các trang nội bộ được bảo vệ bằng filter phân quyền.</li>
                <li>Phiên đăng nhập lưu trên server (HttpSession); mật khẩu không hiển thị trong session/UI.</li>
                <li>Khuyến nghị người dùng không chia sẻ tài khoản, đăng xuất sau khi dùng máy chung.</li>
                <li>Đây là hệ thống demo học thuật — không dùng mật khẩu thật của tài khoản ngân hàng/email cá nhân quan trọng.</li>
            </ul>

            <h2>7. Quyền của người dùng</h2>
            <ul>
                <li>Xem và cập nhật hồ sơ cá nhân (họ tên, email, SĐT) trong mục Hồ sơ.</li>
                <li>Đổi mật khẩu khi đã đăng nhập.</li>
                <li>Yêu cầu ban quản lý hỗ trợ khóa/mở hoặc điều chỉnh tài khoản nếu cần.</li>
            </ul>

            <h2>8. Cookie và lưu trữ trình duyệt</h2>
            <p>
                Hệ thống chủ yếu dùng session phía server. Các tùy chọn giao diện “Nhớ tài khoản”
                (nếu bật) chỉ mang tính hỗ trợ UX trên trình duyệt và không thay thế cơ chế bảo mật.
            </p>

            <h2>9. Liên hệ</h2>
            <p>
                Mọi thắc mắc về bảo mật dữ liệu, vui lòng liên hệ nhóm phát triển:
            </p>
            <ul>
                <li>Email: Group6_SE2036@gmail.com</li>
                <li>Địa chỉ: Đại học FPT, Hòa Lạc, Thạch Thất, Hà Nội</li>
                <li>Hotline demo: 1900 1234</li>
            </ul>

            <h2>10. Cập nhật chính sách</h2>
            <p class="mb-0">
                Chúng tôi có thể cập nhật nội dung chính sách này khi hệ thống thay đổi.
                Phiên bản mới sẽ được hiển thị trên trang này kèm ngày cập nhật.
            </p>

            <div class="mt-4 pt-3 border-top d-flex flex-wrap gap-2">
                <a class="btn btn-outline-primary-custom rounded-pill px-4"
                   href="${pageContext.request.contextPath}/auth?action=login">
                    <i class="bi bi-arrow-left me-1"></i> Về trang đăng nhập
                </a>
                <a class="btn btn-link text-primary-custom"
                   href="${pageContext.request.contextPath}/auth?action=terms">
                    Xem Điều khoản dịch vụ
                </a>
            </div>
        </div>
    </div>
</main>

<footer class="footer">
    <div class="container">
        <div class="row text-center text-md-start align-items-center">
            <div class="col-md-6 mb-2 mb-md-0">
                <p class="small mb-0">&copy; 2026 Skyland Apartment · Nhóm 6 SE2036</p>
            </div>
            <div class="col-md-6 text-md-end">
                <a href="${pageContext.request.contextPath}/auth?action=privacy" class="small me-3">Chính sách bảo mật</a>
                <a href="${pageContext.request.contextPath}/auth?action=terms" class="small">Điều khoản dịch vụ</a>
            </div>
        </div>
    </div>
</footer>

</body>
</html>
