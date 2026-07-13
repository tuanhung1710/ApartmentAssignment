<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Điều khoản dịch vụ | Skyland Apartment</title>
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
           href="${pageContext.request.contextPath}/auth?action=login">
            <i class="bi bi-buildings-fill fs-3 me-2"></i> Skyland Apartment
        </a>
        <div class="ms-auto">
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
            <h1><i class="bi bi-file-text me-2"></i>Điều khoản dịch vụ</h1>
            <p class="legal-meta mb-0">
                Cập nhật lần cuối: 13/07/2026 · Áp dụng cho hệ thống quản lý Skyland Apartment
            </p>

            <p>
                Chào mừng bạn đến với hệ thống quản lý Skyland Apartment. Khi đăng nhập và sử dụng
                hệ thống, bạn đồng ý tuân thủ các điều khoản dưới đây. Vui lòng đọc kỹ trước khi sử dụng.
            </p>

            <h2>1. Giới thiệu dịch vụ</h2>
            <p>
                Skyland Apartment là nền tảng web hỗ trợ quản lý vận hành chung cư: đăng nhập theo vai trò,
                quản lý hồ sơ, căn hộ, phí dịch vụ và yêu cầu hỗ trợ. Hệ thống được phát triển trong khuôn khổ
                môn học PRJ301 (Nhóm 6 – SE2036) nhằm mục đích học tập và demo.
            </p>

            <h2>2. Tài khoản người dùng</h2>
            <ul>
                <li>Tài khoản do ban quản lý / admin cấp; người dùng không tự đăng ký công khai.</li>
                <li>Bạn có trách nhiệm bảo mật username và mật khẩu; không chia sẻ cho người khác.</li>
                <li>Mọi hoạt động phát sinh từ tài khoản được xem là do chủ tài khoản thực hiện.</li>
                <li>Tài khoản có thể bị khóa nếu vi phạm quy định hoặc theo quyết định của admin.</li>
            </ul>

            <h2>3. Vai trò và phạm vi sử dụng</h2>
            <ul>
                <li><strong>RESIDENT:</strong> xem căn hộ/phí của mình, gửi và theo dõi yêu cầu hỗ trợ.</li>
                <li><strong>STAFF / MANAGER:</strong> xử lý nghiệp vụ vận hành theo phân quyền (căn hộ, phí, yêu cầu…).</li>
                <li><strong>ADMIN:</strong> quản trị người dùng và cấu hình hệ thống theo module được triển khai.</li>
            </ul>
            <p>
                Người dùng chỉ được truy cập các chức năng đúng với role được cấp. Cố ý vượt quyền
                (nếu có) là hành vi bị nghiêm cấm.
            </p>

            <h2>4. Quy tắc sử dụng</h2>
            <ul>
                <li>Cung cấp thông tin hồ sơ trung thực khi cập nhật (họ tên, email, SĐT).</li>
                <li>Không gửi yêu cầu giả mạo, spam, nội dung xúc phạm hoặc bất hợp pháp.</li>
                <li>Không tấn công, dò quét, làm gián đoạn hệ thống hoặc dữ liệu của người khác.</li>
                <li>Không sử dụng hệ thống cho mục đích thương mại trái phép ngoài phạm vi demo/học tập.</li>
            </ul>

            <h2>5. Nội dung và yêu cầu của cư dân</h2>
            <p>
                Yêu cầu do cư dân gửi (sửa chữa, hỗ trợ…) sẽ được staff/manager tiếp nhận và cập nhật trạng thái.
                Thời gian xử lý phụ thuộc quy trình ban quản lý và không được cam kết SLA cố định trong môi trường demo.
            </p>

            <h2>6. Phí dịch vụ (nếu áp dụng)</h2>
            <p>
                Thông tin phí hiển thị trên hệ thống mang tính quản lý / tham khảo trong dự án.
                Việc thanh toán thực tế (nếu có) được thực hiện theo quy định ban quản lý ngoài hệ thống
                trừ khi module thanh toán được triển khai rõ ràng.
            </p>

            <h2>7. Sở hữu trí tuệ</h2>
            <p>
                Giao diện, mã nguồn và tài liệu thuộc nhóm phát triển dự án PRJ301 (trừ thư viện mã nguồn mở
                và tài nguyên ảnh/CDN được sử dụng theo giấy phép tương ứng). Không sao chép toàn bộ
                để thương mại hóa khi chưa được phép.
            </p>

            <h2>8. Miễn trừ trách nhiệm</h2>
            <ul>
                <li>Hệ thống mang tính học thuật/demo; có thể có lỗi, dữ liệu mẫu hoặc gián đoạn.</li>
                <li>Chúng tôi không chịu trách nhiệm với thiệt hại phát sinh do sử dụng sai, lộ mật khẩu,
                    hoặc dữ liệu demo không phản ánh vận hành thực tế.</li>
                <li>Không dùng hệ thống để lưu thông tin nhạy cảm ngoài phạm vi bài tập (ví dụ: số thẻ tín dụng thật).</li>
            </ul>

            <h2>9. Tạm ngưng và chấm dứt</h2>
            <p>
                Admin/ban quản lý có quyền tạm khóa tài khoản hoặc dừng cung cấp dịch vụ khi cần bảo trì,
                bảo mật, hoặc kết thúc học kỳ/demo. Người dùng có thể ngừng sử dụng bằng cách đăng xuất
                và không truy cập lại.
            </p>

            <h2>10. Luật áp dụng</h2>
            <p>
                Các điều khoản được soạn cho mục đích học tập tại Việt Nam. Nếu có mâu thuẫn giữa bản tiếng Việt
                và mô tả demo khác, ưu tiên nội dung hiển thị trên trang này.
            </p>

            <h2>11. Liên hệ</h2>
            <ul>
                <li>Email: Group6_SE2036@gmail.com</li>
                <li>Địa chỉ: Đại học FPT, Hòa Lạc, Thạch Thất, Hà Nội</li>
                <li>Hotline demo: 1900 1234</li>
            </ul>

            <p class="mb-0">
                Bằng việc tiếp tục đăng nhập và sử dụng Skyland Apartment, bạn xác nhận đã đọc và đồng ý
                với Điều khoản dịch vụ cùng <a class="text-primary-custom" href="${pageContext.request.contextPath}/auth?action=privacy">Chính sách bảo mật</a>.
            </p>

            <div class="mt-4 pt-3 border-top d-flex flex-wrap gap-2">
                <a class="btn btn-outline-primary-custom rounded-pill px-4"
                   href="${pageContext.request.contextPath}/auth?action=login">
                    <i class="bi bi-arrow-left me-1"></i> Về trang đăng nhập
                </a>
                <a class="btn btn-link text-primary-custom"
                   href="${pageContext.request.contextPath}/auth?action=privacy">
                    Xem Chính sách bảo mật
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
