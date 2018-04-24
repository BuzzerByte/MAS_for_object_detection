package model;

public class AgentModel {
	private static Boolean canny_selected = false;
	private static Boolean harris_selected = false;
	private static Boolean shi_tomasi_selected = false;
	private static Boolean pp_selected = false;
	private static Boolean sys_recomm_selected = false;
	private static Boolean all_selected = false;

	public static void setCanny(Boolean canny_selected) {
		AgentModel.canny_selected = canny_selected;
	}

	public static Boolean getCanny() {
		return AgentModel.canny_selected;
	}

	public static void setHarris(Boolean harris_selected) {
		AgentModel.harris_selected = harris_selected;
	}

	public static Boolean getHarris() {
		return AgentModel.harris_selected;
	}

	public static void setShiTomasi(Boolean shi_tomasi_selected) {
		AgentModel.shi_tomasi_selected = shi_tomasi_selected;
	}

	public static Boolean getShiTomasi() {
		return AgentModel.shi_tomasi_selected;
	}

	public static void setPP(Boolean pp_selected) {
		AgentModel.pp_selected = pp_selected;
	}

	public static Boolean getPP() {
		return AgentModel.pp_selected;
	}

	public static void setSysRecomm(Boolean sys_recomm_selected) {
		AgentModel.sys_recomm_selected = sys_recomm_selected;
	}

	public static Boolean getSysRecomm() {
		return AgentModel.sys_recomm_selected;
	}

	public static void setAll(Boolean all_selected) {
		AgentModel.canny_selected = all_selected;
	}

	public static Boolean getAll() {
		return AgentModel.all_selected;
	}
}
