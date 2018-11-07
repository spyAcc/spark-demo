package com.spy.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

/**
 * spark-sql 读取json数据，sql方法处理
 */
public class SparkSqlApp02 {

    public static final String path = "hdfs://127.0.0.1:8020/sparktest/baidulogs.json";

    public static void main(String[] args) {

        SparkSession spark = SparkSession.builder()
                .appName(SparkSqlApp02.class.getName())
                .master("local[2]")
                .getOrCreate();

        Dataset<Row> df = spark.read().format("json").load(path);

        df.printSchema();

        //建立一个视图
        df.createOrReplaceTempView("baidulog");

        //使用sql语句
        spark.sql("select * from baidulog where id < 10").show();

        //统计一下各种级别日志的个数，并排序
        spark.sql("select level, count(level) as cnt from baidulog group by level order by cnt desc").show();


        spark.stop();
    }

}
