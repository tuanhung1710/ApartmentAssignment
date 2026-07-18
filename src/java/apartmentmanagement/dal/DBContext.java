package apartmentmanagement.dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Lớp nền JDBC cung cấp kết nối SQL Server cho các DAO.
 * <p>
 * Cấu hình kết nối được hardcode (không đọc {@code db.properties}).
 * Cần chỉnh {@code DB_URL}, {@code DB_USER}, {@code DB_PASSWORD} theo môi trường local trước khi chạy.
 */
public class DBContext {

    protected Connection connection;
    protected PreparedStatement statement;
    protected ResultSet resultSet;

    /** Chuỗi kết nối SQL Server – chỉnh host/port/databaseName theo máy local. */
    private static final String DB_URL =
            "jdbc:sqlserver://localhost:1433;databaseName=ApartmentManagement;encrypt=true;trustServerCertificate=true";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "123";

    /**
     * Khởi tạo context; connection chỉ được mở khi gọi {@link #getConnection()}.
     */
    public DBContext() {
    }

    /**
     * Mở và trả về một {@link Connection} mới tới SQL Server.
     * Caller (DAO) chịu trách nhiệm đóng connection trong {@code finally} hoặc qua {@link #closeResources()}.
     *
     * @return connection hợp lệ, hoặc {@code null} nếu thiếu driver / lỗi SQL
     */
    public Connection getConnection() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            return connection;
        } catch (ClassNotFoundException e) {
            System.out.println("DBContext: thiếu driver JDBC SQL Server (mssql-jdbc). " + e.getMessage());
            return null;
        } catch (SQLException e) {
            System.out.println("DBContext getConnection error: " + e.getMessage());
            System.out.println("  → Kiểm tra SQL Server đang chạy, DB ApartmentManagement, user/pass trong DBContext.");
            return null;
        }
    }

    /**
     * Kiểm tra nhanh khả năng kết nối DB (dùng cho Auth / health check).
     * Tự đóng connection tạm sau khi kiểm tra.
     *
     * @return {@code true} nếu mở được connection và connection chưa đóng
     */
    public boolean testConnection() {
        Connection c = null;
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            return c != null && !c.isClosed();
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("DBContext.testConnection failed: " + e.getMessage());
            return false;
        } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Đóng lần lượt {@link ResultSet}, {@link PreparedStatement} và {@link Connection}
     * đang giữ trên instance; bỏ qua lỗi đóng từng tài nguyên để không che lỗi nghiệp vụ.
     * Thứ tự đóng: ResultSet → Statement → Connection.
     */
    public void closeResources() {
        try {
            if (resultSet != null) {
                resultSet.close();
                resultSet = null;
            }
        } catch (SQLException e) {
            System.out.println("Error closing ResultSet: " + e.getMessage());
        }
        try {
            if (statement != null) {
                statement.close();
                statement = null;
            }
        } catch (SQLException e) {
            System.out.println("Error closing Statement: " + e.getMessage());
        }
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            System.out.println("Error closing Connection: " + e.getMessage());
        }
    }

    /**
     * Smoke-test thủ công: mở connection, in tên database hiện tại, rồi đóng tài nguyên.
     *
     * @param args không sử dụng
     */
    public static void main(String[] args) {
        DBContext db = new DBContext();
        Connection conn = db.getConnection();
        if (conn != null) {
            System.out.println("Connected OK: " + conn);
            try {
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT DB_NAME()");
                if (rs.next()) {
                    System.out.println("Database: " + rs.getString(1));
                }
                rs.close();
                st.close();
            } catch (SQLException e) {
                System.out.println("Query error: " + e.getMessage());
            }
            db.closeResources();
        } else {
            System.out.println("Connected FAILED – check SQL Server / password");
        }
    }
}
