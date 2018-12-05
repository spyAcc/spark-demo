package prj;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spy.spark.util.KafkaParams;
import data.MessageBean;
import data.MessageResultBean;
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

import java.util.Arrays;

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

        JavaInputDStream<ConsumerRecord<String, String>> javaDS
                = KafkaUtils.createDirectStream(
                jsc,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.<String, String>Subscribe(kafkaParams.topics, kafkaParams.params)
        );


        JavaPairDStream<String, MessageBean> pairStream = javaDS.mapToPair(record -> {
            ObjectMapper obm = new ObjectMapper();
            MessageBean mb = obm.readValue(record.value(), MessageBean.class);
            return new Tuple2<>(mb.getKey(), mb);
        });

        JavaPairDStream<String, Iterable<MessageBean>> groupStream = pairStream.groupByKey();


        JavaPairDStream<String, MessageResultBean> jDstream = groupStream.mapToPair(tuple -> {

            Iterable<MessageBean> it = tuple._2;

            //字符串拼接
            StringBuffer sb = new StringBuffer();

            for (MessageBean mb: it) {
                sb.append(mb.getValue());
            }

            MessageResultBean mrb = new MessageResultBean();
            mrb.setKey(tuple._1);
            mrb.setValue(sb.toString());

            return new Tuple2<>(mrb.getKey(), mrb);
        });



        /**
         *
         * 1： key的类型
         * 2： value的类型
         * 3： value的状态，可以更新的值，可以传到下次批次
         * 4： 返回值
         *
         */
        Function3<String, Optional<Integer>, State<Integer>, Tuple2<String, Integer>> mapFuc =
                /**
                 * w:key值
                 * one： 新的value值
                 * state： 上次传来的value值
                 */
                (w, one, state) -> {

                    int sum = one.orElse(0) + (state.exists() ? state.get(): 0);
                    Tuple2<String, Integer> out = new Tuple2<>(w, sum);
                    state.update(sum);
                    return out;
                };


        JavaMapWithStateDStream<String, Integer, Integer, Tuple2<String, Integer>> wordcount
                = jDstream.mapWithState(StateSpec.function(mapFuc));



        jsc.start();

        try {
            jsc.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
            jsc.close();
        }



    }


}
