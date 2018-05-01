package model;

public class DirectoryModel {
	private static String dir;
	private static String name;

	public static void setDir(String dir) {
		DirectoryModel.dir = dir;
	}

	public static String getDir() {
		return DirectoryModel.dir;
	}

	public static void set_file_name(String name) {
		// TODO Auto-generated method stub
		DirectoryModel.name = name;
	}

	public static String get_file_name() {
		return DirectoryModel.name;
	}
}
