package tools.subathon.timer.ui.service;

import com.google.protobuf.Timestamp;
import tools.subathon.rpc.RpcResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.subathon.timer.datamodel.TimerDto;
import tools.subathon.timer.proto.timer.v1.GetCurrenTimerForChannelRequest;
import tools.subathon.timer.proto.timer.v1.InitializeNewTimerRequest;
import tools.subathon.timer.proto.timer.v1.PauseTimerRequest;
import tools.subathon.timer.proto.timer.v1.StartTimerRequest;
import tools.subathon.timer.proto.timer.v1.TimerServiceGrpc;
import tools.subathon.timer.ui.mapper.TimerMapper;

import java.time.Instant;

@Service
public class TimerService {

    private static final String SOURCE = "UI";

    private final TimerMapper timerMapper;
    private final DataserviceRpcService dataserviceRpcService;
    private final TimerServiceGrpc.TimerServiceBlockingStub timerRpcService;

    @Autowired
    public TimerService(TimerMapper timerMapper, DataserviceRpcService dataserviceRpcService, TimerServiceGrpc.TimerServiceBlockingStub timerRpcService) {
        this.timerMapper = timerMapper;
        this.dataserviceRpcService = dataserviceRpcService;
        this.timerRpcService = timerRpcService;
    }

    public TimerDto getTimer(String channelId) {
        return timerMapper.protoToDto(
                timerRpcService.getCurrentTimerForChannel(
                        GetCurrenTimerForChannelRequest.newBuilder().setChannelId(channelId).build()
                )
        );
    }

    public TimerDto initialize(String channelId, String channelName) {
        Instant now = Instant.now();
        return timerMapper.protoToDto(
                timerRpcService.initializeNewTimer(
                    InitializeNewTimerRequest.newBuilder()
                            .setChannelId(channelId)
                            .setUsername(channelName)
                            .setTimestamp(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build())
                            .setSource(SOURCE)
                            .build()
                )
        );
    }

    public TimerDto start(String channelId, String channelName) {
        Instant now = Instant.now();
        return timerMapper.protoToDto(
                timerRpcService.startTimer(
                        StartTimerRequest.newBuilder()
                                .setChannelId(channelId)
                                .setUsername(channelName)
                                .setTimestamp(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build())
                                .setSource(SOURCE)
                                .build()
                )
        );
    }

    public TimerDto pause(String channelId, String channelName) {
        Instant now = Instant.now();
        return timerMapper.protoToDto(
                timerRpcService.pauseTimer(
                        PauseTimerRequest.newBuilder()
                                .setChannelId(channelId)
                                .setUsername(channelName)
                                .setTimestamp(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build())
                                .setSource(SOURCE)
                                .build()
                )
        );
    }

    @Deprecated
    public RpcResponse<TimerDto> getTimerForChannel(String channelId) {
        return dataserviceRpcService.getTimerForChannel(channelId);
    }

    @Deprecated
    public RpcResponse<TimerDto> initializeTimerForChannel(String channelId, String channelName) {
        return dataserviceRpcService.initializeTimerForChannel(channelId, channelName);
    }

    @Deprecated
    public RpcResponse<TimerDto> startTimerForChannel(String channelId, String channelName) {
        return dataserviceRpcService.startTimerForChannel(channelId, channelName);
    }

    @Deprecated
    public RpcResponse<TimerDto> pauseTimerForChannel(String channelId, String channelName) {
        return dataserviceRpcService.pauseTimerForChannel(channelId, channelName);
    }

}
