package me.perrycate.groupmeutils.data;

public final class Member {

    private String userId; // use this, not id.
    private String nickname;
    private String imageUrl;
    private String id;
    private boolean muted;
    private boolean autokicked;

    public Member(String userId, String nickname, String imageUrl, String id,
            boolean muted, boolean autokicked) {
        super();
        this.userId = userId;
        this.nickname = nickname;
        this.imageUrl = imageUrl;
        this.id = id;
        this.muted = muted;
        this.autokicked = autokicked;
    }

    public String getUserId() {
        return userId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getId() {
        return id;
    }

    public boolean isMuted() {
        return muted;
    }

    public boolean isAutokicked() {
        return autokicked;
    }

}
