package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBHelpers {
    public static Connection makeConnection() throws SQLException, ClassNotFoundException {
        Connection con = null;

//        String driverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
//        Class.forName(driverClassName);
        String url = "jdbc:sqlserver://haseoleonardtrace.ddns.net:1433;databaseName=HomeDB";
        con = DriverManager.getConnection(url,"sa","14021998");
        return con;
    }
}
