package application.ui.pane;

import application.Main;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import utils.buttons.Buttons;
import utils.buttons.ImageButton;

public class ButtonsZoomPane extends BorderPane {

    HBox hbox;

    Button playStop;

    boolean runningAudio = false;

    /**
     * Initialize the constructor of this class
     * And create the gridPane with all buttons
     *
     * @param main
     * @param primaryStage
     */
    public ButtonsZoomPane(Main main, Stage primaryStage){
        super();

        Button zoom = createZoomButton(main, primaryStage);
        playStop = createPlayStopAudioFileButton(main);
        Button record = createRecordButton(main, primaryStage);

        hbox = new HBox(zoom, playStop, record);
        hbox.setSpacing(5);
        hbox.setAlignment(Pos.CENTER);
        BorderPane.setAlignment(hbox, Pos.CENTER);
        this.setCenter(hbox);

        this.setStyle("-fx-background-color: #535e65");
    }

    /**
     * Function that create the zoom button
     * This button permit returning to the home
     *
     * @param main
     * @param primaryStage
     * @return the button "zoom"
     */
    public Button createZoomButton(Main main, Stage primaryStage){
        Button zoom = new Button();
        zoom.setGraphic(ImageButton.createButtonImageView("images/zoom-.png"));
        zoom.getStyleClass().add("blue");
        zoom.setContentDisplay(ContentDisplay.TOP);
        zoom.setPrefHeight(50);
        zoom.setPrefWidth(300);
        zoom.setOnAction((e) -> {
            main.goToHome(primaryStage);
        });
        return zoom;
    }

    /**
     * Function that create the playStop button
     * This button permit listen or pause the audio file
     *
     * @param main
     * @return the button "playStop"
     */
    public Button createPlayStopAudioFileButton(Main main) {
        Button playStopAudioFile = new Buttons();
        playStopAudioFile.setGraphic(ImageButton.createButtonImageView("images/play.png"));
        playStopAudioFile.getStyleClass().add("blue");
        playStopAudioFile.setContentDisplay(ContentDisplay.TOP);
        playStopAudioFile.setPrefHeight(50);
        playStopAudioFile.setPrefWidth(300);
        playStopAudioFile.setOnAction((e) -> {
            if (runningAudio) {
                runningAudio = false;
                main.getZoomPane().getWaveZoomService().playStopMediaPlayer("pause");
                main.getZoomPane().setStep(false);
                ((ImageView) playStopAudioFile.getGraphic()).setImage(new Image("images/play.png"));
            } else {
                runningAudio = true;
                main.getZoomPane().getWaveZoomService().playStopMediaPlayer("play");
                main.getZoomPane().setStep(true);
                ((ImageView) playStopAudioFile.getGraphic()).setImage(new Image("images/pause.png"));
            }
        });
        return playStopAudioFile;
    }

    /**
     * Function that create the record button
     * This button permit going on the record page
     *
     * @param main
     * @param primaryStage
     * @return the button "record"
     */
    public Button createRecordButton(Main main, Stage primaryStage){
        Button record = new Buttons();
        record.setGraphic(ImageButton.createButtonImageView("images/record.png"));
        record.getStyleClass().add("blue");
        record.setContentDisplay(ContentDisplay.TOP);
        record.setPrefHeight(50);
        record.setPrefWidth(300);
        record.setOnAction((e) -> {
            this.stopMusic();
            main.getRecordPane().generateNameFile(main);
            main.getRecordPane().getCreateJson().setIsZoom(true);
            main.goToRecord(primaryStage);
        });
        return record;
    }

    /**
     * Function that stop the music if she is running
     */
    public void stopMusic(){
        if (runningAudio){
            playStop.fire();
        }
    }
}
