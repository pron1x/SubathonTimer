package tools.subathon.rpc.payload.response;

import java.util.List;

public record BatchResponse(BatchStatus batchStatus, List<ItemResult> items) {

    public record ItemResult(String name, String id, Status status, String message) {

        public enum Status {
            SUCCESS, FAILED
        }
    };

    public enum BatchStatus {
        SUCCESS,
        PARTIAL_SUCCESS,
        FAILURE
    }
}
