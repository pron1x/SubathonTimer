package tools.subathon.timer.dataservice.rpc;

import io.grpc.stub.StreamObserver;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import tools.subathon.timer.datamodel.SubathonCommandEvent;
import tools.subathon.timer.datamodel.TimerDto;
import tools.subathon.timer.datamodel.enums.Command;
import tools.subathon.timer.dataservice.data.mapper.TimerMapper;
import tools.subathon.timer.dataservice.service.TimerService;
import tools.subathon.timer.dataservice.service.exception.MissingChannelConfigurationException;
import tools.subathon.timer.dataservice.service.exception.MissingTimerException;
import tools.subathon.timer.proto.timer.GetCurrenTimerForChannelRequest;
import tools.subathon.timer.proto.timer.GetTimerRequest;
import tools.subathon.timer.proto.timer.InitializeNewTimerRequest;
import tools.subathon.timer.proto.timer.PauseTimerRequest;
import tools.subathon.timer.proto.timer.StartTimerRequest;
import tools.subathon.timer.proto.timer.Timer;
import tools.subathon.timer.proto.timer.TimerServiceGrpc;

import java.time.Instant;

@Service
public class TimerRpcService extends TimerServiceGrpc.TimerServiceImplBase {

    private final TimerMapper timerMapper;
    private final TimerService timerService;

    public TimerRpcService(TimerMapper timerMapper, TimerService timerService) {
        this.timerMapper = timerMapper;
        this.timerService = timerService;
    }

    @Override
    public void getTimer(GetTimerRequest request, StreamObserver<Timer> responseObserver) {
        super.getTimer(request, responseObserver);
    }

    @Override
    public void getCurrentTimerForChannel(GetCurrenTimerForChannelRequest request, StreamObserver<Timer> responseObserver) {
        TimerDto timer = timerService.getLatestTimerForChannel(request.getChannelId());
        if (timer != null) {
            responseObserver.onNext(timerMapper.dtoToProto(timer));
        } else {
            responseObserver.onError(new EntityNotFoundException());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void initializeNewTimer(InitializeNewTimerRequest request, StreamObserver<Timer> responseObserver) {
        Instant timestamp = Instant.ofEpochSecond(request.getTimestamp().getSeconds(), request.getTimestamp().getNanos());
        SubathonCommandEvent command = createEvent(Command.INIT, request.getUsername(), timestamp, request.getSource());
        try {
            TimerDto timer = timerService.startTimer(request.getChannelId(), command);
            responseObserver.onNext(timerMapper.dtoToProto(timer));
        } catch (MissingTimerException | MissingChannelConfigurationException e) {
            responseObserver.onError(e);
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void startTimer(StartTimerRequest request, StreamObserver<Timer> responseObserver) {
        Instant timestamp = Instant.ofEpochSecond(request.getTimestamp().getSeconds(), request.getTimestamp().getNanos());
        SubathonCommandEvent command = createEvent(Command.START, request.getUsername(), timestamp, request.getSource());
        try {
            TimerDto timer = timerService.startTimer(request.getChannelId(), command);
            responseObserver.onNext(timerMapper.dtoToProto(timer));
        } catch (MissingChannelConfigurationException | MissingTimerException e) {
            responseObserver.onError(e);
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void pauseTimer(PauseTimerRequest request, StreamObserver<Timer> responseObserver) {
        Instant timestamp = Instant.ofEpochSecond(request.getTimestamp().getSeconds(), request.getTimestamp().getNanos());
        SubathonCommandEvent command = createEvent(Command.PAUSE, request.getUsername(), timestamp, request.getSource());
        try {
            TimerDto timer = timerService.pauseTimer(request.getChannelId(), command);
            responseObserver.onNext(timerMapper.dtoToProto(timer));
        } catch (MissingTimerException e) {
            responseObserver.onError(e);
        } finally {
            responseObserver.onCompleted();
        }
    }

    private SubathonCommandEvent createEvent(Command cmd, String user, Instant timestamp, String source) {
        SubathonCommandEvent event = new SubathonCommandEvent();
        event.setCommand(cmd);
        event.setUsername(user);
        event.setTimestamp(timestamp);
        event.setSource(source);
        return event;
    }
}
