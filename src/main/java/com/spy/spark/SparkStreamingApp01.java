package com.spy.spark;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.streaming.Duration;
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
 * kafka streaming整合，处理数据，并写入hbase
 */
public class SparkStreamingApp01 {

    private static final Logger logger = LoggerFactory.getLogger(SparkStreamingApp01.class);

    public static void main(String[] args) throws InterruptedException {

        JavaStreamingContext jc = new JavaStreamingContext("local[2]", "SparkStreamingApp01", new Duration(5000));

        Map<String, Object> kafkaParams = new HashMap<String, Object>();
        kafkaParams.put("bootstrap.servers", "localhost:9092");
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put("group.id", "streaming-test");
        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("enable.auto.commit", false);


        Collection<String> topics = Arrays.asList("test");

        JavaInputDStream<ConsumerRecord<String, String>> stream
                = KafkaUtils.createDirectStream(
                jc,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.<String, String>Subscribe(topics, kafkaParams)
        );


        JavaPairDStream<String, String> pairStream = stream.mapToPair(record -> new Tuple2<>(record.key(), record.value()));

        JavaDStream<String> jDstream =  pairStream.map( pair -> {
            return pair._2;
        });


        jDstream.foreachRDD(rdd -> {

            rdd.foreach(v -> {
                logger.info(v);
                //这里可以保存到hbase


            });
        });


        jc.start();
        jc.awaitTermination();

    }




}
