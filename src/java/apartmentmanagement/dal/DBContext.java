package apartmentmanagement.dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Lop nen JDBC cung cap ket noi SQL Server cho cac DAO.
 * <p>
 * Cau hinh ket noi duoc hardcode (khong doc {@code db.properties}).
 * Can chinh {@code DB_URL}, {@code DB_USER}, {@code DB_PASSWORD} theo moi truong local truoc khi chay.
 */
public class DBContext {

    protected Connection connection;
    protected PreparedStatement statement;
    protected ResultSet resultSet;

    /** Chuoi ket noi SQL Server – chinh host/port/databaseName theo may local. */
    private static final String DB_URL =
            "jdbc:sqlserver://localhost:1433;databaseName=ApartmentManagement;encrypt=true;trustServerCertificate=true";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "123";

    /**
     * Khoi tao context; connection chi duoc mo khi goi {@link #getConnection()}.
     */
    public DBContext() {
    }

    /**
     * Mo va tra ve mot {@link Connection} moi toi SQL Server.
     * Caller (DAO) chiu trach nhiem dong connection trong {@code finally} hoac qua {@link #closeResources()}.
     *
     * @return connection hop le, hoac {@code null} neu thieu driver / loi SQL
     */
    public Connection getConnection() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            return connection;
        } catch (ClassNotFoundException e) {
            System.out.println("DBContext: thieu driver JDBC SQL Server (mssql-jdbc). " + e.getMessage());
            return null;
        } catch (SQLException e) {
            System.out.println("DBContext getConnection error: " + e.getMessage());
            System.out.println("  -> Kiem tra SQL Server dang chay, DB ApartmentManagement, user/pass trong DBContext.");
            return null;
        }
    }

    /**
     * Kiem tra nhanh kha nang ket noi DB (dung cho Auth / health check).
     * Tu dong connection tam sau khi kiem tra.
     *
     * @return {@code true} neu mo duoc connection va connection chua dong
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
     * Dong lan luot {@link ResultSet}, {@link PreparedStatement} va {@link Connection}
     * dang giu tren instance; bo qua loi dong tung tai nguyen de khong che loi nghiep vu.
     * Thu tu dong: ResultSet -> Statement -> Connection.
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
     * Smoke-test thu cong: mo connection, in ten database hien tai, roi dong tai nguyen.
     *
     * @param args khong su dung
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
