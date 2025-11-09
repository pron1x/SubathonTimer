package tools.subathon.rpc;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tools.subathon.rpc.payload.RpcPayload;

public class RpcRequest<P extends RpcPayload> {

    private RpcCommand command;
    
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS,
            include = JsonTypeInfo.As.PROPERTY,
            property = "@class")
    private P payload;

    public RpcCommand getCommand() {
        return command;
    }

    public void setCommand(RpcCommand command) {
        this.command = command;
    }

    public P getPayload() {
        return payload;
    }

    public void setPayload(P payload) {
        this.payload = payload;
    }
}
