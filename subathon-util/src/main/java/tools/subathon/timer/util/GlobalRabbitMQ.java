package tools.subathon.timer.util;

public class GlobalRabbitMQ {

    private GlobalRabbitMQ() {
        // hide constructor
    }

    public static final String EXCHANGE_NAME = "subathon-exchange";

    public static final String EVENT_QUEUE_NAME = "event-queue";
    public static final String DATASERVICE_EVENT_ROUTING_KEY = "event.twitch";
    public static final String UI_EVENT_ROUTING_KEY = "event.timer";

    public static final String DATASERVICE_RPC_QUEUE_NAME = "dataservice-rpc-queue";
    public static final String DATASERVICE_RPC_ROUTING_KEY = "rpc.dataservice";


    public static final String UI_RPC_QUEUE_NAME = "ui-rpc-queue";
    public static final String UI_RPC_ROUTING_KEY = "rpc.ui";

    public static final String BOT_RPC_QUEUE_NAME = "bot-rpc-queue";
    public static final String BOT_RPC_ROUTING_KEY = "rpc.bot";
}
