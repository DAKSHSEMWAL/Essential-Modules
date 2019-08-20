
/*
 * Copyright (c) This code is developed and maintained by Daksh Semwal
 * Github Repository https://gitlab.com/parmindersingh419/ccube-android.git
 */

package com.kuro.daksh.quizapp;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class QuizQuestionData {


    private Integer id;
    private String question;
    private String choice1;
    private String choice2;
    private String choice3;

    public QuizQuestionData(Integer id, String question, String choice1, String choice2, String choice3) {
        this.id = id;
        this.question = question;
        this.choice1 = choice1;
        this.choice2 = choice2;
        this.choice3 = choice3;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getChoice1() {
        return choice1;
    }

    public void setChoice1(String choice1) {
        this.choice1 = choice1;
    }

    public String getChoice2() {
        return choice2;
    }

    public void setChoice2(String choice2) {
        this.choice2 = choice2;
    }

    public String getChoice3() {
        return choice3;
    }

    public void setChoice3(String choice3) {
        this.choice3 = choice3;
    }

}
