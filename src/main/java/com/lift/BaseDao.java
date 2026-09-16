package com.lift;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class BaseDao {
    public Connection getConnect() {
        Connection connection = null;
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432", "postgres", "postgres");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }

    public void save(String storeycombin, double sumtime) {
        Statement statement = null;
        for (int i = 0; i < 1; i++) {
            String sql = "insert into lifttest(storeycombin,sumtime)value(?,?)";
            try {
                PreparedStatement preparedStatement = getConnect().prepareStatement(sql);
                preparedStatement.setString(1, storeycombin);
                preparedStatement.setDouble(2, sumtime);
                preparedStatement.execute();
                preparedStatement.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}