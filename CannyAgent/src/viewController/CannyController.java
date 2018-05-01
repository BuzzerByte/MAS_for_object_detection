package viewController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.Features2d;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import jade.CannyAgent;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import model.Computation;
import model.DirectoryModel;
import model.ImageModel;
import model.ObjectModel;

public class CannyController {
	@FXML
	public ImageView ori_img;
	@FXML
	public ImageView processed_img;
	final FileChooser fileChooser = new FileChooser();

	private static CannyAgent agent;

	/*
	 * Assign the canny agent property to Canny Controller
	 */
	public void setAgent(CannyAgent agent) {
		CannyController.agent = agent;
	}

	/*
	 * Initialize controller of Canny Agent
	 */
	public void init() {
		agent.setController(this);
	}

	/*
	 * Canny Edge Detector algorithm
	 */
	public List<KeyPoint> doCanny(String path, String name) throws IOException {
		// Loading the OpenCV core library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Mat image = Imgcodecs.imread(path + "/" + name);
		Mat grayImage = new Mat();
		Mat edges = new Mat();
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
		// Detecting the edges
		Imgproc.Canny(image, edges, 60, 60 * 3);

		List<KeyPoint> KeyPointList = new ArrayList<KeyPoint>();
		Imgproc.findContours(edges, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		for (int i = 0; i < contours.size(); i++) {
			if (Imgproc.contourArea(contours.get(i)) > 10) {
				Rect rect = Imgproc.boundingRect(contours.get(i));
				if (rect.height > 10) {
					Imgproc.rectangle(image, new Point(rect.x, rect.y),
							new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255));
					KeyPointList.add(new KeyPoint((rect.x + (rect.width + rect.x)) / 2,
							(rect.y + (rect.height + rect.y)) / 2, -1));
				}
			}
		}
		return KeyPointList;
	}

	public static double matched_corners(List<KeyPoint> matchedScene, List<KeyPoint> matchedObj, String hybrid_tag) {
		// TODO Auto-generated method stub

		String bookScene = DirectoryModel.getDir() + "/" + DirectoryModel.get_file_name();
		String bookObject = ObjectModel.get_path() + "/" + ObjectModel.get_file_name();

		Mat objectImage = Imgcodecs.imread(bookObject, Imgcodecs.CV_LOAD_IMAGE_COLOR);
		Mat sceneImage = Imgcodecs.imread(bookScene, Imgcodecs.CV_LOAD_IMAGE_COLOR);

		MatOfKeyPoint objectKeyPoints = new MatOfKeyPoint();
		objectKeyPoints.fromList(matchedObj);

		Computation.setObjectCorners(objectKeyPoints.total());
		MatOfKeyPoint objectDescriptors = new MatOfKeyPoint();
		DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);

		descriptorExtractor.compute(objectImage, objectKeyPoints, objectDescriptors);

		// Create the matrix for output image.
		Mat outputImage = new Mat(objectImage.rows(), objectImage.cols(), Imgcodecs.CV_LOAD_IMAGE_COLOR);
		Scalar newKeypointColor = new Scalar(0, 0, 255);

		Features2d.drawKeypoints(objectImage, objectKeyPoints, outputImage, newKeypointColor, 0);

		// Match object image with the scene image
		MatOfKeyPoint sceneKeyPoints = new MatOfKeyPoint();
		sceneKeyPoints.fromList(matchedScene);
		MatOfKeyPoint sceneDescriptors = new MatOfKeyPoint();

		Computation.setSceneCorners(sceneKeyPoints.total());

		descriptorExtractor.compute(sceneImage, sceneKeyPoints, sceneDescriptors);

		Mat matchoutput = new Mat(sceneImage.rows() * 2, sceneImage.cols() * 2, Imgcodecs.CV_LOAD_IMAGE_COLOR);
		Scalar matchestColor = new Scalar(0, 0, 255);

		List<MatOfDMatch> matches = new LinkedList<MatOfDMatch>();
		DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

