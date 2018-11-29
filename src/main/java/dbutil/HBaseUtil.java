package dbutil;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * java hbase api 封装
 * 获取连接，获取表，对表进行增，删，改，查
 *
 * hbase数据结构
 * Map<tableName:String, Map<rowKey:String, Map<cf:String, Map<c:String, v:String>>>>
 *
 *     (tableName, (rowKey, (cf, (c, v))))
 *
 * hbase的所有数据操作都可以抽象为对以上数据结构的操作
 *
 *
 *
 * hbase 查询条件结构
 *
 *
 *
 */
public class HBaseUtil {

    private static final Logger logger = LoggerFactory.getLogger(HBaseUtil.class);

    //hbase 配置设置
    private Configuration configuration;

    //hbase连接
    private Connection connection;

    //scanner缓存的行
    private final int cachingSize = 1000;

    //scanner一次获取的列数
    private final int batchSize = 20;

    public static HBaseUtil hBaseUtil;

    public static synchronized HBaseUtil getHbaseUtilInstance() {
        if(hBaseUtil == null) {
            hBaseUtil = new HBaseUtil();
        }
        return hBaseUtil;
    }

    private HBaseUtil() {
        //需要hbase-site.xml文件在resources下面
        configuration = HBaseConfiguration.create();
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        try {
            if(connection == null || connection.isClosed()) {
                connection = ConnectionFactory.createConnection(configuration, executorService);
            }
        } catch (IOException e) {
            logger.error("hbase get connection error: ", e);
        }

    }


    public void close() {
        try {
            if(connection != null) {
                connection.close();
            }
        } catch (IOException e) {
            logger.error("hbase close connection error: ", e);
        }
    }


    /**
     * 根据rowkey获取一行数据
     * @param tableName
     * @param rowKey
     * @return (cf, (c,v))
     */
    public Map<String, Map<String, String>> getRow(String tableName, String rowKey) {
        return this.getRows(tableName, Arrays.asList(rowKey)).get(rowKey);
    }


    /**
     * 获取多行数据，根据多个rowkey
     * @param tableName
     * @param rowKeys
     * @return (rowkey, (cf, (c,v)))
     */
    public Map<String, Map<String, Map<String, String>>> getRows(String tableName, List<String> rowKeys) {

        Map<String, Map<String, Map<String, String>>> rowsMap = new HashMap<>();

        List<Get> gets = new ArrayList<>();

        for(String rowKey: rowKeys) {
            Get get = new Get(Bytes.toBytes(rowKey));
            gets.add(get);
        }

        try(Table table = connection.getTable(TableName.valueOf(tableName))) {

            Result [] results = table.get(gets);

            for(Result result: results) {

                Map<String, Map<String, String>> resultMap = new HashMap<String, Map<String, String>>();

                //cell为row里面的一列的记录，如果cf中有多个列，那么有多个cell
                for(Cell cell: result.rawCells()) {

                    String familyName = new String(cell.getFamilyArray(), cell.getFamilyOffset(), cell.getFamilyLength());


                    if (resultMap.get(familyName) == null) {

                        Map<String, String> cvMap = new HashMap<>();

                        cvMap.put(
                                new String(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength()),
                                new String(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength())
                        );

                        resultMap.put(familyName, cvMap);

                    } else {

                        resultMap.get(familyName)
                                .put(
                                        new String(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength()),
                                        new String(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength())
                                );

                    }

                }

                rowsMap.put(new String(result.getRow()), resultMap);
            }


        } catch (IOException e) {
            logger.error("get table " + tableName + " error: ", e);
        }

        return rowsMap;

    }


