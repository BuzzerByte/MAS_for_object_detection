package model;

public class DirectoryModel {
	protected static String name;
	protected static String dir;

	public static void setDir(String dir) {
		DirectoryModel.dir = dir;
	}

	public static String getDir() {
		return DirectoryModel.dir;
	}

	public static void set_file_name(String name) {
		DirectoryModel.name = name;
	}

	public static String get_file_name() {
		return DirectoryModel.name;
	}
}
