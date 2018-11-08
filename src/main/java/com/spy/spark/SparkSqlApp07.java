package com.spy.spark;

import data.TestBean;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * spark-sql  hbase， 写数据
 */
public class SparkSqlApp07 {

    private static String[] alpha = new String[]{"a", "b", "c", "d", "e", "f", "g",
            "h", "i", "j", "k", "l", "m" , "n", "o", "p", "q", "r", "s", "t", "u",
            "v", "w", "x", "y", "z", "1", "2", "3", "4","5","6","7","8","9","0"
    };

    public static String rdstr(int n) {
        StringBuilder sb = new StringBuilder();
        Random rd = new Random();
        for(int i = 0; i < n; i++) {
            sb.append(alpha[rd.nextInt(36)]);
        }
        return sb.toString();
    }

    public static void main(String[] args) {

        SparkSession spark = SparkSession.builder()
                .appName(SparkSqlApp07.class.getName())
                .master("local[2]")
                .getOrCreate();

        Random rd = new Random();

        //生成数据
        List<TestBean> tbs = new ArrayList<TestBean>();
        for(int i = 0; i < 10; i++) {
            TestBean tb = new TestBean();
            tb.setRowkey("row-" + rd.nextInt());
            tb.setCfc1(rdstr(rd.nextInt(20)));
            tbs.add(tb);
        }

        //list to rdd
        JavaSparkContext jsc = JavaSparkContext.fromSparkContext(spark.sparkContext());
        JavaRDD<TestBean> tbRDD = jsc.parallelize(tbs);

        //分区写入
        tbRDD.foreachPartition(item -> {

            Configuration conf = HBaseConfiguration.create();
            conf.set("hbase.rootdir", "file:///D:/hadoop/hadoop-2.6.5/hbase/root");




        });





        spark.stop();
    }
}
