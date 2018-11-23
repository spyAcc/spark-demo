package com.spy.spark.util;

import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

public class KafkaParams {

    public HashMap<String, Object> params;

    public Collection<String> topics = Arrays.asList("message-shuffle");

    public KafkaParams() {
        this.params = new HashMap<>();
        this.params.put("bootstrap.servers", "localhost:9092");
        this.params.put("key.deserializer", StringDeserializer.class);
        this.params.put("value.deserializer", StringDeserializer.class);
        this.params.put("enable.auto.commit", false);
        this.params.put("auto.offset.reset", "latest");
        this.params.put("group.id", "streaming-message");
    }


}
