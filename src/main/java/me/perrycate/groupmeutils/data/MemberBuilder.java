package me.perrycate.groupmeutils.data;

public class MemberBuilder {
    private String userId; // use this, not id.
    private String nickname;
    private String imageUrl;
    private String id;
    private boolean muted;
    private boolean autokicked;

    public Member createMember() {
        return new Member(userId, nickname, imageUrl, id, muted, autokicked);
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public void setAutokicked(boolean autokicked) {
        this.autokicked = autokicked;
    }

}
