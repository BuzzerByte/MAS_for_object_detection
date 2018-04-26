package model;

import org.opencv.core.Mat;

public class ObjectModel {
	private static String path;
	private static String file_name;
	private static Mat ori_img;

	public static Mat get_mat_img() {
		return ObjectModel.ori_img;
	}

	public static void set_mat_img(Mat ori_img) {
		ObjectModel.ori_img = ori_img;
	}

	public static String get_path() {
		return path;
	}

	public static void set_path(String path) {
		ObjectModel.path = path;
	}

	public static String get_file_name() {
		return file_name;
	}

	public static void set_file_name(String file_name) {
		ObjectModel.file_name = file_name;
	}

}
