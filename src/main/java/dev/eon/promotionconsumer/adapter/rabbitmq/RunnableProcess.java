package dev.eon.promotionconsumer.adapter.rabbitmq;

public interface RunnableProcess {
    void runProcess(String payload);
}
