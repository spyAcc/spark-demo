package com.spy.spark;

import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import scala.Tuple2;

import java.util.Arrays;

/**
 * word count 程序
 *
 */
public class SparkRddApp01 {

    public static String path = "hdfs://127.0.0.1:8020/words.txt";

    public static void main( String[] args ) {


        SparkSession spark = SparkSession.builder().appName("word-count")
                .master("local[2]").getOrCreate();

//        SparkContext sc =  spark.sparkContext();
//        JavaSparkContext jsc = JavaSparkContext.fromSparkContext(sc);

        JavaRDD<String> lines = spark.read().textFile(path).javaRDD();
        JavaRDD<String> words = lines.flatMap(line -> Arrays.asList(line.split(" ")).iterator());

        JavaRDD<String> cleanWords = words.map(w -> {
           w = w.trim();
           if(w.matches("[.\",?-]")) {
               return "";
           } else {
               return w;
           }
        });

        JavaPairRDD<String, Integer> wordPair = cleanWords.mapToPair(cleanWord -> new Tuple2<>(cleanWord, 1));

        JavaPairRDD<String, Integer> result = wordPair.reduceByKey((_a, _b)->_a+_b);

        result.foreach(each ->{
            System.out.println(each._1 + ": " + each._2);
        });

        spark.stop();


    }
}
