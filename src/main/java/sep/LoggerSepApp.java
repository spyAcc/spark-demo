package sep;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdata.sep.EventListener;
import com.ngdata.sep.PayloadExtractor;
import com.ngdata.sep.SepEvent;
import com.ngdata.sep.SepModel;
import com.ngdata.sep.impl.BasePayloadExtractor;
import com.ngdata.sep.impl.SepConsumer;
import com.ngdata.sep.impl.SepModelImpl;
import com.ngdata.sep.util.zookeeper.ZkUtil;
import com.ngdata.sep.util.zookeeper.ZooKeeperItf;
import data.PersonBean;
import dbutil.EsUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.util.Bytes;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LoggerSepApp {

    /**
     * A simple consumer that just logs the events.
     */
    public static void main(String[] args) throws Exception {
        Configuration conf = HBaseConfiguration.create();
        conf.setBoolean("hbase.replication", true);

        ZooKeeperItf zk = ZkUtil.connect("localhost", 20000);
        SepModel sepModel = new SepModelImpl(zk, conf);

        final String subscriptionName = "logger";

        if (!sepModel.hasSubscription(subscriptionName)) {
            sepModel.addSubscriptionSilent(subscriptionName);
        }

        PayloadExtractor payloadExtractor = new BasePayloadExtractor(Bytes.toBytes("sep-user-demo"), Bytes.toBytes("info"),
                Bytes.toBytes("payload"));

        SepConsumer sepConsumer = new SepConsumer(subscriptionName, 0, new EventLogger(), 1, "localhost", zk, conf,
                payloadExtractor);

        sepConsumer.start();
        System.out.println("Started");

        while (true) {
            Thread.sleep(Long.MAX_VALUE);
        }
    }

    private static class EventLogger implements EventListener {

        public static TransportClient client = EsUtil.getInstance().getEsClient();

        @Override
        public void processEvents(List<SepEvent> sepEvents) {

            BulkRequestBuilder bulkBuilder = client.prepareBulk();
            ObjectMapper mapper = new ObjectMapper();

            for (SepEvent sepEvent : sepEvents) {
                System.out.println("Received event:");
                System.out.println("  table = " + Bytes.toString(sepEvent.getTable()));
                System.out.println("  row = " + Bytes.toString(sepEvent.getRow()));

                PersonBean p = new PersonBean();

                String rowId = Bytes.toString(sepEvent.getRow());

                p.setRowId(rowId);

                for (Cell kv : sepEvent.getKeyValues()) {

                    String k = new String(kv.getQualifierArray(), kv.getQualifierOffset(), kv.getQualifierLength());
                    String v = new String(kv.getValueArray(), kv.getValueOffset(), kv.getValueLength());

                    if(k.equals("name")) {
                        p.setName(v);
                    } else if(k.equals("age")) {
                        p.setAge(Integer.parseInt(v));
                    } else if(k.equals("email")) {
                        p.setEmail(v);
                    } else if(k.equals("payload")) {
                        p.setPlayload(v);
                    }

                }


                try {
                    System.out.println(mapper.writer().writeValueAsString(p));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                try {
                        bulkBuilder.add(client.prepareIndex("person", "info").setSource(

                                XContentFactory.jsonBuilder()
                                        .startObject()
                                        .field("name", p.getName())
                                        .field("age", p.getAge())
                                        .field("email", p.getEmail())
                                        .field("payload", p.getPlayload())
                                        .endObject()

                        ));

                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println("add ok");

            }

            bulkBuilder.execute().actionGet();

            System.out.println("es ok");

        }





    }


}
