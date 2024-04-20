package com.kong.konnect;

import com.kong.konnect.service.consumer.Consumer;
import com.kong.konnect.service.producer.Producer;

import java.util.concurrent.atomic.AtomicBoolean;

public class App {
    public static void main(String[] args) throws InterruptedException {
        AtomicBoolean prodCloseFlag = new AtomicBoolean();
        Thread prodThread = new Thread(new Producer(prodCloseFlag));
        Thread consThread = new Thread(new Consumer(prodCloseFlag));
        prodThread.start();
        consThread.start();
        prodThread.join();
        consThread.join();
    }
}
