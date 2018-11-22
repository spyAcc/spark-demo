package kafka;


import com.fasterxml.jackson.core.JsonProcessingException;
import data.MessageGen;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

/**
 * 查询模拟报文数据，并送入kafka
 */
public class KafkaProducerApp03 {

    private static final String topic = "message-shuffle";

    public static void main(String[] args) throws SQLException, JsonProcessingException {


        Properties prop = new Properties();
        //多个broker，用逗号分隔
        prop.put("bootstrap.servers", "localhost:9092");
        prop.put("batch.size", 20);
        prop.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        prop.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<String, String>(prop);

        final List<String> datas = new MessageGen().getMessageJsonFromDB();


        for(int i = 0; i < 100; i++) {
            producer.send(new ProducerRecord<>(topic, Integer.toString(i), datas.get(i) ));
        }

        producer.close();


    }

}
