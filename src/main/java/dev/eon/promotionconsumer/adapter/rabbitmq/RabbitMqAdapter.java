package dev.eon.promotionconsumer.adapter.rabbitmq;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.rabbitmq.client.*;
import dev.eon.promotionconsumer.util.ValueUtil;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class RabbitMqAdapter {
    private Connection _connection;
    private Channel _channel;

    public RabbitMqAdapter(String host, String username, String password) throws IOException, TimeoutException {
        this.createInstance(host, username, password);
    }

    private void createInstance(String host, String username, String password) throws IOException, TimeoutException {
        ConnectionFactory newConnectionFactory = new ConnectionFactory();

        newConnectionFactory.setHost(host);
        newConnectionFactory.setUsername(username);
        newConnectionFactory.setPassword(password);

        try {
            this._connection = newConnectionFactory.newConnection(Executors.newSingleThreadExecutor());
            this._channel = this._connection.createChannel();
            this._channel.basicQos(0);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void createQueue(String name) throws IOException {
        this._channel.queueDeclare(name, true, false, false, null);
    }

    public void pushMessage(String queueName, String data) throws IOException {
        this.createQueue(queueName);
        this._channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, data.getBytes());
        System.out.println("Pushed Message: " + data + "\nTo: " + queueName);
    }

    public void consumeMessage(String queueName, RunnableProcess action) throws IOException {
        this.createQueue(queueName);
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            action.runProcess(message);
            System.out.println("Consumed Message: " + message + "\nFrom: " + queueName);
        };
        this._channel.basicConsume(queueName, true, deliverCallback, (consumerTag) -> {
        });
    }

    public static UUID getRequestId(String payload) throws Exception {
        try {
            try {
                JsonObject currentRequest = (JsonObject) ValueUtil.gson.fromJson(payload, JsonObject.class);
                String rawRequestId = currentRequest.get("requestId").getAsString();
                if (rawRequestId != null) {
                    try {
                        UUID currentRequestId = UUID.fromString(rawRequestId);
                        return currentRequestId;
                    } catch (IllegalArgumentException var4) {
                        throw new Exception("Incorrect UUID Format");
                    }
                } else {
                    return null;
                }
            } catch (JsonSyntaxException var5) {
                throw new Exception("Payload is not in JSON Format");
            }
        } catch (Throwable e) {
            throw e;
        }
    }
}
