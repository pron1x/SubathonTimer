package tools.subathon.rpc;

public class RpcResponse<T> {

    private T body;
    private RpcStatus statusCode;
    private String errorMessage;

    public static <T> RpcResponse<T> of(T body) {
        RpcResponse<T> response = new RpcResponse<>();
        if (body != null) {
            response.setBody(body);
            response.setStatusCode(RpcStatus.OK);
        } else {
            response.setStatusCode(RpcStatus.NOT_FOUND);
        }
        return response;
    }

    public static <T> RpcResponse<T> of() {
        return of(null);
    }

    public static <T> RpcResponse<T> error(String errorMessage) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setStatusCode(RpcStatus.ERROR);
        response.setErrorMessage(errorMessage);
        return response;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    public RpcStatus getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(RpcStatus statusCode) {
        this.statusCode = statusCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
