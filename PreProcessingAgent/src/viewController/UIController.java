package viewController;

import java.io.File;
import java.util.regex.Pattern;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import jade.PreProcessingAgent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import model.AgentModel;
import model.DirectoryModel;
import model.ImageModel;
import model.Loader;
import model.MessageModel;
import model.ObjectModel;

public class UIController {
	@FXML
	private ImageView smooth_img;
	@FXML
	private ImageView contrast_img;
	@FXML
	private ImageView ori_img;
	@FXML
	private ImageView dn_sampling_img;
	@FXML
	public ImageView morpho_img;
	@FXML
	private Button choose_img;
	@FXML
	private Button choose_obj;
	@FXML
	private TextField img_path;
	@FXML
	public TextField status;
	@FXML
	public CheckBox recognized_obj_check;
	@FXML
	public CheckBox identify_obj_check;
	@FXML
	public Button process_img;
	@FXML
	public TextField txt_accuracy;
	@FXML
	public ImageView view_selected_obj;
	@FXML
	public HBox hbox_obj;
	@FXML
	public HBox hbox_ori_img;
	@FXML
	public HBox hbox_result;
	@FXML
	public TextField obj_path;
	@FXML
	public TextField txt_time;
	@FXML
	public Label accuracy_label;
	@FXML
	public CheckBox dataset;

	final FileChooser fileChooser = new FileChooser();
	static double alpha = 1;
	static double beta = 50;
	private static PreProcessingAgent agent;
	private static String state = "";
	private static String file_path;
	private static String file_name;
	private static String accuracy;
	private static String color;
	public static boolean selectDataset;

	/*
	 * Set the agent for user interface controller
	 */
	public void setAgent(PreProcessingAgent agent) {
		UIController.agent = agent;
	}

	/*
	 * Initialize controller for pre-processing agent
	 */
	public void init() {
		agent.setController(this);
		String style_outter = "-fx-border-color: black;" + "-fx-border-width: 2;";
		hbox_obj.setStyle(style_outter);
		hbox_ori_img.setStyle(style_outter);
		hbox_result.setStyle(style_outter);
	}

	/*
	 * Button function used to trigger image processing
	 */
	@FXML
	protected void trigger_process_img() {
		if (MessageModel.getMessage().equals("none")) {
			Alert alert = new Alert(AlertType.INFORMATION, "Please select at least one detection agent");
			alert.show();
		} else {
			if (AgentModel.getPP()) {
				Mat blur_img = smoothing(ImageModel.get_mat_img());
				Mat contrast_enhance_img = contrast_enhancement(blur_img);
				Mat down_sampling_img = down_sampling(contrast_enhance_img);
				Mat morphological_img = morphological_operation(down_sampling_img);
				Loader.save(morphological_img, getFileName(), getFilePath());
				if (ImageModel.get_file_name().contains("PP")) {
					Imgcodecs.imwrite(ImageModel.get_path() + "/results/" + ImageModel.get_file_name(),
							morphological_img);
					Loader.save(morphological_img, ImageModel.get_file_name(), ImageModel.get_path());
				} else {
					Imgcodecs.imwrite(ImageModel.get_path() + "/results/PP" + ImageModel.get_file_name(),
							morphological_img);
					Loader.save(morphological_img, "PP" + ImageModel.get_file_name(), ImageModel.get_path());
				}

				Mat obj_blur_img = smoothing(ObjectModel.get_mat_img());
				Mat obj_contrast_enhance_img = contrast_enhancement(obj_blur_img);
				Mat obj_down_sampling_img = down_sampling(obj_contrast_enhance_img);
				Mat obj_morphological_img = morphological_operation(obj_down_sampling_img);

				if (ObjectModel.get_file_name().contains("PP")) {
					Imgcodecs.imwrite(ObjectModel.get_path() + "/results/" + ObjectModel.get_file_name(),
							obj_morphological_img);
					ObjectModel.set_file_name(ObjectModel.get_file_name());
				} else {
					Imgcodecs.imwrite(ObjectModel.get_path() + "/results/PP" + ObjectModel.get_file_name(),
							obj_morphological_img);
					ObjectModel.set_file_name("PP" + ObjectModel.get_file_name());
				}

				agent.sendImageInfo();
			} else if (dataset.isSelected()) {
				agent.sendImageInfo();
			} else {
				ObjectModel.set_file_name(ObjectModel.get_file_name().replace("PP", ""));
				Loader.save(ImageModel.get_mat_img(), getFileName(), getFilePath());
				agent.sendImageInfo();
			}

		}
	}

	@FXML
	protected void select_dataset() {
		if (dataset.isSelected()) {
			choose_img.setText("Select Dataset");
			selectDataset = true;
			accuracy_label.setText("Object Detected");
		} else {
			choose_img.setText("Choose Scene Image");
			selectDataset = false;
			accuracy_label.setText("Accuracy");
		}
	}

