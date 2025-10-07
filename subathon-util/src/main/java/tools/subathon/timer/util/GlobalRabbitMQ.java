package tools.subathon.timer.util;

public class GlobalRabbitMQ {

    private GlobalRabbitMQ() {
        // hide constructor
    }

    // Topic exchange
    public static final String EXCHANGE_NAME = "subathon-exchange";

    // Fire-and-forget internal timer events
    public static final String TIMER_EVENT_QUEUE = "timer-event-queue"; // Bound to 'event.timer' in  ui
    public static final String TIMER_EVENT_ROUTING_KEY = "event.timer"; // Routes to timer-event-queue

    // Fire-and-forget twitch imported events
    public static final String TWITCH_EVENT_QUEUE = "twitch-event-queue"; // Bound to 'event.twitch' in dataservice
    public static final String TWITCH_EVENT_ROUTING_KEY = "event.twitch"; // Routes to twitch-event-queue

    // User-Configuration RPC queue
    public static final String USER_CONFIG_RPC_QUEUE =  "dataservice-user-config-rpc-queue";
    public static final String USER_CONFIG_ROUTING_KEY = "ui.dataservice.user-config";

    // Timer RPC queue [<src>.<dest>.<type>]
    public static final String TIMER_RPC_QUEUE = "dataservice-timer-rpc-queue";
    public static final String TIMER_ROUTING_KEY = "ui.dataservice.timer";
    public static final String BOT_COMMAND_ROUTING_KEY = "bot.dataservice.timer";

    // Bot Channel Management RPC queue
    public static final String CHANNEL_MANAGEMENT_QUEUE = "bot-channel-management-rpc-queue";
    public static final String CHANNEL_MANAGEMENT_ROUTING_KEY = "dataservice.bot.channel-management";

    // Importer Token Management RPC queue
    public static final String IMPORTER_MANAGEMENT_QUEUE = "importer-management-rpc-queue";
    public static final String IMPORTER_MANAGEMENT_ROUTING_KEY = "dataservice.importer.management";
}
