package com.pronixxx.subathon.seimporter.model;

import java.net.URI;

public class StreamElementsBitEventModel extends StreamElementsEventModel {

    private BitEventData data;

    public BitEventData getData() {
        return data;
    }

    public void setData(BitEventData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "StreamElementsBitEventModel{" +
                "data=" + data +
                "} " + super.toString();
    }

    public static class BitEventData {
        private int amount;
        private int quantity;
        private String displayName;
        private String providerId;
        private URI avatar;
        private String message;
        private String username;

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getProviderId() {
            return providerId;
        }

        public void setProviderId(String providerId) {
            this.providerId = providerId;
        }

        public URI getAvatar() {
            return avatar;
        }

        public void setAvatar(URI avatar) {
            this.avatar = avatar;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        @Override
        public String toString() {
            return "BitEventData{" +
                    "amount=" + amount +
                    ", quantity=" + quantity +
                    ", displayName='" + displayName + '\'' +
                    ", providerId='" + providerId + '\'' +
                    ", avatar=" + avatar +
                    ", message='" + message + '\'' +
                    ", username='" + username + '\'' +
                    '}';
        }
    }
}
