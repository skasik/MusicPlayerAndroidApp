package com.asik.musicplayer;

public class LanguageModel {
    String languageName;
    String parameter;
    int buttonId;

    public LanguageModel(String languageName, int buttonId) {
        this.languageName = languageName;
        this.parameter = languageName.toLowerCase();
        this.buttonId = buttonId;
    }
    public LanguageModel(String languageName, String parameter, int buttonId) {
        this.languageName = languageName;
        this.parameter = parameter;
        this.buttonId = buttonId;
    }

    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public int getButtonId() {
        return buttonId;
    }

    public void setButtonId(int buttonId) {
        this.buttonId = buttonId;
    }
}
