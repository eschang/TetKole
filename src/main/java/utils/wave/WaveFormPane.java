package utils.wave;

import application.ui.pane.ButtonsPane;
import application.ui.pane.WavePane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class WaveFormPane extends ResizableCanvas {

	private final float[] defaultWave;
	private float[] waveData;
	private Color backgroundColor;
	private Color foregroundColor;
	private Color transparentForeground;
	protected int width;
	protected int height;
	private double timerXPosition;
	private WavePane wavePane;
	private WaveFormService waveFormService;
	private ButtonsPane buttonsPane;
	private Stage primaryStage;

	private double leftBorder;
	private double rightBorder;
	private final int sizeBorder = 10;

	public WaveFormPane(ButtonsPane buttonsPane, Stage primaryStage, int width, int height) {
		defaultWave = new float[width];
		this.buttonsPane = buttonsPane;
		this.primaryStage = primaryStage;
		this.width = width;
		this.height = height;
		this.setWidth(width);
		this.setHeight(height);
		this.leftBorder = primaryStage.getWidth() / 3.0;
		this.rightBorder = (2 * primaryStage.getWidth()) / 3.0;
		this.timerXPosition = this.leftBorder;

		for (int i = 0; i < width; i++){
			defaultWave[i] = 0.28802148f;
		}
		waveData = defaultWave;

		backgroundColor = Color.web("#252525");
		setForeground(Color.ORANGE);
	}

	public void setForeground(Color color) {
		this.foregroundColor = color;
		transparentForeground = Color.rgb((int) ( foregroundColor.getRed() * 255 ), (int) ( foregroundColor.getGreen() * 255 ), (int) ( foregroundColor.getBlue() * 255 ), 0.3);
	}

	public void setupMediaPlayer(){
		waveFormService.startTimeMediaPlayer(this.getCurrentTime());
	}

	public double getTimerXPosition() {
		return timerXPosition;
	}

	public void setTimerXPosition(double timerXPosition) {
		this.timerXPosition = timerXPosition;
		if (this.timerXPosition > this.rightBorder){
			this.timerXPosition = this.leftBorder;
			this.buttonsPane.stopMusic();
			this.waveFormService.playStopMediaPlayer("stop");
			this.waveFormService.startTimeMediaPlayer(this.getCurrentTime());
		}
	}

	public void clear() {
		waveData = defaultWave;

		gc.setFill(backgroundColor);
		gc.fillRect(0, 0, width, height);

		gc.setStroke(foregroundColor);
		gc.strokeLine(0, height / 2.0, width, height / 2.0);
	}

	public void paintWaveForm() {

		gc.setFill(backgroundColor);
		gc.fillRect(0, 0, width, height);

		gc.setStroke(foregroundColor);
		if (waveData != null){
			for (int i = 0; i < waveData.length; i++) {
				if (!wavePane.getAnimationService().isRunning()) {
					clear();
					break;
				}
				int value = (int) ( waveData[i] * height );
				int y1 = ( height - 2 * value ) / 2;
				int y2 = y1 + 2 * value;
				gc.strokeLine(i, y1, i, y2);
			}
		}

		gc.setFill(Color.WHITE);
		gc.fillRect(this.leftBorder, 0, this.sizeBorder, height);
		gc.strokeText(this.calculTimeLeftBorder(), this.posLeftStrokeText(), this.adjustPosYStrokeText(), 500);
		gc.fillRect(this.rightBorder, 0, this.sizeBorder, height);
		gc.strokeText(this.calculTimeRightBorder(), this.posRightStrokeText(), 15, 500);

		gc.setFill(Color.RED);
		gc.fillOval(timerXPosition, 0, 1, height);

		gc.setFill(transparentForeground);
		gc.fillRect(0, 0, this.leftBorder, height);
		gc.fillRect((this.rightBorder + this.sizeBorder), 0, width, height);

	}

	public double posLeftStrokeText(){
		if (this.leftBorder < (primaryStage.getWidth() / 4)){
			return this.leftBorder + 5;
		}else {
			return this.leftBorder - 110;
		}
	}

	public double posRightStrokeText(){
		if (this.rightBorder > ((primaryStage.getWidth() / 4) * 3)){
			return this.rightBorder - 110;
		}else {
			return this.rightBorder + 25;
		}
	}

	public int adjustPosYStrokeText(){
		if ((this.rightBorder - this.leftBorder) < 100.0){
			return 30;
		}else {
			return 15;
		}
	}

	public String calculTimeLeftBorder(){
		double time = this.leftBorder / waveFormService.getRatioAudio();
		double milliTime = Math.round((this.leftBorder / waveFormService.getRatioAudio()) * 100.0) / 100.0;

		double hoursLeftBorderTime = time / 3600;
		double minutesLeftBorderTime = (time % 3600) / 60;
		double secondsLeftBorderTime = time % 60;

		String hours = String.valueOf(hoursLeftBorderTime);
		String onlyHours = hours.split("\\.")[0];
		String minutes = String.valueOf(minutesLeftBorderTime);
		String onlyMinutes = minutes.split("\\.")[0];
		String seconds = String.valueOf(secondsLeftBorderTime);
		String onlySeconds = seconds.split("\\.")[0];
		String milliSeconds = String.valueOf(milliTime);
		String onlyMilliSeconds = milliSeconds.substring(milliSeconds.indexOf(".")).substring(1);

		return onlyHours + "h:" + onlyMinutes + "min:" + onlySeconds + "s:" + onlyMilliSeconds + "ms";
	}

	public String calculTimeRightBorder(){
		double time = this.rightBorder / waveFormService.getRatioAudio();
		double milliTime = Math.round((this.rightBorder / waveFormService.getRatioAudio()) * 100.0) / 100.0;

		double hoursRightBorderTime = time / 3600;
		double minutesRightBorderTime = (time % 3600) / 60;
		double secondsRightBorderTime = time % 60;

		String hours = String.valueOf(hoursRightBorderTime);
		String onlyHours = hours.split("\\.")[0];
		String minutes = String.valueOf(minutesRightBorderTime);
		String onlyMinutes = minutes.split("\\.")[0];
		String seconds = String.valueOf(secondsRightBorderTime);
		String onlySeconds = seconds.split("\\.")[0];
		String milliSeconds = String.valueOf(milliTime);
		String onlyMilliSeconds = milliSeconds.substring(milliSeconds.indexOf(".")).substring(1);

		return onlyHours + "h:" + onlyMinutes + "min:" + onlySeconds + "s:" + onlyMilliSeconds + "ms";
	}

	public double getCurrentTime(){
		return (Math.round((this.leftBorder / waveFormService.getRatioAudio()) * 100.0) / 100.0);
	}

	public void resetBorders(){
		this.leftBorder = primaryStage.getWidth() / 3.0;
		this.rightBorder = (2 * primaryStage.getWidth()) / 3.0;
		this.timerXPosition = this.leftBorder;
	}

	public void setWaveVisualization(WavePane wavePane) {
		this.wavePane = wavePane;
	}

	public double getRatioAudio(){
		return this.waveFormService.getRatioAudio();
	}

	public double getLeftBorder(){
		return this.leftBorder;
	}

	public void setLeftBorder(double value){
		if (value < (this.rightBorder - this.sizeBorder - 1)){
			this.leftBorder = value;
			this.timerXPosition = value;
		}
	}

	public double getRightBorder(){
		return this.rightBorder;
	}

	public void setRightBorder(double value){
		if (value > (this.leftBorder + this.sizeBorder)){
			this.rightBorder = value;
		}
	}

	public int getSizeBorder(){
		return this.sizeBorder;
	}

	public void sendWaveService(WaveFormService waveFormService){
		this.waveFormService = waveFormService;
	}

	public void setWaveData(float[] waveData) {
		this.waveData = waveData;
	}

	public int getStartTimeChoose(){
		return (int) (this.leftBorder / waveFormService.getRatioAudio());
	}

	public int getEndTimeChoose(){
		return (int) (this.rightBorder / waveFormService.getRatioAudio());
	}
}
