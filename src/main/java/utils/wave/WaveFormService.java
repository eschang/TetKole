package utils.wave;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.Random;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import application.Main;
import application.ui.pane.WavePane;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.info.MultimediaInfo;
import ws.schild.jave.progress.EncoderProgressListener;

public class WaveFormService extends Service<Boolean> {

	Main main;

	private static final double WAVEFORM_HEIGHT_COEFFICIENT = 1.3;
	private static final CopyOption[] options = new CopyOption[]{ COPY_ATTRIBUTES , REPLACE_EXISTING };
	private float[] resultingWaveform;
	private int[] wavAmplitudes;
	private String fileAbsolutePath;
	private final WavePane wavePane;
	private Stage primaryStage;
	private final Random random = new Random();
	private File temp1;
	private File temp2;
	private Encoder encoder;
	private ConvertProgressListener listener = new ConvertProgressListener();
	private WaveFormJob waveFormJob;
	private Media audioFile;
	private MediaPlayer mediaPlayer;
	private double durationAudioFile = 0;
	public String audioFileName;
	public String pathAudioFile;

	public enum WaveFormJob {
		AMPLITUDES_AND_WAVEFORM, WAVEFORM;
	}

	public WaveFormService(WavePane wavePane, Main main, Stage primaryStage) {

		this.main = main;
		this.wavePane = wavePane;
		this.primaryStage = primaryStage;

		setOnSucceeded(s -> done());
		setOnFailed(f -> failure());
		setOnCancelled(c -> failure());
	}

	public void startService(String fileAbsolutePath , WaveFormJob waveFormJob) {
		if (waveFormJob == WaveFormJob.WAVEFORM){
			cancel();
		}

		wavePane.stopPainterService();
		this.waveFormJob = waveFormJob;
		this.fileAbsolutePath = fileAbsolutePath;

		if (waveFormJob != WaveFormJob.WAVEFORM){
			this.wavAmplitudes = null;
		}

		restart();
	}

	public void setupMediaPlayer(String path){
		pathAudioFile = path;
		audioFileName = new File(path).getName();
		audioFile = new Media(new File(path).toURI().toString());
		mediaPlayer = new MediaPlayer(audioFile);

		mediaPlayer.setOnReady(new Runnable() {
			@Override
			public void run() {
				durationAudioFile = audioFile.getDuration().toSeconds();
				wavePane.setupMediaPlayer();
			}
		});
	}

	public void playStopMediaPlayer(String status){
		if (audioFile != null){
			switch (status){

				case "play":
					mediaPlayer.play();
					break;

				case "pause":
					mediaPlayer.pause();
					break;

				case "stop":
					mediaPlayer.stop();
					break;

				default:
					break;
			}
		}
	}

	public void startTimeMediaPlayer(double value){
		mediaPlayer.setStartTime(new Duration(value * 1000));
	}

	public double getRatioAudio(){
		return this.primaryStage.getWidth() / durationAudioFile;
	}

	public void done() {
		wavePane.setWaveData(resultingWaveform);
		wavePane.startPainterService();
		deleteTemporaryFiles();
	}

	private void failure() {
		deleteTemporaryFiles();
	}

	private void deleteTemporaryFiles() {
		if (temp1 != null && temp2 != null) {
			temp1.delete();
			temp2.delete();
		}
	}

