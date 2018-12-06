package prj;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import data.MessageBean;
import dbutil.DbUtil;
import dbutil.PropertiesUtil;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 * 模拟报文的输入，从数据库中每隔是s（20）秒拿n(100)条报文
 *
 */
public class KafkaProducerMessageShuffle {


    private static final String topic = "message-shuffle";

    private static final int sleepTime = 20*1000;

    public int getCountMessageShuffle() throws SQLException {

        Connection conn = DbUtil.getConnection();

        String sql = "select count(*) as cnt from message";
        Statement st = conn.createStatement();

        int count = 0;
        ResultSet rs = st.executeQuery(sql);
        if (rs.next()) {
            count = rs.getInt("cnt");
        }

        return count;
    }

    public List<MessageBean> getMessageShuffle(int start, int length) throws SQLException {

        Connection conn = DbUtil.getConnection();

        String sql = "select * from message limit ?, ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setInt(1, start);
        pst.setInt(2, length);

        ResultSet rs = pst.executeQuery();

        List<MessageBean> reslist = new ArrayList<>();

        while (rs.next()) {
            MessageBean mb = new MessageBean();

            mb.setKey(rs.getString("k"));
            mb.setValue(rs.getString("v"));
            mb.setSort(rs.getInt("sort"));
            mb.setLen(rs.getInt("len"));

            reslist.add(mb);
        }


        if(pst != null) {
            pst.close();
        }

        if(conn != null) {
            conn.close();
        }

        return reslist;
    }


    public static void main(String[] args) throws SQLException, InterruptedException, JsonProcessingException {

        Properties prop = PropertiesUtil.getPropertiesUtilInstance("kafkaProducerConfig.properties").getProp();
        Producer<String, String> producer = new KafkaProducer<String, String>(prop);

        KafkaProducerMessageShuffle kafkaProducerMessageShuffle = new KafkaProducerMessageShuffle();

        int count = kafkaProducerMessageShuffle.getCountMessageShuffle();

        int start = 0;
        int lenth = 1000;

        ObjectMapper mapper = new ObjectMapper();

        System.out.println("报文数据共：" + count);

        while (start < count) {

            System.out.println("发送一批数据：");

            List<MessageBean> sendDatas = kafkaProducerMessageShuffle.getMessageShuffle(start, lenth);

            for (MessageBean mb: sendDatas) {
                System.out.println(mb.toString());
                producer.send(new ProducerRecord<>(topic, mapper.writeValueAsString(mb)));
            }

            start += lenth;
            Thread.sleep(sleepTime);
        }

        producer.close();

    }


}
