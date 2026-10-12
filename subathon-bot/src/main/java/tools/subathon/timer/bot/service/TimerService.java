package tools.subathon.timer.bot.service;

import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import org.springframework.stereotype.Service;
import tools.subathon.timer.proto.timer.v1.PauseTimerRequest;
import tools.subathon.timer.proto.timer.v1.StartTimerRequest;
import tools.subathon.timer.proto.timer.v1.Timer;
import tools.subathon.timer.proto.timer.v1.TimerServiceGrpc;
import tools.subathon.timer.proto.timer.v1.TimerState;
import tools.subathon.timer.util.interfaces.HasLogger;

import java.time.Instant;

@Service
public class TimerService implements HasLogger {

    private final static String SOURCE = "twitch-chat";

    private final TimerServiceGrpc.TimerServiceBlockingStub timerRpcService;

    public TimerService(TimerServiceGrpc.TimerServiceBlockingStub timerRpcService) {
        this.timerRpcService = timerRpcService;
    }

    public boolean start(String channelId, String channelName) {
        Instant now = Instant.now();
        try {
            Timer response = timerRpcService.startTimer(
                    StartTimerRequest.newBuilder()
                            .setChannelId(channelId)
                            .setUsername(channelName)
                            .setTimestamp(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build())
                            .setSource(SOURCE)
                            .build()
            );
            return TimerState.TIMER_STATE_TICKING.equals(response.getState());

        } catch (StatusRuntimeException e) {
            getLogger().error("gRPC error starting timer. Status was: {}.", e.getStatus().getCode(), e);
            return false;
        }
    }

    public boolean pause(String channelId, String userName) {
        Instant now = Instant.now();
        try {
            Timer response = timerRpcService.pauseTimer(
                    PauseTimerRequest.newBuilder()
                            .setChannelId(channelId)
                            .setUsername(userName)
                            .setTimestamp(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build())
                            .setSource(SOURCE)
                            .build()
            );
            return TimerState.TIMER_STATE_PAUSED.equals(response.getState());
        } catch (StatusRuntimeException e) {
            getLogger().error("gRPC error pausing timer. Status was: {}.", e.getStatus().getCode(), e);
            return false;
        }
    }

    public boolean addTime(String channelId, String userName, long seconds) {
        Instant now = Instant.now();
        try {
            timerRpcService.addTime(
                    tools.subathon.timer.proto.timer.v1.AddTimeRequest.newBuilder()
                            .setChannelId(channelId)
                            .setUsername(userName)
                            .setSeconds(seconds)
                            .setTimestamp(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build())
                            .setSource(SOURCE)
                            .build()
            );
            return true;
        } catch (StatusRuntimeException e) {
            getLogger().error("gRPC error adding time to timer. Status was: {}.", e.getStatus().getCode(), e);
            return false;
        }
    }

    public boolean subtractTime(String channelId, String userName, long seconds) {
        Instant now = Instant.now();
        try {
            timerRpcService.subtractTime(
                    tools.subathon.timer.proto.timer.v1.SubtractTimeRequest.newBuilder()
                            .setChannelId(channelId)
                            .setUsername(userName)
                            .setSeconds(seconds)
                            .setTimestamp(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build())
                            .setSource(SOURCE)
                            .build()
            );
            return true;
        } catch (StatusRuntimeException e) {
            getLogger().error("gRPC error subtracting time from timer. Status was: {}.", e.getStatus().getCode(), e);
            return false;
        }
    }
}
