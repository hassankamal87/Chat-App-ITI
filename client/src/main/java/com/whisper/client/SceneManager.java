package com.whisper.client;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

public class SceneManager {
    private static final SceneManager instance = new SceneManager();

    private Stage primaryStage;

    private final Map<String, Scene> scenes = new HashMap<>();
    private static final int SCENE_WIDTH = 800;
    private static final int SCENE_HEIGHT = 600;

    public static SceneManager getInstance() {
        return instance;
    }
    public void initStage(Stage stage){
        if(primaryStage != null){
            throw new IllegalArgumentException("The Stage has already been initialized");
        }
        primaryStage = stage;
    }

    public void loadView(String name){
        if(primaryStage==null){
            throw new RuntimeException("Stage Coordinator should be " +
                    "initialized with a Stage before it could be used");
        }
        if(!scenes.containsKey(name)){
            FXMLLoader fxmlLoader = new FXMLLoader(getClass()
                    .getResource(String.format("/views/%s.fxml", name)));
            Parent root = null;
            try {
                root = fxmlLoader.load();
            } catch (IOException e) {
                e.toString();
            }
            Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
            scenes.put(name, scene);
        }
        primaryStage.setScene(scenes.get(name));
    }
    public Parent loadPane(String name){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass()
                .getResource(String.format("/views/%s.fxml", name)));
        Parent root = null;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            e.toString();
        }
        return root;
    }
}