		descriptorMatcher.knnMatch(objectDescriptors, sceneDescriptors, matches, 2);

		LinkedList<DMatch> goodMatchesList = new LinkedList<DMatch>();

		// float nndrRatio = 0.7f;

		for (int i = 0; i < matches.size(); i++) {
			MatOfDMatch matofDMatch = matches.get(i);
			DMatch[] dmatcharray = matofDMatch.toArray();
			DMatch m1 = dmatcharray[0];
			goodMatchesList.addLast(m1);
		}

		Computation.setMatchedCorners(goodMatchesList.size());
		if (goodMatchesList.size() >= 7) {

			List<KeyPoint> objKeypointlist = objectKeyPoints.toList();
			List<KeyPoint> scnKeypointlist = sceneKeyPoints.toList();

			LinkedList<Point> objectPoints = new LinkedList<>();
			LinkedList<Point> scenePoints = new LinkedList<>();
			for (int i = 0; i < goodMatchesList.size(); i++) {
				objectPoints.addLast(objKeypointlist.get(goodMatchesList.get(i).queryIdx).pt);
				scenePoints.addLast(scnKeypointlist.get(goodMatchesList.get(i).trainIdx).pt);
			}

			MatOfPoint2f objMatOfPoint2f = new MatOfPoint2f();
			objMatOfPoint2f.fromList(objectPoints);
			MatOfPoint2f scnMatOfPoint2f = new MatOfPoint2f();
			scnMatOfPoint2f.fromList(scenePoints);

			Mat homography = Calib3d.findHomography(objMatOfPoint2f, scnMatOfPoint2f, Calib3d.RANSAC, 3);

			Mat obj_corners = new Mat(4, 1, CvType.CV_32FC2);
			Mat scene_corners = new Mat(4, 1, CvType.CV_32FC2);

			obj_corners.put(0, 0, new double[] { 0, 0 });
			obj_corners.put(1, 0, new double[] { objectImage.cols(), 0 });
			obj_corners.put(2, 0, new double[] { objectImage.cols(), objectImage.rows() });
			obj_corners.put(3, 0, new double[] { 0, objectImage.rows() });

			Core.perspectiveTransform(obj_corners, scene_corners, homography);

			Mat img = Imgcodecs.imread(bookScene, Imgcodecs.CV_LOAD_IMAGE_COLOR);

			Imgproc.line(img, new Point(scene_corners.get(0, 0)), new Point(scene_corners.get(1, 0)),
					new Scalar(0, 255, 0), 4);
			Imgproc.line(img, new Point(scene_corners.get(1, 0)), new Point(scene_corners.get(2, 0)),
					new Scalar(0, 255, 0), 4);
			Imgproc.line(img, new Point(scene_corners.get(2, 0)), new Point(scene_corners.get(3, 0)),
					new Scalar(0, 255, 0), 4);
			Imgproc.line(img, new Point(scene_corners.get(3, 0)), new Point(scene_corners.get(0, 0)),
					new Scalar(0, 255, 0), 4);

			MatOfDMatch goodMatches = new MatOfDMatch();
			goodMatches.fromList(goodMatchesList);

			Features2d.drawMatches(objectImage, objectKeyPoints, sceneImage, sceneKeyPoints, goodMatches, matchoutput,
					matchestColor, newKeypointColor, new MatOfByte(), 2);

			Imgcodecs.imwrite(ImageModel.get_path() + "/" + ImageModel.get_file_name() + hybrid_tag + ".jpg",
					outputImage);
		} else {

		}
		return goodMatchesList.size();
	}

	public static double computeAccuracy() {
		double scene_corners = Computation.getSceneCorners();
		double object_corners = Computation.getObjectCorners();
		double matched_corners = Computation.getMatchedCorners();
		double accuracy = 0;

		accuracy = ((matched_corners / object_corners) + (matched_corners / scene_corners)) / 2;
		accuracy = accuracy * 100;

		return accuracy;
	}
}
