package com.pronixxx.subathon.seimporter.model;

import java.net.URI;

public class StreamElementsSubscribeModel extends StreamElementsEventModel {

    private SubscribeEventData data;

    public SubscribeEventData getData() {
        return data;
    }

    public void setData(SubscribeEventData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "StreamElementsSubscribeModel{" +
                "data=" + data +
                "} " + super.toString();
    }

    public static class SubscribeEventData {
        private int amount;
        private int quantity;
        private String tier;
        private boolean gifted;
        private String sender;
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

        public String getTier() {
            return tier;
        }

        public void setTier(String tier) {
            this.tier = tier;
        }

        public boolean isGifted() {
            return gifted;
        }

        public void setGifted(boolean gifted) {
            this.gifted = gifted;
        }

        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
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
            return "SubscribeEventData{" +
                    "amount=" + amount +
                    ", quantity=" + quantity +
                    ", tier='" + tier + '\'' +
                    ", gifted=" + gifted +
                    ", sender='" + sender + '\'' +
                    ", displayName='" + displayName + '\'' +
                    ", providerId='" + providerId + '\'' +
                    ", avatar=" + avatar +
                    ", message='" + message + '\'' +
                    ", username='" + username + '\'' +
                    '}';
        }
    }
}
