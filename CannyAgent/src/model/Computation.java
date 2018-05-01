package model;

public class Computation {
	private static double t_scene_corners;
	private static double t_object_corners;
	private static double t_matched_corners;
	private static double th_scene_corners;
	private static double th_object_corners;

	public static void setSceneCorners(double t_scene_corners) {
		Computation.t_scene_corners = t_scene_corners;
	}

	public static double getSceneCorners() {
		return Computation.t_scene_corners;
	}

	public static void setObjectCorners(double t_object_corners) {
		Computation.t_object_corners = t_object_corners;
	}

	public static double getObjectCorners() {
		return Computation.t_object_corners;
	}

	public static void setHybridObjectCorners(double th_object_corners) {
		Computation.th_object_corners = th_object_corners;
	}

	public static double getHybridObjectCorners() {
		return Computation.th_object_corners;
	}

	public static void setHybridSceneCorners(double th_scene_corners) {
		Computation.th_scene_corners = th_scene_corners;
	}

	public static double getHybridSceneCorners() {
		return Computation.th_scene_corners;
	}

	public static void setMatchedCorners(double t_matched_corners) {
		Computation.t_matched_corners = t_matched_corners;
	}

	public static double getMatchedCorners() {
		return Computation.t_matched_corners;
	}

}
