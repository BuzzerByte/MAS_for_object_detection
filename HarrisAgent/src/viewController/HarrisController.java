package viewController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.Features2d;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import jade.HarrisAgent;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import model.Computation;
import model.DirectoryModel;
import model.ImageModel;
import model.ObjectModel;

public class HarrisController {
	@FXML
	public ImageView ori_img;
	@FXML
	public ImageView processed_img;
	final FileChooser fileChooser = new FileChooser();

	private static HarrisAgent agent;

	/*
	 * Assign the Harris agent property to Harris Controller
	 */
	public void setAgent(HarrisAgent agent) {
		HarrisController.agent = agent;
	}

	/*
	 * Initialize controller of Harris Agent
	 */
	public void init() {
		agent.setController(this);
	}

	/*
	 * Harris Corner Detector algorithm
	 */
	public List<KeyPoint> doHarris(String path, String name) throws IOException {
		Mat image = Imgcodecs.imread(path + "/" + name);
		MatOfKeyPoint dst = new MatOfKeyPoint(), dst_norm = new MatOfKeyPoint(), dst_norm_scaled = new MatOfKeyPoint();
		Mat grayImage = new Mat();
		// Detector parameters
		int blockSize = 9;
		int apertureSize = 5;
		double k = 0.1;

		// convert to grayscale

		Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
		// Detecting corners using Harris Corner Detector
		Imgproc.cornerHarris(grayImage, dst, blockSize, apertureSize, k);

		// Normalizing
		Core.normalize(dst, dst_norm, 0, 255, Core.NORM_MINMAX, CvType.CV_32FC1, new Mat());
		Core.convertScaleAbs(dst_norm, dst_norm_scaled);

		List<KeyPoint> KeyPointList = new ArrayList<KeyPoint>();
		// map points to every corner detected
		for (int j = 0; j < dst_norm_scaled.rows(); j++) {
			for (int i = 0; i < dst_norm_scaled.cols(); i++) {
				if ((int) dst_norm_scaled.get(j, i)[0] > 200) {
					KeyPointList.add(new KeyPoint(i, j, -1));
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

			try {
				Core.perspectiveTransform(obj_corners, scene_corners, homography);
			} catch (CvException e) {
				System.out.println("Ignore and continue");
			}

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
