package tools.subathon.timer.datamodel.rpc;

import java.util.Map;

public class RpcRequestEntity<T> {

    private RpcAction action;
    private Map<String, Object> params;
    T body;

    public RpcAction getAction() {
        return action;
    }

    public void setAction(RpcAction action) {
        this.action = action;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

}
