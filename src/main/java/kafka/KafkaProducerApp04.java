package kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class KafkaProducerApp04 {

    private static final String topic = "message-shuffle";

    private static String [] alpha = new String[]{"a", "b", "c", "d", "e", "f", "g",
            "h", "i", "j", "k", "l", "m" , "n", "o", "p", "q", "r", "s", "t", "u",
            "v", "w", "x", "y", "z", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0"
    };

    public static List<String> getWordStr(int lineNum) {
        List<String> res = new ArrayList<>();

        Random rd = new Random();

        for(int i = 0; i < lineNum; i++) {
            StringBuilder sb = new StringBuilder();

            int c = rd.nextInt(20) + 1;
            for(int j = 0; j < c; j++) {
                sb.append(alpha[rd.nextInt(36)]);
                sb.append(" ");
            }
            res.add(sb.toString());

        }

        return res;
    }

    public static void main(String[] args) throws InterruptedException {

        Properties prop = new Properties();
        //多个broker，用逗号分隔
        prop.put("bootstrap.servers", "localhost:9092");
        prop.put("batch.size", 20);
        prop.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        prop.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<String, String>(prop);

        Random rd = new Random();

        int whileNum = 0;
        while(whileNum++ < 100) {

            List<String> datas = getWordStr(rd.nextInt(100));

            for(int i = 0; i < datas.size(); i++) {
                producer.send(new ProducerRecord<>(topic, datas.get(i) ));
            }

            Thread.sleep(10000);

        }

        producer.close();

    }

}
