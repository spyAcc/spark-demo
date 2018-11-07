package dbutil;

import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DbUtil {

    private final static String url = "jdbc:mysql://localhost:3306/spark";
    private final static String user = "root";
    private final static String password = "123456";
    private final static String driver = "com.mysql.jdbc.Driver";

    private static HikariDataSource dataSource = null;

    static {
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driver);
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(2);
    }

    public static Connection getConnection() {

        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static HikariDataSource getDataSource() {
        return dataSource;
    }


}
