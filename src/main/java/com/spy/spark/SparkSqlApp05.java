package com.spy.spark;

import dbutil.DbUtil;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * spark-sql mysql 数据表操作
 */
public class SparkSqlApp05 {


    private final static String url = "jdbc:mysql://localhost:3306/spark";
    private final static String user = "root";
    private final static String password = "123456";
    private final static String driver = "com.mysql.jdbc.Driver";

    private static final String out2 = "file:///D:/hadoop/data/mysqllog.parquet";

    public static void main(String[] args) throws SQLException {

        SparkSession spark = SparkSession.builder()
                .appName(SparkSqlApp05.class.getName())
                .master("local[2]")
                .getOrCreate();


        Dataset<Row> jdbcDf = spark.read().format("jdbc")
                .option("url", url)
                .option("dbtable", "spark.log")
                .option("user", user)
                .option("password", password)
                .option("driver", driver)
                .load();

        jdbcDf.printSchema();
        jdbcDf.show();

        Dataset<Row> cacheDf = jdbcDf.cache();
        cacheDf.createOrReplaceTempView("logs");

        spark.sql("select * from logs where type = 'system' and level = 'error' limit 7").show();

        //结果写入文件
        cacheDf.coalesce(1).write().mode(SaveMode.Overwrite).save(out2);

        //统计各个level的数量，写入数据库的结果表
        Dataset<Row> res = spark.sql("select level, count(level) as cnt from logs group by level order by cnt desc");
        //这个坑很大，jdbc的格式，只能读，不能写，写要自己写
        //        res.write().format("jdbc")
//                .option("url", url)
//                .option("dbtable", "spark.res")
//                .option("user", user)
//                .option("password", password)
//                .option("driver", driver)
//                .save();


        //对每个分区进行写操作
        res.foreachPartition(rds ->{

            Connection conn = DbUtil.getConnection();
            String sql = "insert into res (level, num) values (?,?)";
            PreparedStatement pst = conn.prepareStatement(sql);

            conn.setAutoCommit(false);

            while (rds.hasNext()) {
                Row row = rds.next();

                pst.setString(1, row.getString(row.fieldIndex("level")));
                pst.setInt(2, ((Long)row.getLong(row.fieldIndex("cnt"))).intValue());

                pst.addBatch();

            }

            pst.executeBatch();
            conn.commit();

            conn.close();

        });


        spark.catalog().uncacheTable("logs");

        spark.stop();

    }

}
