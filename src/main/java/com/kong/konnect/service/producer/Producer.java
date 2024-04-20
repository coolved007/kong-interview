package com.kong.konnect.service.producer;

import com.kong.konnect.config.Params;
import com.kong.konnect.model.CDCEventModel;
import com.kong.konnect.util.PropertiesHelper;
import com.kong.konnect.util.SerDeHelper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.*;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class Producer implements Runnable{
    private final KafkaProducer<String, String> producer;
    private final Properties props;
    private final AtomicBoolean isClosed;

    public Producer(AtomicBoolean closed) {
        this.props = PropertiesHelper.getProperties();
        this.producer = new KafkaProducer<>(this.props);
        this.isClosed = closed;
    }


    public void sendMessage(String topic, String key, String value) {
        ProducerRecord<String, String> producerRecord =
                new ProducerRecord<>(topic, key, value);
        this.producer.send(producerRecord);
    }

    @Override
    public void run() {
        String topic = this.props.getProperty(Params.KAFKA_TOPIC_NAME);
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream(Params.CDC_INPUT_FILE_NAME))))) {
            String line;
            while ((line = reader.readLine()) != null) {
                CDCEventModel cdcEvent = SerDeHelper.deserialize(line, CDCEventModel.class);
                this.sendMessage(topic, cdcEvent.getId(), line);
                Thread.sleep(10);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            this.producer.close();
            this.isClosed.set(true);  // to auto-close consumer thread rather than waiting infinitely
        }
    }
}