	/*
	 * Browse image from local storage
	 */
	@FXML
	protected void browse_img() {
		if (choose_img.getText().equals("Select Dataset")) {
			DirectoryChooser chooser = new DirectoryChooser();
			chooser.setTitle("Select Dataset");
			File selectedDirectory = chooser.showDialog(null);
			String rel_path = selectedDirectory.toString().replace("C:\\", "/");
			rel_path = rel_path.replaceAll(Pattern.quote("\\"), "/");
			img_path.setText(rel_path);
			DirectoryModel.setDir(rel_path);

		} else if (choose_img.getText().equals("Choose Scene Image")) {
			File file1 = fileChooser.showOpenDialog(null);
			if (file1 != null) {
				String rel_path = file1.getParent().replace("C:\\", "/");
				rel_path = rel_path.replaceAll(Pattern.quote("\\"), "/");
				img_path.setText(rel_path + "/" + file1.getName());
				setFileName(file1.getName());
				setFilePath(rel_path);
				Mat oriImage = Imgcodecs.imread(rel_path + "/" + file1.getName());
				Image showOriImg = Utils.mat2Image(oriImage);
				updateImageView(ori_img, showOriImg);
				ImageModel.set_mat_img(oriImage);
			} else {
				System.out.println("Image couldn't be loaded");
			}
		}
	}

	@FXML
	protected void browse_obj() {
		File file1 = fileChooser.showOpenDialog(null);
		if (file1 != null) {
			String rel_path = file1.getParent().replace("C:\\", "/");
			rel_path = rel_path.replaceAll(Pattern.quote("\\"), "/");
			System.out.println(rel_path);
			obj_path.setText(rel_path + "/" + file1.getName());
			ObjectModel.set_path(rel_path);
			ObjectModel.set_file_name(file1.getName());
			Mat obj = Imgcodecs.imread(rel_path + "/" + file1.getName());

			ObjectModel.set_mat_img(obj);
			Image selected_obj = Utils.mat2Image(obj);
			updateImageView(view_selected_obj, selected_obj);
		} else {
			System.out.println("Image couldn't be loaded");
		}
	}

	/*
	 * Pre-processing smoothing function
	 */
	private Mat smoothing(Mat image) {
		Mat blurImage = new Mat();
		Imgproc.blur(image, blurImage, new Size(3, 3));
		return blurImage;
	}

	/*
	 * Pre-processing contrast enhancement function
	 */
	private Mat contrast_enhancement(Mat image) {
		Mat dest = new Mat(image.rows(), image.cols(), image.type());
		// applying brightness enhancement
		image.convertTo(dest, -1, alpha, beta);
		return dest;
	}

	/*
	 * Pre-processing down_sampling function
	 */
	private Mat down_sampling(Mat image) {
		Mat dest = new Mat();
		Imgproc.pyrDown(image, dest, new Size(image.cols() / 2, image.rows() / 2));
		return image;
	}

	/*
	 * Pre-processing morphological function
	 */
	private Mat morphological_operation(Mat image) {
		Mat morphOutput = new Mat();
		int dilation_size = 5;
		Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
				new Size(1 * dilation_size + 1, 1 * dilation_size + 1));
		Imgproc.dilate(image, morphOutput, dilateElement);
		return morphOutput;
	}

	public static void updateImageView(ImageView view, Image image) {
		Utils.onFXThread(view.imageProperty(), image);
	}

	/*
	 * Set the accuracy of object detected
	 */
	public static void setAccuracy(String accuracy) {
		UIController.accuracy = accuracy;
	}

	/*
	 * Get the accuracy of object detected
	 */
	public static String getAccuracy() {
		return UIController.accuracy;
	}

	/*
	 * Set the color of accuracy value
	 */
	public static void setColor(String color) {
		UIController.color = color;
	}

	/*
	 * Get the color of accuracy value
	 */
	public static String getColor() {
		return UIController.color;
	}

	/*
	 * Set the state of current processing image
	 */
	public static void setState(String state) {
		UIController.state = state;
	}

	/*
	 * Get the state of current processing image
	 */
	public static String getState() {
		return state;
	}

	/*
	 * Set the file path for current image file
	 */
	public static void setFilePath(String file_path) {
		UIController.file_path = file_path;
	}

	/*
	 * Get the file path for current image file
	 */
	public static String getFilePath() {
		return file_path;
	}

	/*
	 * Set the file name for current image file
	 */
	public static void setFileName(String file_name) {
		UIController.file_name = file_name;
	}

	/*
	 * Get the file name for current image file
	 */
	public static String getFileName() {
		return file_name;
	}

}
