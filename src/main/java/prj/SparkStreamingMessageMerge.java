package prj;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spy.spark.util.KafkaParams;
import data.MessageBean;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.Function3;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.State;
import org.apache.spark.streaming.StateSpec;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SparkStreamingMessageMerge {

    private static final String checkpointdir = "file:///D:/hadoop/hadoop-2.6.5/checkpoint";

    private static final Logger logger = LoggerFactory.getLogger(SparkStreamingMessageMerge.class);

    public static void main(String[] args) {

        SparkConf sc = new SparkConf();
        sc.setAppName("SparkStreamingMessageMerge");
        sc.setMaster("local[2]");
        sc.set("spark.local.dir","D:/hadoop/hadoop-2.6.5/sparklocal");

        JavaStreamingContext jsc = new JavaStreamingContext(sc, Durations.seconds(20));

        jsc.checkpoint(checkpointdir);

        KafkaParams kafkaParams = new KafkaParams();

        /**
         * json报文流输入
         */
        JavaInputDStream<ConsumerRecord<String, String>> javaDS
                = KafkaUtils.createDirectStream(
                jsc,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.<String, String>Subscribe(kafkaParams.topics, kafkaParams.params)
        );


        /**
         * 解析json为bean， 组成tuple2的pair
         */
        JavaPairDStream<String, MessageBean> pairStream = javaDS.mapToPair(record -> {
            ObjectMapper obm = new ObjectMapper();
            MessageBean mb = obm.readValue(record.value(), MessageBean.class);
            return new Tuple2<>(mb.getKey(), mb);
        });

        /**
         * 根据key进行分组
         */
        JavaPairDStream<String, List<MessageBean>> groupStream = pairStream.groupByKey()
                .mapToPair(tuple -> {

                    Iterable<MessageBean> it = tuple._2;

                    List<MessageBean> listblock = new ArrayList<>();

                    for(MessageBean mb : it) {
                        listblock.add(mb);
                    }


                    return new Tuple2<>(tuple._1, listblock);
                });


        /**
         *
         * 将group做状态流
         *
         */


        /**
         *
         * 1： key的类型
         * 2： value的类型
         * 3： value的状态，可以更新的值，可以传到下次批次
         * 4： 返回值
         *
         */
        Function3<String, Optional<List<MessageBean>>, State<List<MessageBean>>, Tuple2<String, String>> mapFuc =
                /**
                 * k:key值
                 * newMsg： 新的value值
                 * oldMsg： 上次传来的value值
                 */
                (k, newMsg, oldMsg) -> {

                    List<MessageBean> newCom = newMsg.orNull();

                    StringBuilder sb = new StringBuilder();

                    if(newCom != null) {
                        List<MessageBean> oldCom = oldMsg.exists()? oldMsg.get(): new ArrayList<MessageBean>();

                        oldCom.addAll(newCom);

                        Collections.sort(oldCom);


                        //判断完整性
                        int s = oldCom.size();
                        if(s == oldCom.get(0).getLen()) {

                            //拼接报文
                            for(MessageBean mb: oldCom) {

                                sb.append(mb.getValue());

                            }
                            logger.info("----------------------------------------------------");
                            logger.info("----------------------------------------------------");
                            logger.info("----------------------------------------------------");
                            logger.info(sb.toString());
                            logger.info("----------------------------------------------------");
                            logger.info("----------------------------------------------------");
                            logger.info("----------------------------------------------------");

                            oldMsg.remove();

                        } else {
                            oldMsg.update(oldCom);
                        }


                    }

                    Tuple2<String, String> out = new Tuple2<>(k, sb.toString());
                    return out;

                };


        JavaMapWithStateDStream<String, List<MessageBean>, List<MessageBean>, Tuple2<String, String>> stateStream
                = groupStream.mapWithState(StateSpec.function(mapFuc));


        /**
         * 打印结果
         */
        stateStream.print();



        jsc.start();

        try {
            jsc.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
            jsc.close();
        }



    }


}
