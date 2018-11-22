package com.spy.spark;

import com.fasterxml.jackson.databind.ObjectMapper;
import data.MessageBean;
import data.MessageResultBean;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * kafka streaming整合，json解析，groupbykey分组，最后拼接字符串
 */
public class SparkStreamingApp01 {

    private static final Logger logger = LoggerFactory.getLogger(SparkStreamingApp01.class);

    private static final String topic = "message-shuffle";

    public static void main(String[] args) throws InterruptedException {

        SparkConf sc = new SparkConf();
        sc.setAppName("SparkStreamingApp01");
        sc.setMaster("local[2]");
        sc.set("spark.local.dir","D:/hadoop/hadoop-2.6.5/sparklocal");

        JavaStreamingContext jc = new JavaStreamingContext(sc, Durations.seconds(20));

        Map<String, Object> kafkaParams = new HashMap<String, Object>();
        kafkaParams.put("bootstrap.servers", "localhost:9092");
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put("group.id", "streaming-message");
        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("enable.auto.commit", false);


        Collection<String> topics = Arrays.asList(topic);

        JavaInputDStream<ConsumerRecord<String, String>> stream
                = KafkaUtils.createDirectStream(
                jc,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.<String, String>Subscribe(topics, kafkaParams)
        );


        JavaPairDStream<String, MessageBean> pairStream = stream.mapToPair(record -> {
            ObjectMapper obm = new ObjectMapper();
            MessageBean mb = obm.readValue(record.value(), MessageBean.class);
            return new Tuple2<>(mb.getKey(), mb);
        });


        JavaPairDStream<String, Iterable<MessageBean>> groupStream = pairStream.groupByKey();

        JavaDStream<MessageResultBean> jDstream = groupStream.map(tuple -> {

            Iterable<MessageBean> it = tuple._2;

            //字符串拼接
            StringBuffer sb = new StringBuffer();

            for (MessageBean mb: it) {
                sb.append(mb.getValue());
            }

            MessageResultBean mrb = new MessageResultBean();
            mrb.setKey(tuple._1);
            mrb.setValue(sb.toString());

            return mrb;
        });

        jDstream.foreachRDD(rdd -> {

            rdd.foreach(v -> {
                logger.info(v.getKey() + "---" + v.getValue());

            });
        });


        jc.start();
        jc.awaitTermination();

    }




}
