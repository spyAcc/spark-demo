package com.spy.spark;

import com.spy.spark.util.KafkaParams;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.Optional;
import org.apache.spark.streaming.Durations;
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

/**
 * spark-streaming kafka, updatestatebykey的应用，状态流wordcount
 */
public class SparkStreamingApp02 {

    private static final Logger logger = LoggerFactory.getLogger(SparkStreamingApp02.class);

    private static final String checkpointdir = "file:///D:/hadoop/hadoop-2.6.5/checkpoint";

    public static void main(String[] args) {

        SparkConf sc = new SparkConf();
        sc.setAppName("SparkStreamingApp01");
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


        JavaPairDStream<String, Integer> words = javaDS
                .map(record -> record.value())
                .flatMap(line -> Arrays.asList(line.split(" ")).iterator())
                .mapToPair(word -> new Tuple2<String, Integer>(word, 1));

        /**
         *
         * valuelist: 新的值
         * oldstate： 旧的值
         *
         */
        JavaPairDStream<String, Integer> wordcount = words.updateStateByKey((valuelist, oldState) -> {

            Integer newState = 0;

            if(oldState.isPresent()) {
                newState = oldState.get();
            }

            for(Integer v:valuelist) {
                newState += v;
            }

            return Optional.of(newState);
        });


        wordcount.foreachRDD(rdd -> {

            rdd.foreach(ty -> {
                logger.info(ty._1 + ": " + ty._2);
            });

        });



        jsc.start();

        try {
            jsc.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
            jsc.close();
        }

    }


}
