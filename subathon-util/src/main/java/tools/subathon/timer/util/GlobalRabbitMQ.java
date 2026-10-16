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

}
