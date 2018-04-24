package model;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.opencv.core.Mat;

public class Loader {
	public static Mat load(String name, String path) {
		File file = new File(path, name);
		byte[] buffer = new byte[0];
		try {
			buffer = FileUtils.readFileToByteArray(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Mat mat = new Mat(ImageModel.getRow(), ImageModel.getCol(), ImageModel.getType());
		mat.put(0, 0, buffer);
		return mat;
	}

	public static void save(Mat mat, String name, String path) {
		File file = new File(path, name);
		int length = (int) (mat.total() * mat.elemSize());
		ImageModel.setRow(mat.rows());
		ImageModel.setCol(mat.cols());
		ImageModel.setType(mat.type());
		ImageModel.set_file_name(name);
		ImageModel.set_path(path);

		byte buffer[] = new byte[length];
		mat.get(0, 0, buffer);
		try {
			FileUtils.writeByteArrayToFile(file, buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
