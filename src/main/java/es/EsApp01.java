package es;


import data.TradeBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 *
 * es 索引建立，使用es java api
 *
 */
public class EsApp01 {

    private String [] types = new String[]{
            "A", "B", "C", "D"
    };

    public List<TradeBean> getTradeBeanList(int n) {

        ArrayList<TradeBean> tds = new ArrayList<>();

        Random rd = new Random();

        for(int i = 0; i < n; i++) {
            TradeBean tb = new TradeBean();
            tb.setTradeId(UUID.randomUUID().toString());
            tb.setTradeType(types[rd.nextInt(4)]);
            tb.setTimestamp(System.currentTimeMillis());
            tds.add(tb);
        }

        return tds;
    }


    private static String index = "rtaml";

    private static String type = "trade";

    public static void main(String[] args) {

//        testSearch();
        System.out.println("start");
        testSearch();
        System.out.println("end");



    }


    public static void testSearch() {
        EsApp01 esApp01 = new EsApp01();

        TradeDataUtil.searchIndex(index, type, "B");


    }


    public static void testCreateBluk() {

        EsApp01 esApp01 = new EsApp01();

        List<TradeBean> list = esApp01.getTradeBeanList(50);

        try {
            TradeDataUtil.createIndex(index, type, list);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    public static void testCreate() {
        EsApp01 esApp01 = new EsApp01();


        List<TradeBean> list = esApp01.getTradeBeanList(20);


        try {
            for(TradeBean tb: list) {

                TradeDataUtil.createIndex(index, type, tb);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
