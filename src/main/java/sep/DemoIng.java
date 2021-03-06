package sep;

/*
 * Copyright 2012 NGDATA nv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DemoIng {
    private List<String> names;
    private List<String> domains;

    public static void main(String[] args) throws Exception {
        new DemoIng().run();
    }

    public void run() throws Exception {
        Configuration conf = HBaseConfiguration.create();

        DemoSchema.createSchema(conf);

        final byte[] infoCf = Bytes.toBytes("info");

        // column qualifiers
        final byte[] nameCq = Bytes.toBytes("name");
        final byte[] emailCq = Bytes.toBytes("email");
        final byte[] ageCq = Bytes.toBytes("age");
        final byte[] payloadCq = Bytes.toBytes("payload");

        loadData();

        ObjectMapper jsonMapper = new ObjectMapper();

        Table htable = ConnectionFactory.createConnection(conf).getTable(TableName.valueOf("sep-user-demo"));

        int n = 20;
        while (n > 0) {
            byte[] rowkey = Bytes.toBytes(UUID.randomUUID().toString());
            Put put = new Put(rowkey);

            String name = pickName();
            String email = name.toLowerCase() + "@" + pickDomain();
            String age = String.valueOf((int) Math.ceil(Math.random() * 100));

            put.addColumn(infoCf, nameCq, Bytes.toBytes(name));
            put.addColumn(infoCf, emailCq, Bytes.toBytes(email));
            put.addColumn(infoCf, ageCq, Bytes.toBytes(age));

            MyPayload payload = new MyPayload();
            payload.setPartialUpdate(false);
            put.addColumn(infoCf, payloadCq, jsonMapper.writeValueAsBytes(payload));

            htable.put(put);
            System.out.println("Added row " + Bytes.toString(rowkey));
            n--;
        }
    }

    private String pickName() {
        return names.get((int)Math.floor(Math.random() * names.size()));
    }

    private String pickDomain() {
        return domains.get((int)Math.floor(Math.random() * domains.size()));
    }

    private void loadData() throws IOException {
        // Names
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("names.txt")));

        names = new ArrayList<String>();

        String line;
        while ((line = reader.readLine()) != null) {
            names.add(line);
        }

        // Domains
        domains = new ArrayList<String>();
        domains.add("gmail.com");
        domains.add("hotmail.com");
        domains.add("yahoo.com");
        domains.add("live.com");
        domains.add("ngdata.com");
    }



    static class DemoSchema {

        public static void createSchema(Configuration hbaseConf) throws IOException {
            Admin admin = ConnectionFactory.createConnection(hbaseConf).getAdmin();
            if (!admin.tableExists(TableName.valueOf("sep-user-demo"))) {
                HTableDescriptor tableDescriptor = new HTableDescriptor(TableName.valueOf("sep-user-demo"));

                HColumnDescriptor infoCf = new HColumnDescriptor("info");
                infoCf.setScope(1);
                tableDescriptor.addFamily(infoCf);

                admin.createTable(tableDescriptor);
            }
            admin.close();
        }
    }


    static class MyPayload {
        boolean partialUpdate = false;

        public boolean isPartialUpdate() {
            return partialUpdate;
        }

        public void setPartialUpdate(boolean partialUpdate) {
            this.partialUpdate = partialUpdate;
        }
    }


}
