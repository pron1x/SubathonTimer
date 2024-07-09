package com.pronixxx.subathon.util;

public class GlobalRabbitMQ {
    public static final String EXCHANGE_NAME = "subathon-exchange";

    public static final String SUBATHON_QUEUE_NAME = "subathon-queue";
    public static final String SUBATHON_ROUTING_KEY = "subathon.event.#";

    public static final String BOT_QUEUE_NAME = "bot-queue";
    public static final String BOT_ROUTING_KEY = "bot.event.#";

    public static final String TIMER_EVENT_QUEUE_NAME = "timer-event-queue";
    public static final String TIMER_EVENT_ROUTING_KEY = "timer.event.#";
}
