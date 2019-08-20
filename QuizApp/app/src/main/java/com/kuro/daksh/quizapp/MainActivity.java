package com.kuro.daksh.quizapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.buttonA)
    RadioButton buttonA;
    @BindView(R.id.buttonB)
    RadioButton buttonB;
    @BindView(R.id.buttonC)
    RadioButton buttonC;
    @BindView(R.id.group)
    RadioGroup group;
    @BindView(R.id.next)
    Button next;
    @BindView(R.id.prev)
    Button prev;

    @BindView(R.id.triviaQuestion)
    TextView questionText;
    @BindView(R.id.reset)
    TextView reset;
    @BindView(R.id.descrption)
    TextView description;

    int ansid;
    private Dialog progressDialog;

    @BindView(R.id.totalquestion)
    TextView totalquestion;
    QuizQuestionData currentQuestion;

    int qid = 0;
    List<QuizQuestionData> list= new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //progressDialog = showProgressDialog(this);

        buttonA.setTextColor(getResources().getColor(R.color.black));
        buttonB.setTextColor(getResources().getColor(R.color.black));
        buttonC.setTextColor(getResources().getColor(R.color.black));
        /*progressDialog.show();
        final Thread t = new Thread() {
            @Override
            public void run() {
                int jumpTime = 0;
                while (jumpTime < totalprogressTime) {

                    try {
                        sleep(3000
                        );
                        jumpTime += 5;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        };
        t.start();
        getQuiz();*/
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.buttonA:
                        ansid=1;
                        break;

                    case R.id.buttonB:
                        ansid=2;
                        break;

                    case R.id.buttonC:
                        ansid=3;
                        break;
                }
            }
        });
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                group.clearCheck();
            }
        });
        list.add(new QuizQuestionData(1,"What is Chen-7?","A Level 5 planet like Earth","A deadly plague","A habitable asteroid"));
        list.add(new QuizQuestionData(2,"What were the last words that the Tenth Doctor said in The End of Time - Part 2 before he regenerated, and what episode did he repeat them in?","I don't want to go… / The Day of The Doctor","Why do I have to go? / The Stolen Earth","I don't want to go…/ The Stolen Earth"));
        list.add(new QuizQuestionData(3,"Which planet are the Slitheen from?","A Level 5 planet like Earth","A deadly plague","A habitable asteroid"));
        list.add(new QuizQuestionData(4,"What was the name Henry Van Statten gave to his prize captured alien?","Dalek","Rusty","Metaltron"));
        list.add(new QuizQuestionData(5,"What is Chen-7?","A Level 5 planet like Earth","A deadly plague","A habitable asteroid"));
        list.add(new QuizQuestionData(6,"What were the last words that the Tenth Doctor said in The End of Time - Part 2 before he regenerated, and what episode did he repeat them in?","I don't want to go… / The Day of The Doctor","Why do I have to go? / The Stolen Earth","I don't want to go…/ The Stolen Earth"));
        list.add(new QuizQuestionData(7,"Which planet are the Slitheen from?","A Level 5 planet like Earth","A deadly plague","A habitable asteroid"));
        list.add(new QuizQuestionData(8,"What was the name Henry Van Statten gave to his prize captured alien?","Dalek","Rusty","Metaltron"));
        list.add(new QuizQuestionData(9,"What is Chen-7?","A Level 5 planet like Earth","A deadly plague","A habitable asteroid"));
        list.add(new QuizQuestionData(10,"What were the last words that the Tenth Doctor said in The End of Time - Part 2 before he regenerated, and what episode did he repeat them in?","I don't want to go… / The Day of The Doctor","Why do I have to go? / The Stolen Earth","I don't want to go…/ The Stolen Earth"));
        list.add(new QuizQuestionData(11,"Which planet are the Slitheen from?","A Level 5 planet like Earth","A deadly plague","A habitable asteroid"));
        list.add(new QuizQuestionData(12,"What was the name Henry Van Statten gave to his prize captured alien?","Dalek","Rusty","Metaltron"));
        list.add(new QuizQuestionData(13,"What is Chen-7?","A Level 5 planet like Earth","A deadly plague","A habitable asteroid"));
        list.add(new QuizQuestionData(14,"What were the last words that the Tenth Doctor said in The End of Time - Part 2 before he regenerated, and what episode did he repeat them in?","I don't want to go… / The Day of The Doctor","Why do I have to go? / The Stolen Earth","I don't want to go…/ The Stolen Earth"));
        list.add(new QuizQuestionData(15,"Which planet are the Slitheen from?","A Level 5 planet like Earth","A deadly plague","A habitable asteroid"));
        list.add(new QuizQuestionData(16,"What was the name Henry Van Statten gave to his prize captured alien?","Dalek","Rusty","Metaltron"));
        list.add(new QuizQuestionData(17,"What is Chen-7?","A Level 5 planet like Earth","A deadly plague","A habitable asteroid"));
        list.add(new QuizQuestionData(18,"What were the last words that the Tenth Doctor said in The End of Time - Part 2 before he regenerated, and what episode did he repeat them in?","I don't want to go… / The Day of The Doctor","Why do I have to go? / The Stolen Earth","I don't want to go…/ The Stolen Earth"));
        list.add(new QuizQuestionData(19,"Which planet are the Slitheen from?","A Level 5 planet like Earth","A deadly plague","A habitable asteroid"));
        list.add(new QuizQuestionData(20,"What was the name Henry Van Statten gave to his prize captured alien?","Dalek","Rusty","Metaltron"));
        list.add(new QuizQuestionData(21,"What is Chen-7?","A Level 5 planet like Earth","A deadly plague","A habitable asteroid"));
        list.add(new QuizQuestionData(22,"What were the last words that the Tenth Doctor said in The End of Time - Part 2 before he regenerated, and what episode did he repeat them in?","I don't want to go… / The Day of The Doctor","Why do I have to go? / The Stolen Earth","I don't want to go…/ The Stolen Earth"));
        list.add(new QuizQuestionData(23,"Which planet are the Slitheen from?","A Level 5 planet like Earth","A deadly plague","A habitable asteroid"));
        list.add(new QuizQuestionData(24,"What was the name Henry Van Statten gave to his prize captured alien?","Dalek","Rusty","Metaltron"));
        startQuiz();

    }


    private void startQuiz() {

        currentQuestion = list.get(qid);
        totalquestion.setText((qid+1)+"/"+list.size());
        updateQueAndOptions(list.size());
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!next.getText().equals("Submit")) {
                    //submitAnswer(currentQuestion.getId(),uniqueid,ansid);
                    qid++;
                    group.clearCheck();
                    currentQuestion = list.get(qid);
                    updateQueAndOptions(list.size());
                }
                else {
                    //submitAnswer(currentQuestion.getId(),uniqueid,ansid);
                    //submitQuiz(videoId,uniqueid,id);
                }

            }
        });
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qid--;
                group.clearCheck();
                currentQuestion = list.get(qid);
                updateQueAndOptions(list.size());
            }
        });


    }

    public void updateQueAndOptions(int size) {
        Log.i("qid", "" + qid);
        totalquestion.setText((qid+1)+"/"+list.size());
        ansid=0;
        //This method will setText for que and options
        if(qid==size-1)
            next.setText("Submit");
        else
            next.setText("Next");
        if (qid > 0)
            prev.setClickable(true);
        if (qid == 0)
            prev.setClickable(false);
        questionText.setText("Q " + (qid + 1) + ". " + currentQuestion.getQuestion());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            questionText.setText("Q " + (qid + 1) + ". " + Html.fromHtml(currentQuestion.getQuestion(), Html.FROM_HTML_MODE_COMPACT));
        } else {
            questionText.setText("Q " + (qid + 1) + ". " + Html.fromHtml(currentQuestion.getQuestion()));
        }
        buttonA.setText(currentQuestion.getChoice1());
        buttonB.setText(currentQuestion.getChoice2());
        buttonC.setText(currentQuestion.getChoice3());



    }
}
