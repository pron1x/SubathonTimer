package tools.subathon.rpc;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Objects;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = RpcResponse.Success.class, name = "success"),
        @JsonSubTypes.Type(value = RpcResponse.Failure.class, name = "failure")
})
public sealed interface RpcResponse<T> permits RpcResponse.Success, RpcResponse.Failure {

    record Success<T>(T body) implements RpcResponse<T> {}

    record Failure<T>(RpcStatus statusCode, String errorMessage) implements RpcResponse<T> {}

    static <T> RpcResponse<T> ok(T body) {
        if(body == null) {
            throw new IllegalArgumentException("Success body must not be null");
        }
        return new Success<>(body);
    }

    static <T> RpcResponse<T> notFound() {
        return new Failure<>(RpcStatus.NOT_FOUND, "Resource not found");
    }

    static <T> RpcResponse<T> error(String message) {
        return new Failure<>(RpcStatus.ERROR, Objects.requireNonNull(message, "Error message must not be null"));
    }

    static <T> RpcResponse<T> timeout() {
        return new Failure<>(RpcStatus.ERROR, "Request timed out");
    }

}
