package model;

public class ImageModel {
	private static String parent_path;
	private static String file_name;
	private static int row;
	private static int col;
	private static int type;

	public static String get_path() {
		return parent_path;
	}

	public static void set_path(String parent_path) {
		ImageModel.parent_path = parent_path;
	}

	public static String get_file_name() {
		return file_name;
	}

	public static void set_file_name(String file_name) {
		ImageModel.file_name = file_name;
	}

	public static int getRow() {
		return row;
	}

	public static void setRow(int row) {
		ImageModel.row = row;
	}

	public static int getCol() {
		return col;
	}

	public static void setCol(int col) {
		ImageModel.col = col;
	}

	public static int getType() {
		return type;
	}

	public static void setType(int type) {
		ImageModel.type = type;
	}
}
