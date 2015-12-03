package com.theironyard;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

public class Main extends Application {

    static final int WIDTH = 800;
    static final int HEIGHT = 600;
    static final int ANT_COUNT = 100;

    ArrayList<Ant> ants;
    long lastTimestamp = 0;

    ArrayList<Ant> createAnts() {
        ArrayList<Ant> ants = new ArrayList();
        for (int i = 0; i < ANT_COUNT; i++) {
            Random r = new Random();
            ants.add(new Ant(r.nextInt(WIDTH), r.nextInt(HEIGHT)));
        }
        return ants;
    }

    void drawAnts(GraphicsContext context) {
        context.clearRect(0, 0, WIDTH, HEIGHT);
        for (Ant ant : ants) {
            if (aggravateAnt(ant)) {
                context.setFill(Color.RED);
            } else {
                context.setFill(Color.GREEN);
            }
            context.fillOval(ant.x, ant.y, 5, 5);
        }
    }

    double randomStep() {
        return Math.random() * 2 - 1; //multiplying by two and subtracting one so we get a number between -1 and 1
    }

    Ant moveAnt(Ant ant) { //give it an ant object and then it wants you to return it
        //slow it down to utilize parallelism
        try {
            Thread.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //ant.x = randomStep() + ant.x;
        //ant.y = randomStep() + ant.y;
        //or
        ant.x += randomStep();
        ant.y += randomStep();
        return ant;
    }

    boolean aggravateAnt(Ant ant) {
        for (Ant a : ants) {
            if (a != ant) {
                double distance = Math.sqrt(Math.pow((a.x - ant.x), 2) + Math.pow((a.y - ant.y), 2));
                if (distance <= Math.abs(10)) {
                    return true;
                }
            }
        }
        return false;
    }

    void updateAnts() {
        //changed stream to parallelstream below to utilize parallelism
        ants = ants.parallelStream()
                .map(this::moveAnt) //it says this is not a static method, we use the word this which is a way of saying get the particular object that we are inside of right now and then get that method
                .collect(Collectors.toCollection(ArrayList<Ant>::new));
    }

    int fps(long now) {
        double diff = now - lastTimestamp;
        double diffSeconds = diff / 1000000000;
        return (int) (1 / diffSeconds);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        Canvas canvas = (Canvas) scene.lookup("#canvas");
        Label fpsLabel = (Label) scene.lookup("#fps");
        GraphicsContext context = canvas.getGraphicsContext2D();

        primaryStage.setTitle("Ants");
        primaryStage.setScene(scene);
        primaryStage.show();

        ants = createAnts();

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                fpsLabel.setText(String.valueOf(fps(now)));
                lastTimestamp = now;
                updateAnts();
                drawAnts(context);
            }
        };
        timer.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