	@Override
	protected Task<Boolean> createTask() {
		return new Task<>() {

			@Override
			protected Boolean call() {
				try {
					if (waveFormJob == WaveFormJob.AMPLITUDES_AND_WAVEFORM) {
						String fileFormat = "mp3";
						resultingWaveform = processFromNoWavFile(fileFormat);
					} else if (waveFormJob == WaveFormJob.WAVEFORM) {
						resultingWaveform = processAmplitudes(wavAmplitudes);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					if (ex.getMessage().contains("There is not enough space on the disk")) {
						System.err.println("Not enough disk space");
					}
					return false;
				}
				return true;
			}

			private float[] processFromNoWavFile(String fileFormat) throws IOException {
				int randomN = random.nextInt(99999);

				File temporalDecodedFile = File.createTempFile("decoded_" + randomN, ".wav");
				File temporalCopiedFile = File.createTempFile("original_" + randomN, "." + fileFormat);
				temp1 = temporalDecodedFile;
				temp2 = temporalCopiedFile;

				temporalDecodedFile.deleteOnExit();
				temporalCopiedFile.deleteOnExit();

				Files.copy(new File(fileAbsolutePath).toPath(), temporalCopiedFile.toPath(), options);

				transcodeToWav(temporalCopiedFile, temporalDecodedFile);

				if (wavAmplitudes == null) {
					wavAmplitudes = getWavAmplitudes(temporalDecodedFile);
				}

				temporalDecodedFile.delete();
				temporalCopiedFile.delete();

				return processAmplitudes(wavAmplitudes);
			}

			private void transcodeToWav(File sourceFile, File destinationFile) {
				try {

					main.getLoadingPane().updateLoading(0.2);

					AudioAttributes audio = new AudioAttributes();
					audio.setCodec("pcm_s16le");
					audio.setChannels(2);
					audio.setSamplingRate(44100);

					EncodingAttributes attributes = new EncodingAttributes();
					attributes.setOutputFormat("wav");
					attributes.setAudioAttributes(audio);

					encoder = encoder != null ? encoder : new Encoder();
					encoder.encode(new MultimediaObject(sourceFile), destinationFile, attributes, listener);

					main.getLoadingPane().updateLoading(0.4);

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			private int[] getWavAmplitudes(File file) {
				try (AudioInputStream input = AudioSystem.getAudioInputStream(file)) {

					main.getLoadingPane().updateLoading(0.6);

					AudioFormat baseFormat = input.getFormat();

					Encoding encoding = Encoding.PCM_UNSIGNED;
					float sampleRate = baseFormat.getSampleRate();
					int numChannels = baseFormat.getChannels();

					AudioFormat decodedFormat = new AudioFormat(encoding, sampleRate, 16, numChannels, numChannels * 2, sampleRate, false);
					int available = input.available();

					try (AudioInputStream pcmDecodedInput = AudioSystem.getAudioInputStream(decodedFormat, input)) {

						main.getLoadingPane().updateLoading(0.8);

						final int BUFFER_SIZE = 4096;

						byte[] buffer = new byte[BUFFER_SIZE];

						int maximumArrayLength = 100000;
						int[] finalAmplitudes = new int[maximumArrayLength];
						int samplesPerPixel = available / maximumArrayLength;

						int currentSampleCounter = 0;
						int arrayCellPosition = 0;
						float currentCellValue = 0.0f;

						int arrayCellValue;

						while (pcmDecodedInput.readNBytes(buffer, 0, BUFFER_SIZE) > 0){
							for (int i = 0; i < buffer.length - 1; i += 2) {

								arrayCellValue = (int) (((((buffer[i + 1] << 8) | buffer[i] & 0xff) << 16) / 32767) * WAVEFORM_HEIGHT_COEFFICIENT);

								if (currentSampleCounter != samplesPerPixel) {
									++currentSampleCounter;
									currentCellValue += Math.abs(arrayCellValue);
								} else {
									if (arrayCellPosition != maximumArrayLength){
										finalAmplitudes[arrayCellPosition] = finalAmplitudes[arrayCellPosition + 1] = (int) currentCellValue / samplesPerPixel;
									}

									currentSampleCounter = 0;
									currentCellValue = 0;
									arrayCellPosition += 2;
								}
							}
						}
						main.getLoadingPane().updateLoading(1.0);
						return finalAmplitudes;
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} catch (Exception ex) {
					ex.printStackTrace();

				}
				return new int[1];
			}

			private float[] processAmplitudes(int[] sourcePcmData) {
				int width = wavePane.width;
				float[] waveData = new float[width];
				int samplesPerPixel = sourcePcmData.length / width;
				float nValue;

				for (int w = 0; w < width; w++) {

					int c = w * samplesPerPixel;
					nValue = 0.0f;

					for (int s = 0; s < samplesPerPixel; s++) {
						nValue += (Math.abs(sourcePcmData[c + s]) / 65536.0f);
					}

					waveData[w] = nValue / samplesPerPixel;
				}

				return waveData;
			}
		};
	}

	public class ConvertProgressListener implements EncoderProgressListener {

		public ConvertProgressListener() {
		}

		public void message(String m) {
		}

		public void progress(int p) {
			double progress = p / 1000.00;
		}

		public void sourceInfo(MultimediaInfo m) {
		}
	}

	public String getFileAbsolutePath() {
		return fileAbsolutePath;
	}

	public float[] getResultingWaveform() {
		return resultingWaveform;
	}

}
