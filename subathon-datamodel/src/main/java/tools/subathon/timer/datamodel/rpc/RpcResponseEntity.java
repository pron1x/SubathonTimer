package tools.subathon.timer.datamodel.rpc;

public class RpcResponseEntity<T> {

    private T body;
    private RpcStatus statusCode;
    private String errorMessage;

    public static <T> RpcResponseEntity<T> of(T body) {
        RpcResponseEntity<T> response = new RpcResponseEntity<>();
        if (body != null) {
            response.setBody(body);
            response.setStatusCode(RpcStatus.OK);
        } else {
            response.setStatusCode(RpcStatus.NOT_FOUND);
        }
        return response;
    }

    public static <T> RpcResponseEntity<T> of() {
        return of(null);
    }

    public static <T> RpcResponseEntity<T> error(String errorMessage) {
        RpcResponseEntity<T> response = new RpcResponseEntity<>();
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
