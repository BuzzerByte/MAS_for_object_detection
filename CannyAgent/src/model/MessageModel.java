package model;

import java.io.Serializable;
import java.util.List;

import org.opencv.core.KeyPoint;

@SuppressWarnings("serial")
public class MessageModel implements Serializable {
	private static String chosen_msg;
	private static List<KeyPoint> KeyPointList;

	public static void setMessage(String chosen_msg) {
		MessageModel.chosen_msg = chosen_msg;
	}

	public static String getMessage() {
		return MessageModel.chosen_msg;
	}

	public static void setKeyPoint(List<KeyPoint> canny_keypoint) {
		// TODO Auto-generated method stub
		MessageModel.KeyPointList = canny_keypoint;
	}

	public static List<KeyPoint> getKeyPoint() {
		return MessageModel.KeyPointList;
	}
}
