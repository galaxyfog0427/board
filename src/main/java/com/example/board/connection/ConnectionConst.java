package com.example.board.connection;

public class ConnectionConst {

    public static final String URL = "jdbc:mysql://localhost:3306/board";
    public static final String USERNAME = "root";
    public static final String PASSWORD = System.getenv("DB_PASSWORD");

}
