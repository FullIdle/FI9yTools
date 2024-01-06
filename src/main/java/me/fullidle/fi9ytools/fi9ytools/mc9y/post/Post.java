package me.fullidle.fi9ytools.fi9ytools.mc9y.post;

public interface Post {
    public static enum PostType {
        DEFAULT,
        RESOURCE
    }
    public String getTitle();
    public String getPublisher();
    public PostType getPostType();
    public String getPostURL();
}