    /**
     * 获取整个表的所有数据
     * @param tableName
     * @return  (rowkey, (cf, (c,v)))
     */
    public Map<String, Map<String, Map<String, String>>> get(String tableName) {

        Map<String, Map<String, Map<String, String>>> rMap = new HashMap<>();

        try (Table table = connection.getTable(TableName.valueOf(tableName))) {

            Scan scan = new Scan();
            scan.setCaching(this.cachingSize);
            scan.setBatch(this.batchSize);
            ResultScanner resultScanner = table.getScanner(scan);

            for(Result result: resultScanner) {

                Map<String, Map<String, String>> resultMap = new HashMap<String, Map<String, String>>();

                //cell为row里面的一列的记录，如果cf中有多个列，那么有多个cell
                for(Cell cell: result.rawCells()) {

                    String familyName = new String(cell.getFamilyArray(), cell.getFamilyOffset(), cell.getFamilyLength());


                    if (resultMap.get(familyName) == null) {

                        Map<String, String> cvMap = new HashMap<>();

                        cvMap.put(
                                new String(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength()),
                                new String(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength())
                        );

                        resultMap.put(familyName, cvMap);

                    } else {

                        resultMap.get(familyName)
                                .put(
                                        new String(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength()),
                                        new String(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength())
                                );

                    }

                }

                rMap.put(new String(result.getRow()), resultMap);

            }


        } catch (IOException e) {
            logger.error("get table " + tableName + " error: ", e);
        }

        return rMap;
    }






    /**
     * 删除指定行
     * @param tableName
     * @param rowKey
     */
    public void delete(String tableName, String rowKey) {
        List<String> rowKeys = new ArrayList<>();
        rowKeys.add(rowKey);
        this.delete(tableName, rowKeys);
    }

    /**
     * 删除多行
     * @param tableName
     * @param rowKeys
     */
    public void delete(String tableName, List<String> rowKeys) {

        try (Table table = connection.getTable(TableName.valueOf(tableName))) {
            List<Delete> dellist = new ArrayList<>();

            for (String rowkey: rowKeys) {
                dellist.add(new Delete(Bytes.toBytes(rowkey)));
            }

            table.delete(dellist);
            logger.info("success delete rows");

        } catch (IOException e) {
            logger.error("get table " + tableName + " error: ", e);
        }

    }


    /**
     *
     * 新增, 修改 数据
     * @param tableName
     * @param rowKey
     * @param columnvalues   (cf, (c,v))
     */
    public int put(String tableName, String rowKey, Map<String, Map<String, String>> columnvalues) {
        Map<String, Map<String, Map<String, String>>> rowdatas = new HashMap<String, Map<String, Map<String, String>>>();
        rowdatas.put(rowKey, columnvalues);
        return this.put(tableName, rowdatas);
    }


    /**
     * 新增，修改 多行数据
     * @param tableName
     * @param rowdatas (rowkey, (cf, (c,v)))
     * @return
     */
    public int put(String tableName, Map<String, Map<String, Map<String, String>>> rowdatas) {

        List<Put> puts = new ArrayList<>();
        int putnumber = 0;

        try (Table table = connection.getTable(TableName.valueOf(tableName))) {

            for(String rowkey: rowdatas.keySet()) {

                Put put = new Put(Bytes.toBytes(rowkey));

                Map<String, Map<String, String>> columnvalues = rowdatas.get(rowkey);

                HColumnDescriptor [] columnFamilies = table.getTableDescriptor().getColumnFamilies();

                for (HColumnDescriptor hColumnDescriptor: columnFamilies) {

                    String familyName = hColumnDescriptor.getNameAsString();

                    Map<String, String> columnNameValue = columnvalues.get(familyName);

                    if(columnNameValue != null) {

                        for(String columnName : columnNameValue.keySet()) {

                            put.addColumn(Bytes.toBytes(familyName),
                                    Bytes.toBytes(columnName),
                                    Bytes.toBytes(columnNameValue.get(columnName)));

                        }

                    }

                }

                puts.add(put);
                putnumber += put.size();
            }

            table.put(puts);

        } catch (IOException e) {
            logger.error("get table " + tableName + " error: ", e);
        }

        return putnumber;
    }



    public String printMap3(Map<String, Map<String, Map<String, String>>> map) {

        StringBuilder sb = new StringBuilder();

        for(String key: map.keySet()) {
            sb.append("\t" + key + ":\r\n" + printMap2(map.get(key)) + "\r\n");
        }

        return sb.toString();
    }


    public String printMap2(Map<String, Map<String, String>> map) {

        StringBuilder sb = new StringBuilder();

        for(String key: map.keySet()) {
            sb.append("\t\t" + key + ":\r\n" + printMap(map.get(key)) + "\r\n");
        }

        return sb.toString();
    }

    public String printMap(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();

        for(String key: map.keySet()) {
            sb.append("\t\t\t" + key + ":" + map.get(key) + "\r\n");
        }

        return sb.toString();
    }





}


