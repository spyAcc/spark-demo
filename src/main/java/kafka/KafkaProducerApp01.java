package kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * kafka 生产者，生产消息
 */
public class KafkaProducerApp01 {

    public static void main(String[] args) {

        Properties prop = new Properties();
        //多个broker，用逗号分隔
        prop.put("bootstrap.servers", "localhost:9092");
        prop.put("batch.size", 5);
        prop.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        prop.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<String, String>(prop);
        for(int i = 101; i < 200; i++) {
            producer.send(new ProducerRecord<>("message-shuffle", Integer.toString(i), Integer.toString(i) ));
            System.out.println(i);
        }

        producer.close();

    }




}
