package com.pronixxx.subathon.seimporter.model;

import java.net.URI;

public class StreamElementsTipModel extends StreamElementsEventModel {

    private TipEventData data;

    public TipEventData getData() {
        return data;
    }

    public void setData(TipEventData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "StreamElementsTipModel{" +
                "data=" + data +
                "} " + super.toString();
    }

    public static class TipEventData {
        private double amount;
        private String displayName;
        private String providerId;
        private String currency;
        private URI avatar;
        private String message;
        private String username;

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
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

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
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
            return "TipEventData{" +
                    "amount=" + amount +
                    ", displayName='" + displayName + '\'' +
                    ", providerId='" + providerId + '\'' +
                    ", currency='" + currency + '\'' +
                    ", avatar=" + avatar +
                    ", message='" + message + '\'' +
                    ", username='" + username + '\'' +
                    '}';
        }
    }
}
