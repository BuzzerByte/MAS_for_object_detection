package model;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class Loader {
	// convert mat to file in term of byte array
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

	// convert file to byte array in term of byte array
	public static Mat load(String name, String path) {
		int row = ImageModel.getRow();
		int col = ImageModel.getCol();
		int type = ImageModel.getType();
		File file = new File(path, name);
		byte[] buffer = new byte[0];
		try {
			buffer = FileUtils.readFileToByteArray(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Mat mat = new Mat(row, col, type);
		mat.put(0, 0, buffer);
		return mat;
	}

	public static void save_image(Mat mat_result, String get_file_name, String get_path) {
		// TODO Auto-generated method stub
		String png_file = get_file_name.replace(".jpg(byteformat)", ".png");
		System.out.println(get_path + "\\" + png_file);
		Imgcodecs.imwrite(get_path + "\\" + png_file, mat_result);
	}
}
