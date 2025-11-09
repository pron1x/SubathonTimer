package tools.subathon.rpc.payload.timer;

import tools.subathon.rpc.payload.RpcPayload;

public sealed interface TimerPayload extends RpcPayload permits GetTimerPayload, InitTimerPayload, PauseTimerPayload,
        StartTimerPayload, IncrementTimerPayload, DecrementTimerPayload
{

}

