package com.example.guessthecelebrity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> celebURLS= new ArrayList<>();
    ArrayList<String> celebNames= new ArrayList<>();
    ConstraintLayout resultview;
    int chosenCeleb=0;
    ImageView imageView;
    int locationOfCorrectAnswer;
    int score=0;
    int numberOfQuestions=0;
    String[] answers=new String[4];
    Button button0,button1,button2,button3;
    TextView resultTextView,points,timer;
    public void playAgain(View view) {
        score = 0;
        numberOfQuestions = 0;
        imageView.setVisibility(View.VISIBLE);
        button0.setVisibility(View.VISIBLE);
        button1.setVisibility(View.VISIBLE);
        button2.setVisibility(View.VISIBLE);
        button3.setVisibility(View.VISIBLE);
        resultview.setVisibility(ConstraintLayout.INVISIBLE);
        timer.setText("30s");
        points.setText("0/0");
        resultTextView.setText("");
        createNewQuestion();
        new CountDownTimer(30100, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
               timer.setText(String.valueOf(millisUntilFinished / 1000) + "s");
            }

            @Override
            public void onFinish() {
                imageView.setVisibility(View.INVISIBLE);
                button0.setVisibility(View.INVISIBLE);
                button1.setVisibility(View.INVISIBLE);
                button2.setVisibility(View.INVISIBLE);
                button3.setVisibility(View.INVISIBLE);
                resultview.setVisibility(ConstraintLayout.VISIBLE);
                timer.setText("0s");
                resultTextView.setText("Your Score " + Integer.toString(score) + "/" + Integer.toString(numberOfQuestions));
            }
        }.start();
    }
    public static class ImageDownloader extends AsyncTask<String,Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url=new URL(urls[0]);
                HttpURLConnection connection=(HttpURLConnection)url.openConnection();
                connection.connect();
                InputStream inputStream=connection.getInputStream();
                return BitmapFactory.decodeStream(inputStream);
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            URL url;
            try {
                url=new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in= urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data;
                data = reader.read();
                StringBuilder result = new StringBuilder();
                while (data !=-1) {
                    char current = (char) data;
                    result.append(current);
                    data  =  reader.read();
                }
                return result.toString();
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }
    public void celebChosen(View view){
        if (view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))){
            score++;
            Toast.makeText(MainActivity.this, "Correct", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(MainActivity.this, "Wrong! It was "+celebNames.get(chosenCeleb), Toast.LENGTH_SHORT).show();
        }
        numberOfQuestions++;
        points.setText(Integer.toString(score)+"/"+Integer.toString(numberOfQuestions));
        createNewQuestion();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView= findViewById(R.id.imageView);
        resultTextView= findViewById(R.id.textView4);
        points= findViewById(R.id.textViewpoints);
        timer= findViewById(R.id.textViewtimer);
        button0=findViewById(R.id.button);
        button1=findViewById(R.id.button2);
        button2=findViewById(R.id.button3);
        button3=findViewById(R.id.button4);
        resultview=findViewById(R.id.resultview);
        DownloadTask task= new DownloadTask();
        try {
            String result = task.execute("https://www.imdb.com/list/ls022431524/").get();
            Log.i("LIST OF NAMES",result);
            String[] SpiltResult=result.split("<div class=\"row text-center lister-working hidden\"></div>");
            Pattern p=Pattern.compile("src=\"https://m.media-amazon.com/images/M(.*?)\"");
            Matcher m=p.matcher(SpiltResult[0]);
            while (m.find()){
                celebURLS.add("https://m.media-amazon.com/images/M" + m.group(1));
                System.out.println(celebURLS);
            }
            p=Pattern.compile("img alt=\"(.*?)\"");
            m=p.matcher(SpiltResult[0]);
            while (m.find()){
                celebNames.add(m.group(1));
                System.out.println(celebNames);
            }
        }
        catch (ExecutionException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        playAgain(findViewById(R.id.button8));
    }
    public void createNewQuestion(){
        Random random=new Random();
        chosenCeleb=random.nextInt(celebURLS.size());
        ImageDownloader imageTask= new ImageDownloader();
        Bitmap celebImage;
        try {
            celebImage=imageTask.execute(celebURLS.get(chosenCeleb)).get();
            imageView.setImageBitmap(celebImage);
            locationOfCorrectAnswer=random.nextInt(4);
            int incorrectAnswerLocation;
            for (int i=0;i<4;i++){
                if (i==locationOfCorrectAnswer){
                    answers[i]=celebNames.get(chosenCeleb);
                }
                else {
                    incorrectAnswerLocation=random.nextInt(celebNames.size());
                    while (incorrectAnswerLocation==chosenCeleb){

                        incorrectAnswerLocation=random.nextInt(celebNames.size());
                    }
                    answers[i]=celebNames.get(incorrectAnswerLocation);
                }
            }
            button0.setText(answers[0]);
            button1.setText(answers[1]);
            button2.setText(answers[2]);
            button3.setText(answers[3]);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}