package model;

public class MessageModel {
	private static String chosen_msg;

	public static void setMessage(String chosen_msg) {
		MessageModel.chosen_msg = chosen_msg;
	}

	public static String getMessage() {
		return MessageModel.chosen_msg;
	}
}
