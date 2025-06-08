package tools.subathon.timer.util;

public class GlobalRabbitMQ {

    private GlobalRabbitMQ() {
        // hide constructor
    }

    public static final String EXCHANGE_NAME = "subathon-exchange";

    public static final String TIMER_EVENT_QUEUE = "timer-event-queue"; // Bound to 'event.timer' in  ui
    public static final String TIMER_EVENT_ROUTING_KEY = "event.timer"; // Routes to timer-event-queue

    public static final String TWITCH_EVENT_QUEUE = "twitch-event-queue"; // Bound to 'event.twitch' in dataservice
    public static final String TWITCH_EVENT_ROUTING_KEY = "event.twitch"; // Routes to twitch-event-queue

    public static final String DATASERVICE_RPC_QUEUE_NAME = "dataservice-rpc-queue"; // Bound to rpc.dataservice
    public static final String DATASERVICE_RPC_ROUTING_KEY = "rpc.dataservice"; // Routes to dataservice-rpc-queue


    public static final String UI_RPC_QUEUE_NAME = "ui-rpc-queue"; // Bound to rpc.ui
    public static final String UI_RPC_ROUTING_KEY = "rpc.ui"; // Routes to ui-rpc-queue

    public static final String BOT_RPC_QUEUE_NAME = "bot-rpc-queue"; // Bound to rpc.bot
    public static final String BOT_RPC_ROUTING_KEY = "rpc.bot"; // Routes to bot-rpc-queue

    public static final String SEIMPORTER_RPC_QUEUE_NAME = "seimporter-rpc-queue";
    public static final String SEIMPORTER_RPC_ROUTING_KEY = "rpc.seimporter";
}
