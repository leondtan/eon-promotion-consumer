package dev.eon.promotionconsumer;

import dev.eon.promotionconsumer.adapter.mail.MailAdapter;
import dev.eon.promotionconsumer.adapter.psql.PsqlAdapter;
import dev.eon.promotionconsumer.adapter.rabbitmq.RabbitMqAdapter;
import dev.eon.promotionconsumer.util.PropertiesReader;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class PromotionalConsumerApplication {

    public static void main(String[] args) throws IOException, TimeoutException {
        System.out.println("Promotion Consumer Started");

        PropertiesReader env = new PropertiesReader("config.properties");

        RabbitMqAdapter rabbitMq = new RabbitMqAdapter(
                env.getProperty("rabbitmq.host"),
                env.getProperty("rabbitmq.user"),
                env.getProperty("rabbitmq.password")
        );

        PsqlAdapter database = new PsqlAdapter(
                env.getProperty("database.host"),
                env.getProperty("database.username"),
                env.getProperty("database.password")
        );

        MailAdapter mail = new MailAdapter(
                env.getProperty("mail.host"),
                env.getProperty("mail.username"),
                env.getProperty("mail.password")
        );

        rabbitMq.consumeMessage("promotional", (payload) -> { MainProcess.runProcess(payload, env, database, mail); });
    }
}
