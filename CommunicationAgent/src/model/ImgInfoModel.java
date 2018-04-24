package model;

public class ImgInfoModel {
	private static String img_info;
	private static String path;

	public static void setImgInfo(String info) {
		ImgInfoModel.img_info = info;
	}

	public static String getImgInfo() {
		return ImgInfoModel.img_info;
	}

	public static void setPath(String path) {
		ImgInfoModel.path = path;
	}

	public static String getPath() {
		return ImgInfoModel.path;
	}
}
