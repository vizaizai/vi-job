package com.github.vizaizai.server.utils;

import com.github.vizaizai.logging.LoggerFactory;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * h2数据库工具
 * @author liaochongwei
 * @date 2023/7/10 16:07
 */
public class H2Utils {
    private static final String dbUrl = "jdbc:h2:~/vi_job_log";
    private static final String dbUserName = "sa";
    private static final String dbPassword = "sa";

    public static Connection getConnection()throws Exception{
        return DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
    }

    public static void main(String[] args) throws Exception {
        Connection connection = getConnection();

        Statement stmt = connection.createStatement();
        //stmt.execute("CREATE TABLE MY_USER(ID VARCHAR(10) PRIMARY KEY,NAME VARCHAR(50))"); //创建表
//        stmt.executeUpdate("INSERT INTO MY_USER VALUES('001','刘备')"); //插入数据
//        stmt.executeUpdate("INSERT INTO MY_USER VALUES('002','关羽')");
//        stmt.executeUpdate("INSERT INTO MY_USER VALUES('003','张飞')");

        ResultSet rs = stmt.executeQuery("SELECT NAME FROM MY_USER WHERE ID='001'"); //查询
        while(rs.next()){
            String name = rs.getString("NAME");
            Assertions.assertTrue(name.equals("刘备"));
        }
        rs.close();
        stmt.close();
        connection.close();


        Logger logger = LoggerFactory.getLogger(String.class);

        //logger.
    }
}
