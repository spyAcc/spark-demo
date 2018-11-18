package kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import data.LogBean;
import dbutil.DbUtil;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 发送json数据
 */
public class KafkaProducerApp02 {

    public static HikariDataSource getHikariDataSource() {
        return DbUtil.getDataSource();
    }

    public static List<LogBean> getDatas() throws SQLException {

        Connection conn = getHikariDataSource().getConnection();

        Statement st = conn.createStatement();

        String sql = "select * from log limit 100";

        ResultSet rs = st.executeQuery(sql);

        List<LogBean> datalist = new ArrayList<>();

        while (rs.next()) {
            LogBean d = new LogBean();
            d.setId(rs.getInt("id"));
            d.setType(rs.getString("type"));
            d.setLevel(rs.getString("level"));
            d.setIp(rs.getString("ip"));
            d.setUrl(rs.getString("url"));
            d.setTimes(rs.getString("times"));
            datalist.add(d);
        }

        if(rs != null) {
            rs.close();
        }

        if(conn != null) {
            conn.close();
        }

        return datalist;
    }

    public static void main(String[] args) throws SQLException, JsonProcessingException {

        Properties prop = new Properties();
        //多个broker，用逗号分隔
        prop.put("bootstrap.servers", "localhost:9092");
        prop.put("batch.size", 5);
        prop.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        prop.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<String, String>(prop);


        List<LogBean> datas = getDatas();

        ObjectMapper mapper = new ObjectMapper();

        for(int i = 0; i < datas.size(); i++) {
            producer.send(new ProducerRecord<>("test", Integer.toString(i), mapper.writeValueAsString(datas.get(i)) ));
        }

        producer.close();



    }




}
