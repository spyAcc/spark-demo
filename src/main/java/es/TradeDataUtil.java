package es;

import data.TradeBean;
import dbutil.EsUtil;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;

import java.io.IOException;
import java.util.List;

public class TradeDataUtil {


    public static boolean createIndex(String index, String type, List<TradeBean> datas) throws IOException {

        TransportClient client = EsUtil.getInstance().getEsClient();

        BulkRequestBuilder bkbuild = client.prepareBulk();


        for (TradeBean tb: datas) {

            bkbuild.add(client.prepareIndex(index, type).setSource(XContentFactory.jsonBuilder()
                    .startObject()
                    .field("tradeId", tb.getTradeId())
                    .field("tradeType", tb.getTradeType())
                    .field("tradeTime", tb.getTimestamp())
                    .endObject()));

        }


        BulkResponse response = bkbuild.execute().actionGet();
        if (response == null || response.hasFailures()) {
            return false;
        }

        return true;
    }

    public static boolean createIndex(String index, String type, TradeBean trade) throws IOException {

        TransportClient client = EsUtil.getInstance().getEsClient();

        IndexResponse ir = client.prepareIndex(index, type).setSource(
                XContentFactory.jsonBuilder()
                        .startObject()
                        .field("tradeId", trade.getTradeId())
                        .field("tradeType", trade.getTradeType())
                        .field("tradeTime", trade.getTimestamp())
                        .endObject()
        ).get();

        if (ir.status() == RestStatus.OK || ir.status() == RestStatus.CREATED) {
            return true;
        }

        return false;
    }


    public static boolean deleteIndex(String index, String type, String id) {

        TransportClient client = EsUtil.getInstance().getEsClient();

        DeleteResponse dr = client.prepareDelete(index, type, id).get();

        if(dr.status() == RestStatus.OK || dr.status() == RestStatus.ACCEPTED) {
            return true;
        }

        return false;

    }



    public static boolean updateIndex(String index, String type, String id, TradeBean trade) throws IOException {

        TransportClient client = EsUtil.getInstance().getEsClient();

        UpdateResponse ur = client.prepareUpdate(index, type, id).setDoc(
                XContentFactory.jsonBuilder()
                        .startObject()
                        .field("tradeId", trade.getTradeId())
                        .field("tradeType", trade.getTradeType())
                        .field("tradeTime", trade.getTimestamp())
                        .endObject()
        ).get();

        if(ur.status() == RestStatus.OK || ur.status() == RestStatus.ACCEPTED) {
            return true;
        }

        return false;
    }



    public static void searchIndex(String index, String type, String query) {

        TransportClient client = EsUtil.getInstance().getEsClient();

        SearchResponse rs = client.prepareSearch(index).setTypes(type)
                .setQuery(QueryBuilders.matchQuery("tradeType", query)).setSize(200)
                .get();


        SearchHit [] hits = rs.getHits().getHits();

        System.out.println(hits.length);

        for (SearchHit hit: hits) {
            System.out.println(hit.getSourceAsString());
        }

    }



}
