package data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import dbutil.DbUtil;

import java.sql.*;
import java.util.*;

/**
 * 生产模拟报文
 */
public class MessageGen {

    private HikariDataSource dataSource = DbUtil.getDataSource();

    /**
     * 使用uuid生成key
     * @return
     */
    private String genKey() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    /**
     * 根据key和sort生产value
     */
    private String genValue(String key, int sort) {
        return key + "[" + sort + "]~";
    }


    private List<MessageBean> genOneMsg() {
        List<MessageBean> mblist = new ArrayList<MessageBean>();

        String key = this.genKey();
        Random rd = new Random();
        int len = rd.nextInt(10) + 1;

        for(int i = 0; i < len; i++) {
            MessageBean mb = new MessageBean();
            mb.setKey(key);
            mb.setLen(len);
            mb.setSort(i);
            mb.setValue(this.genValue(key, i));
            mblist.add(mb);
        }

        return mblist;
    }

    /**
     * @param number  报文个数
     */
    public List<MessageBean> genMsgs(int number) {

        List<MessageBean> datalist = new ArrayList<>();

        for(int i = 0; i < number; i++) {
            datalist.addAll(this.genOneMsg());
        }

        Collections.shuffle(datalist);

        return datalist;
    }


    public void insertDb(final List<MessageBean> datalist) throws SQLException {

        Connection conn = this.dataSource.getConnection();

        conn.setAutoCommit(false);

        String sql = "insert into message (k, v, sort, len) values (?, ?, ?, ?)";
        PreparedStatement pst = conn.prepareStatement(sql);

        for (MessageBean mb: datalist) {

            pst.setString(1, mb.getKey());
            pst.setString(2, mb.getValue());
            pst.setInt(3, mb.getSort());
            pst.setInt(4, mb.getLen());

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


    public List<String> getMessageJsonFromDB() throws SQLException, JsonProcessingException {

        Connection conn = this.dataSource.getConnection();

        String sql = "select k, v, sort, len from message";

        Statement st = conn.createStatement();

        ResultSet rs = st.executeQuery(sql);

        List<String> res = new ArrayList<String>();

        ObjectMapper obm = new ObjectMapper();

        while (rs.next()) {

            MessageBean mb = new MessageBean();

            mb.setKey(rs.getString("k"));
            mb.setValue(rs.getString("v"));
            mb.setSort(rs.getInt("sort"));
            mb.setLen(rs.getInt("len"));

            res.add(obm.writeValueAsString(mb));
        }

        return res;

    }

    public static void main(String[] args) throws SQLException, JsonProcessingException {

        MessageGen mg = new MessageGen();
//        List<MessageBean> datas = mg.genMsgs(1000);
//        mg.insertDb(datas);


        List<String> d = mg.getMessageJsonFromDB();
        for(int i = 0; i < 10; i++) {
            System.out.println(d.get(i));
        }
    }


}
