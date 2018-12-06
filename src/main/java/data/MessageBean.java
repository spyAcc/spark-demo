package data;

import java.io.Serializable;

/**
 * 模拟报文
 */
public class MessageBean implements Serializable, Comparable<MessageBean> {

    private String key;

    private String value;

    private int sort;

    private int len;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }


    @Override
    public int compareTo(MessageBean o) {
        if(this.getKey().equals(o.getKey())) {
            return this.sortBysort(o);
        } else {
            return this.getKey().compareTo(o.getKey());
        }
    }

    private int sortBysort(MessageBean mb) {
        return this.getSort() - mb.getSort();
    }

    @Override
    public String toString() {
        return "MessageBean{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", sort=" + sort +
                ", len=" + len +
                '}';
    }
}
