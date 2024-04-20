package com.kong.konnect;

import com.kong.konnect.service.consumer.Consumer;
import com.kong.konnect.service.producer.Producer;

import java.util.concurrent.atomic.AtomicBoolean;

public class App {
    public static void main(String[] args) throws InterruptedException {
        AtomicBoolean closeFlag = new AtomicBoolean();
        Thread prodThread = new Thread(new Producer(closeFlag));
        Thread consThread = new Thread(new Consumer(closeFlag));
        prodThread.start();
        consThread.start();
        prodThread.join();
        consThread.join();
    }
}
