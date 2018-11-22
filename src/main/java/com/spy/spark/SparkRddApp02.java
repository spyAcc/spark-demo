package com.spy.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;

/**
 * spark 聚合 groupbykey
 */
public class SparkRddApp02 {

    public static void main(String[] args) {
         SparkConf conf = new SparkConf();
         conf.setMaster("local[2]");
         conf.setAppName("union");
         JavaSparkContext sc = new JavaSparkContext(conf);
         List<Tuple2<String, Integer>> list = Arrays.asList(
                 new Tuple2<String, Integer>("cl1", 90),
                 new Tuple2<String, Integer>("cl2", 91),
                 new Tuple2<String, Integer>("cl3", 97),
                 new Tuple2<String, Integer>("cl1", 96),
                 new Tuple2<String, Integer>("cl1", 89),
                 new Tuple2<String, Integer>("cl3", 90),
                 new Tuple2<String, Integer>("cl2", 60));
        JavaPairRDD<String, Integer> listRDD = sc.parallelizePairs(list);
        JavaPairRDD<String, Iterable<Integer>> results  = listRDD.groupByKey();

        System.out.println(results.collect());
        sc.close();
    }

}
