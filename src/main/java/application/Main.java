package application;

import application.ui.files.RecordVoice;
import application.ui.pane.*;
import utils.save.SaveFolder;
import utils.wave.WaveFormService;
import application.ui.pane.WavePane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;

import java.awt.*;

public class Main extends Application {

    @Getter
    DecoratedPane decoratedPane;
    @Getter
    ButtonsPane buttonsPane;
    @Getter
    OptionsPane optionsPane;
    @Getter
    EmptyPane emptyPane;

    SaveFolder saveFolder = new SaveFolder();
    RecordVoice recordVoice;
    WavePane wavePane;

    int widthScreen, heightScreen;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        saveFolder.createSaveFolderWindows();

        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        this.widthScreen = (int) dimension.getWidth();
        this.heightScreen = (int) dimension.getHeight();

        primaryStage.setWidth(this.widthScreen / 1.5);
        primaryStage.setHeight(this.heightScreen / 1.5);
        primaryStage.setFullScreen(true);
        primaryStage.setTitle("TranslateAudioFiles");


        optionsPane = new OptionsPane(this, primaryStage);
        buttonsPane = new ButtonsPane(this, primaryStage, this.widthScreen, saveFolder.getFolderPath());
        wavePane = new WavePane(200, 32);
        emptyPane = new EmptyPane();

        decoratedPane = new DecoratedPane(this, primaryStage);
        decoratedPane.setCenter(wavePane);
        decoratedPane.setBottom(buttonsPane);

        Scene scene = new Scene(decoratedPane, primaryStage.getWidth(), primaryStage.getHeight());
        scene.getStylesheets().add("style.css");
        primaryStage.setScene(scene);
        scene.setFill(Color.TRANSPARENT);
        primaryStage.initStyle(StageStyle.TRANSPARENT);

        primaryStage.show();
    }

    public void goToOption(Stage primaryStage){
        ((BorderPane) primaryStage.getScene().getRoot()).setCenter(optionsPane);
        ((BorderPane) primaryStage.getScene().getRoot()).setBottom(emptyPane);
    }

    public void goToHome(Stage primaryStage){
        primaryStage.getScene().setRoot(decoratedPane);
        ((BorderPane) primaryStage.getScene().getRoot()).setCenter(wavePane);
        ((BorderPane) primaryStage.getScene().getRoot()).setBottom(buttonsPane);
    }

    public void useAudioFile(String pathAudioFile){
        wavePane.getWaveService().startService(pathAudioFile, WaveFormService.WaveFormJob.AMPLITUDES_AND_WAVEFORM);
    }
}
