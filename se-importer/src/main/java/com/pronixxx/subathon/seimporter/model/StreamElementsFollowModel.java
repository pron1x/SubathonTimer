package com.pronixxx.subathon.seimporter.model;

import java.net.URI;

public class StreamElementsFollowModel extends StreamElementsEventModel {
    private FollowEventData data;

    public FollowEventData getData() {
        return data;
    }

    public void setData(FollowEventData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "SEFollowModel{" +
                "data=" + data +
                "} " + super.toString();
    }

    public class FollowEventData {
        private int quantity;
        private String displayName;
        private String providerId;
        private URI avatar;
        private String username;

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

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        @Override
        public String toString() {
            return "FollowEventData{" +
                    "quantity=" + quantity +
                    ", displayName='" + displayName + '\'' +
                    ", providerId='" + providerId + '\'' +
                    ", avatar=" + avatar +
                    ", username='" + username + '\'' +
                    '}';
        }
    }
}
