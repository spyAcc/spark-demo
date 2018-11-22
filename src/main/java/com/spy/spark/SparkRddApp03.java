package com.spy.spark;

import com.fasterxml.jackson.core.JsonProcessingException;
import data.MessageBean;
import data.MessageGen;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.sql.SQLException;
import java.util.List;

/**
 * spark 排序 sortbykey, sortby
 */
public class SparkRddApp03 {

    private static final Logger logger = LoggerFactory.getLogger(SparkRddApp03.class);

    public static void main(String[] args) throws SQLException, JsonProcessingException {

        MessageGen mg = new MessageGen();
        List<MessageBean> msgs = mg.genMsgs(100);

        SparkSession spark = SparkSession.builder()
                .appName(SparkRddApp03.class.getName())
                .master("local[2]")
                .getOrCreate();


        JavaSparkContext jsc = JavaSparkContext.fromSparkContext(spark.sparkContext());

        JavaRDD<MessageBean> jrdd = jsc.parallelize(msgs);


        JavaRDD<MessageBean> sortRDD = jrdd.sortBy(mb -> mb, true, 1);

        JavaPairRDD<String, Iterable<MessageBean>> groupRDD = sortRDD.mapToPair(messageBean -> {
           return new Tuple2<String, MessageBean>(messageBean.getKey(), messageBean);
        }).groupByKey();



        groupRDD.foreach(ty -> {

            Iterable<MessageBean> it = ty._2;
            StringBuffer sb = new StringBuffer();
            for (MessageBean m: it) {
                sb.append(m.getValue());
            }

            logger.info(ty._1 + "---" + sb.toString());
        });






    }

}
