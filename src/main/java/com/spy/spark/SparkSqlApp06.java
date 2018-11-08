package com.spy.spark;

import net.iharder.Base64;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.protobuf.ProtobufUtil;
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * spark-sql hbase数据库, 读取数据为 df
 */
public class SparkSqlApp06 {

    private static final String path = "hdfs://127.0.0.1:8020/sparktest/baidulogs.json";

    public static void main(String[] args) throws IOException {

        SparkSession spark = SparkSession.builder()
                .appName(SparkSqlApp06.class.getName())
                .master("local[2]")
                .getOrCreate();

        Configuration conf = HBaseConfiguration.create();

        conf.set("hbase.rootdir", "file:///D:/hadoop/hadoop-2.6.5/hbase/root");

        Scan scan = new Scan();
        conf.set(TableInputFormat.INPUT_TABLE, "test");

        ClientProtos.Scan proto = ProtobufUtil.toScan(scan);
        conf.set(TableInputFormat.SCAN, Base64.encodeBytes(proto.toByteArray()));

        JavaSparkContext jsc = JavaSparkContext.fromSparkContext(spark.sparkContext());

        JavaPairRDD<ImmutableBytesWritable, Result> javaPairRDD = jsc.newAPIHadoopRDD(conf, TableInputFormat.class, ImmutableBytesWritable.class, Result.class);


        JavaRDD<Row> testRDD = javaPairRDD.map(tuple -> {

            Result result = tuple._2();

            String rowkey = Bytes.toString(result.getRow());

            //获取列值
            String c1 = Bytes.toString(result.getValue(Bytes.toBytes("cf"), Bytes.toBytes("c1")));

            return RowFactory.create(rowkey, c1);
        });


        //构建schema
        List<StructField> stf = new ArrayList<>();
        stf.add(DataTypes.createStructField("rowkey", DataTypes.StringType, true));
        stf.add(DataTypes.createStructField("c1", DataTypes.StringType, true));

        StructType schema = DataTypes.createStructType(stf);

        Dataset<Row> testDf = spark.createDataFrame(testRDD, schema);

        testDf.printSchema();
        testDf.show();


        spark.stop();

    }

}
