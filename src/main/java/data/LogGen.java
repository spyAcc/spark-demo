package data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import dbutil.DbUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class LogGen {

    private HikariDataSource dataSource = null;

    public void getDataSource() throws SQLException {
        dataSource = DbUtil.getDataSource();
    }


    private String [] type = new String[]{"system", "custom"};

    private String [] level = new String[]{"debug", "waring", "info", "error"};

    private String getIp() {
        Random rd = new Random();
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < 4; i++) {
            sb.append(rd.nextInt(255));
            sb.append(".");
        }

        return sb.toString().substring(0, sb.length()-1);
    }

    private String[] alpha = new String[]{"a", "b", "c", "d", "e", "f", "g",
        "h", "i", "j", "k", "l", "m" , "n", "o", "p", "q", "r", "s", "t", "u",
            "v", "w", "x", "y", "z", "1", "2", "3", "4","5","6","7","8","9","0"
    };

    private String getUrl() {
        Random rd = new Random();
        StringBuilder sb = new StringBuilder();

        sb.append("http://www.baidu.com/");
        int cut = rd.nextInt(3) + 1;
        int len = rd.nextInt(10);

        for(int j = 0; j < cut; j++) {
            for(int i = 0; i < len; i++) {
                sb.append(alpha[rd.nextInt(36)]);
            }
            sb.append("/");
        }

        return sb.toString().substring(0, sb.length()-1);
    }


    private String getTime() {
        Random rd = new Random();
        LocalDateTime ldt = LocalDateTime.now();
        LocalDateTime res = ldt.plusSeconds(rd.nextLong()%100000000);
        return res.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public void genLogs(int num) throws SQLException {

        Connection conn = this.dataSource.getConnection();

        ArrayList<LogBean> datas = new ArrayList<LogBean>();

        Random rd = new Random();

        for(int i = 0; i < num; i++) {
            LogBean lg = new LogBean();

            lg.setType(type[rd.nextInt(2)]);
            lg.setLevel(level[rd.nextInt(4)]);
            lg.setIp(this.getIp());
            lg.setUrl(this.getUrl());
            lg.setTimes(this.getTime());

            datas.add(lg);
        }

        String sql = "insert into log (type, level, ip, url, times) values (?,?,?,?,?)";
        PreparedStatement pst = conn.prepareStatement(sql);

        conn.setAutoCommit(false);

        for(LogBean logitem: datas) {

            pst.setString(1, logitem.getType());
            pst.setString(2, logitem.getLevel());
            pst.setString(3, logitem.getIp());
            pst.setString(4, logitem.getUrl());
            pst.setString(5, logitem.getTimes());

            pst.addBatch();
        }

        pst.executeBatch();
        conn.commit();

        if(pst != null) {
            pst.close();
        }
        if(conn != null) {
            conn.close();
        }

    }


    public void writeLogToText(int startnum, int endnum, String path) throws SQLException, IOException {

        Connection conn = this.dataSource.getConnection();

        String sql = "select * from log limit " + startnum  + "," + endnum;

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        FileOutputStream fos = new FileOutputStream(path);

        while(rs.next()) {
            StringBuilder sb = new StringBuilder();
            sb.append(rs.getInt(1));
            sb.append("\t");
            sb.append(rs.getString(2));
            sb.append("\t");
            sb.append(rs.getString(3));
            sb.append("\t");
            sb.append(rs.getString(4));
            sb.append("\t");
            sb.append(rs.getString(5));
            sb.append("\t");
            sb.append(rs.getDate(6));
            sb.append("\r\n");
            fos.write(sb.toString().getBytes());
        }

        fos.flush();
        fos.close();


        if(st != null) {
            st.close();
        }

        if(conn != null) {
            conn.close();
        }

    }



    public void writeLogToJson(int startnum, int endnum, String path) throws SQLException, IOException {

        Connection conn = this.dataSource.getConnection();

        String sql = "select * from log limit " + startnum  + "," + endnum;

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        ArrayList<LogBean> logBeans = new ArrayList<>();
        while(rs.next()) {
            LogBean tmp = new LogBean();
            tmp.setId(rs.getInt(1));
            tmp.setType(rs.getString(2));
            tmp.setLevel(rs.getString(3));
            tmp.setIp(rs.getString(4));
            tmp.setUrl(rs.getString(5));
            tmp.setTimes(rs.getDate(6).toString());
            logBeans.add(tmp);
        }

        ObjectMapper mapper = new ObjectMapper();

        StringBuilder sb = new StringBuilder();

        for (LogBean item: logBeans) {
            sb.append(mapper.writeValueAsString(item));
            sb.append("\r\n");
        }

        FileOutputStream fos = new FileOutputStream(path);

        fos.write(sb.toString().getBytes());

        fos.flush();
        fos.close();

        if(st != null) {
            st.close();
        }

        if(conn != null) {
            conn.close();
        }

    }


    public static void main(String[] args) throws SQLException, IOException {
//        dowrite();
//        dogen();
        dowritejson();
    }

    private static void dogen() throws SQLException {
        LogGen l = new LogGen();
        l.getDataSource();

        for(int i = 0 ; i < 10; i++) {
            l.genLogs(500);
        }
    }


    private static void dowrite() throws SQLException, IOException {
        LogGen l = new LogGen();
        l.getDataSource();
        String path = "./baidulogs.txt";
        l.writeLogToText(0, 1000, path);

    }

    private static void dowritejson() throws SQLException, IOException {
        LogGen l = new LogGen();
        l.getDataSource();
        String path = "./baidulogs.json";
        l.writeLogToJson(0, 1000, path);
    }


}
