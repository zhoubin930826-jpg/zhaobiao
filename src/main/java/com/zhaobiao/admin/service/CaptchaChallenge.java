package com.zhaobiao.admin.service;

public class CaptchaChallenge {

    private final String captchaId;
    private final String scene;
    private final String code;
    private final byte[] image;

    public CaptchaChallenge(String captchaId, String scene, String code, byte[] image) {
        this.captchaId = captchaId;
        this.scene = scene;
        this.code = code;
        this.image = image;
    }

    public String getCaptchaId() {
        return captchaId;
    }

    public String getScene() {
        return scene;
    }

    public String getCode() {
        return code;
    }

    public byte[] getImage() {
        return image;
    }
}
