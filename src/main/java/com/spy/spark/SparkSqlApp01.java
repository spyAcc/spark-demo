package com.spy.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import static org.apache.spark.sql.functions.col;

/**
 * spark-sql 读取json数据，基本处理函数， dataframe/dataset api
 */
public class SparkSqlApp01 {

    public static String path = "hdfs://127.0.0.1:8020/sparktest/baidulogs.json";

    public static void main(String[] args) {

        SparkSession spark = SparkSession.builder()
                .appName(SparkSqlApp01.class.getName())
                .master("local[2]")
                .getOrCreate();

        Dataset<Row> df = spark.read().json(path);

        //先缓存一下
        df.cache();

        //打印表结构
        df.printSchema();
        //show，默认20条记录
        df.show();
        df.show(30, false);

        //选择某列数据
        df.select("id", "type", "times").show();

        //使用函数
        df.select(col("id").as("id-2"), col("level")).show();


        //过滤数据
        df.filter("id > 100 and level = 'debug'").show();

        df.filter(col("id").gt(100)).filter(col("level").like("waring")).show();

        //分组
        df.groupBy("level").count().show();

        //排序
        df.sort(col("id").desc()).show();

        //遍历
        df.foreach(b -> {
            spark.log().info(b.getString(b.fieldIndex("ip")) + ":" + b.getString(b.fieldIndex("url")));
        });

        //连接
        df.join(df, "id").filter("id < 10").show();

        spark.stop();
    }

}
