package com.example.authvk;

public class FriendVK {
    private String name;
    private String imageURL;

    public FriendVK() {
    }

    public FriendVK(String name, String imageURL) {
        this.name = name;
        this.imageURL = imageURL;
    }

    public String getName() {
        return name;
    }

    public String getImageURL() {
        return imageURL;
    }
}
