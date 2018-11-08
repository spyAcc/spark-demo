package data;

import java.io.Serializable;


/**
 *
 * hbase 的字段
 *
 * row-key
 * cf:c1
 *
 */
public class TestBean implements Serializable {

    private String rowkey;

    private String cfc1;

    public String getRowkey() {
        return rowkey;
    }

    public void setRowkey(String rowkey) {
        this.rowkey = rowkey;
    }

    public String getCfc1() {
        return cfc1;
    }

    public void setCfc1(String cfc1) {
        this.cfc1 = cfc1;
    }
}
