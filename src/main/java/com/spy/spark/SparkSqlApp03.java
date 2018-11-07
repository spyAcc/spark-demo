package com.spy.spark;

import data.LogBean;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.List;

/**
 * spark-sql 读取text，rdd转换为dataframe
 */
public class SparkSqlApp03 {

    private static final String path = "hdfs://127.0.0.1:8020/sparktest/baidulogs.txt";

    public static void main(String[] args) {

//        function1();
        function2();

    }

    public static void function1() {
        SparkSession spark = SparkSession.builder()
                .appName(SparkSqlApp03.class.getName())
                .master("local[2]")
                .getOrCreate();


        JavaRDD<LogBean> logRDD = spark.read().textFile(path)
                .javaRDD()
                .map(line -> {
                    String [] parts = line.split("\t");
                    LogBean lgb = new LogBean();
                    lgb.setId(Integer.parseInt(parts[0]));
                    lgb.setType(parts[1]);
                    lgb.setLevel(parts[2]);
                    lgb.setIp(parts[3]);
                    lgb.setUrl(parts[4]);
                    lgb.setType(parts[5]);

                    return lgb;
                });


        Dataset<Row> logDf = spark.createDataFrame(logRDD, LogBean.class);

        logDf.createOrReplaceTempView("logs");

        spark.sql("select * from logs").show();

        spark.stop();
    }


    public static void function2() {


        SparkSession spark = SparkSession.builder()
                .appName(SparkSqlApp03.class.getName())
                .master("local[2]")
                .getOrCreate();


        JavaRDD<String> logRDD = spark.read().textFile(path).toJavaRDD();

        //构建row
        JavaRDD<Row> logRowRDD = logRDD.map(line -> {
            String [] splits = line.split("\t");
            return RowFactory.create(Integer.parseInt(splits[0]), splits[1], splits[2], splits[3], splits[4], splits[5]);
        });

        //构建schema
        List<StructField> stf = new ArrayList<>();
        stf.add(DataTypes.createStructField("id", DataTypes.IntegerType, true));
        stf.add(DataTypes.createStructField("type", DataTypes.StringType, true));
        stf.add(DataTypes.createStructField("level", DataTypes.StringType, true));
        stf.add(DataTypes.createStructField("ip", DataTypes.StringType, true));
        stf.add(DataTypes.createStructField("url", DataTypes.StringType, true));
        stf.add(DataTypes.createStructField("times", DataTypes.StringType, true));

        StructType schema = DataTypes.createStructType(stf);

        Dataset<Row> logDf = spark.createDataFrame(logRowRDD, schema);

        logDf.createOrReplaceTempView("logs");

        spark.sql("select * from logs").show();

        spark.stop();

    }


}
