package com.asik.musicplayer;

import java.util.ArrayList;

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

    public static Boolean isLanguagePresent(LanguageModel languageModel, ArrayList<LanguageModel> languages){
        for (int i=0; i<languages.size(); ++i){
            if (languages.get(i).getParameter().equals(languageModel.getParameter())) return true;
        }

        return false;
    }
    public static ArrayList<LanguageModel> removeLanguage(LanguageModel languageModel, ArrayList<LanguageModel> languages){
        ArrayList<LanguageModel> allLanguages = new ArrayList<>();
        for (int i=0; i<languages.size(); ++i){
            if (!languages.get(i).getParameter().equals(languageModel.getParameter())) {
                allLanguages.add(languages.get(i));
            }
        }
        return allLanguages;
    }
}
