package com.kong.konnect.service.consumer;

import com.kong.konnect.config.Params;
import com.kong.konnect.model.CDCEventModel;
import com.kong.konnect.util.PropertiesHelper;
import com.kong.konnect.util.SerDeHelper;
import org.apache.http.HttpHost;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.client.transport.rest_client.RestClientTransport;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class Consumer implements Runnable{
    private final KafkaConsumer<String, String> consumer;
    private final Properties props;
    private final RestClient restClient;
    private final OpenSearchClient client;
    private final AtomicBoolean isClosed;
    private final String esIndexName;

    public Consumer(AtomicBoolean closed) {
        this.props = PropertiesHelper.getProperties();
        this.consumer = new KafkaConsumer<>(this.props);
        this.isClosed = closed;
        String esHost = this.props.getProperty(Params.ES_HOST);
        int esPort = Integer.parseInt(this.props.getProperty(Params.ES_PORT));
        this.restClient = RestClient.builder(new HttpHost(esHost, esPort)).build();
        this.client = new OpenSearchClient(
                new RestClientTransport(this.restClient, new JacksonJsonpMapper()));
        this.esIndexName = this.props.getProperty(Params.ES_INDEX_NAME);

    }

    public void processMessages(ConsumerRecords<String, String> records) throws IOException {
        BulkRequest.Builder bulkReqBuilder = new BulkRequest.Builder();
        for (ConsumerRecord<String, String> record: records) {
            CDCEventModel cdcEvent = SerDeHelper.deserialize(record.value(), CDCEventModel.class);
            switch (cdcEvent.getOp()) {
                case "c":
                    bulkReqBuilder.operations(
                            new BulkOperation.Builder().index(i ->  i.index(this.esIndexName).id(cdcEvent.getId()).document(cdcEvent.getAfter().getValue().getObject()))
                                    .build());
                    break;
                case "u":
                    bulkReqBuilder.operations(
                            new BulkOperation.Builder().update(u ->  u.index(this.esIndexName).id(cdcEvent.getId()).document(cdcEvent.getAfter().getValue().getObject()).docAsUpsert(true))
                                    .build());
                    break;
                case "d":
                    bulkReqBuilder.operations(
                            new BulkOperation.Builder().delete(d ->  d.id(cdcEvent.getId()))
                                    .build());
                    break;
            }
        }
        BulkResponse bulkResponse = this.client.bulk(bulkReqBuilder.build());

        if (bulkResponse.errors()) {
            System.err.println("Some documents failed to index:");
            System.err.println(bulkResponse.items());
        } else {
            System.out.println("All documents indexed successfully.");
        }
    }

    @Override
    public void run() {
        String topic = this.props.getProperty(Params.KAFKA_TOPIC_NAME);
        this.consumer.subscribe(Collections.singletonList(topic));
        try {
            while (true) {
                ConsumerRecords<String, String> records = this.consumer.poll(Duration.ofMillis(1000));
                System.out.println(records.count());
                if (records.isEmpty()) {
                    if (this.isClosed.get())  // if producer thread is done, try to auto close consumer
                        break;
                    continue;
                }
                this.processMessages(records);
                this.consumer.commitSync();
            }
            this.restClient.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            this.consumer.close();
        }
    }
}
