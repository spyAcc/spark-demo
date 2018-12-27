package dbutil;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 * es 操作类，单例模式
 */
public class EsUtil {

    private static Logger logger = LoggerFactory.getLogger(EsUtil.class);

    private TransportClient esClient;

    private static EsUtil instance = new EsUtil();

    private EsUtil() {

        Properties prop = PropertiesUtil.getPropertiesUtilInstance("esConfig.properties").getProp();

        Settings settings = Settings.builder()
                .put("cluster.name", prop.getProperty("es.cluster.name"))
                .build();

        esClient = new PreBuiltTransportClient(settings);

        String clusterNodes = prop.getProperty("es.cluster.nodes");

        String [] nodes = clusterNodes.split(",");

        try {
            for(String node: nodes) {
                String [] ips = node.split(":");
                String ip = ips[0];
                String port = ips[1];
                esClient.addTransportAddress(new TransportAddress(InetAddress.getByName(ip), Integer.parseInt(port)));
            }
        } catch (UnknownHostException e) {
            logger.error("es ip and port error", e);
        }


    }

    public static EsUtil getInstance() {
        return instance;
    }


    public TransportClient getEsClient() {
        return esClient;
    }








}
