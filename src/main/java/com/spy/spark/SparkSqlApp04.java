package com.spy.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

/**
 * spark-sql 读取,保存
 */
public class SparkSqlApp04 {


    private static final String path = "hdfs://127.0.0.1:8020/sparktest/baidulogs.json";

    private static final String out = "file:///D:/hadoop/data/blog.parquet";

    private static final String out2 = "file:///D:/hadoop/data/blog.json";

    public static void main(String[] args) {

        SparkSession spark = SparkSession.builder()
                .appName(SparkSqlApp04.class.getName())
                .master("local[2]")
                .getOrCreate();


        Dataset<Row> logDf = spark.read().json(path);

        //默认是 parquit 格式
        logDf.limit(10).coalesce(1).write().mode(SaveMode.Overwrite).save(out);

        //保存为json
        logDf.limit(20).write().mode(SaveMode.Overwrite).json(out2);

        //度保存的文件
        spark.read().json(out2).show();

        spark.stop();
    }


}
