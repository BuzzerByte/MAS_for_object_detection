package jade;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.opencv.core.KeyPoint;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import model.DirectoryModel;
import model.ImageModel;
import model.MessageModel;
import model.ObjectModel;
import viewController.CannyController;
import viewController.CannyGUI;

@SuppressWarnings("serial")
public class CannyAgent extends Agent {
	private CannyController controller;
	private static CannyGUI gui;

	/*
	 * Get the agent controller
	 */
	public CannyController getController() {
		return controller;
	}

	/*
	 * Set the agent controller
	 */
	public void setController(CannyController controller) {
		this.controller = controller;
	}

	/*
	 * Setup the agent property
	 */
	protected void setup() {
		gui = new CannyGUI();
		gui.setAgent(this);
		new Thread() {
			@Override
			public void run() {
				CannyGUI.main(null);
			}
		}.start();
		receiveImageInfo();
		agent_availability();
	}

	/*
	 * Agent clean up operation
	 */
	protected void takeDown() {
		gui.dispose();
		System.out.println("Canny agent " + getAID().getName() + " terminating.");
	}

	/*
	 * Check agent availability
	 */
	public void agent_availability() {
		addBehaviour(new OneShotBehaviour(this) {
			@Override
			public void action() {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(new AID("preprocessing", AID.ISLOCALNAME));
				msg.setContent("Canny agent activated");
				msg.setConversationId("availability");
				send(msg);
			}
		});
	}

	/*
	 * perform image processing task when message is received from communication
	 * agent
	 */
	public void receiveImageInfo() {
		addBehaviour(new CyclicBehaviour(this) {
			@Override
			public void action() {
				MessageTemplate mt = MessageTemplate.MatchConversationId("canny_img_info");
				ACLMessage msg = receive(mt);
				if (msg != null) {
					String img_info = msg.getContent();
					String img_info_array[] = img_info.split("-");

					if (img_info_array.length <= 4) {
						String hybrid_msg = img_info_array[img_info_array.length - 2];
						MessageModel.setMessage(hybrid_msg);
						DirectoryModel.setDir(img_info_array[img_info_array.length - 1]);
						File folder = new File(DirectoryModel.getDir());
						File[] listOfFiles = folder.listFiles(new ImageFileFilter());
						int starter;
						switch (hybrid_msg) {
						case "canny":
							for (File file : listOfFiles) {
								if (file.isFile()) {
									DirectoryModel.set_file_name(file.getName());
									List<KeyPoint> scene_key_points;
									try {
										System.out.println("Scanning: " + file.getName());
										scene_key_points = controller.doCanny(DirectoryModel.getDir() + "/",
												DirectoryModel.get_file_name());
										File scene_file = new File(DirectoryModel.getDir() + "/canny/",
												DirectoryModel.get_file_name() + "(canny).txt");
										FileUtils.writeLines(scene_file, scene_key_points);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								} else if (file.isDirectory()) {
									System.out.println("Directory " + file.getName());
								}
							}
							sendCannyProcessStatus();
							break;
						case "HC":
							starter = listOfFiles.length;
							for (int i = (listOfFiles.length / 2); i < starter; i++) {
								File file = listOfFiles[i];
								if (file.isFile()) {
									DirectoryModel.set_file_name(file.getName());
									List<KeyPoint> scene_key_points;
									try {
										System.out.println("Scanning: " + file.getName());
										scene_key_points = controller.doCanny(DirectoryModel.getDir() + "/",
												DirectoryModel.get_file_name());
										File scene_file = new File(DirectoryModel.getDir() + "/canny/",
												DirectoryModel.get_file_name() + "(canny).txt");
										FileUtils.writeLines(scene_file, scene_key_points);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

								} else if (file.isDirectory()) {
									System.out.println("Directory " + file.getName());
								}
							}
							sendCannyProcessStatus();
							break;
						case "SC":
							starter = listOfFiles.length / 2;
							for (int i = 0; i < starter; i++) {
								File file = listOfFiles[i];
								if (file.isFile()) {
									DirectoryModel.set_file_name(file.getName());
									List<KeyPoint> scene_key_points;
									try {
										System.out.println("Scanning: " + file.getName());
										scene_key_points = controller.doCanny(DirectoryModel.getDir() + "/",
												DirectoryModel.get_file_name());
										File scene_file = new File(DirectoryModel.getDir() + "/canny/",
												DirectoryModel.get_file_name() + "(canny).txt");
										FileUtils.writeLines(scene_file, scene_key_points);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

								} else if (file.isDirectory()) {
									System.out.println("Directory " + file.getName());
								}
							}
							sendCannyProcessStatus();
							break;
						case "HSC":
							starter = listOfFiles.length;
							for (int i = (listOfFiles.length / 3) + (listOfFiles.length / 3); i < starter; i++) {
								File file = listOfFiles[i];
								if (file.isFile()) {
									DirectoryModel.set_file_name(file.getName());
									List<KeyPoint> scene_key_points;
									try {
										System.out.println("Scanning: " + file.getName());
										scene_key_points = controller.doCanny(DirectoryModel.getDir() + "/",
												DirectoryModel.get_file_name());
										File scene_file = new File(DirectoryModel.getDir() + "/canny/",
												DirectoryModel.get_file_name() + "(canny).txt");
										FileUtils.writeLines(scene_file, scene_key_points);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

								} else if (file.isDirectory()) {
									System.out.println("Directory " + file.getName());
								}
							}
							sendCannyProcessStatus();
							break;
						default:
							System.out.println("System error, please restart");
						}
					} else {
						// Decompose image information
						MessageModel.setMessage(img_info_array[img_info_array.length - 6]);
						ImageModel.setRow(Integer.parseInt(img_info_array[img_info_array.length - 5]));
						ImageModel.setCol(Integer.parseInt(img_info_array[img_info_array.length - 4]));
						ImageModel.setType(Integer.parseInt(img_info_array[img_info_array.length - 3]));
						ImageModel.set_file_name(img_info_array[img_info_array.length - 1]);
						ImageModel.set_path(img_info_array[img_info_array.length - 2]);
						ObjectModel.set_file_name(img_info_array[img_info_array.length - 7]);
						ObjectModel.set_path(img_info_array[img_info_array.length - 8]);
						System.out.println(img_info);
						try {
							if (ImageModel.get_file_name().contains("PP")
									&& ObjectModel.get_file_name().contains("PP")) {
								List<KeyPoint> scene_key_points = controller.doCanny(ImageModel.get_path() + "/results",
										ImageModel.get_file_name());
								File scene_file = new File(ImageModel.get_path() + "/canny/",
										ImageModel.get_file_name() + "(canny).txt");
								FileUtils.writeLines(scene_file, scene_key_points);

								List<KeyPoint> object_key_points = controller
										.doCanny(ObjectModel.get_path() + "/results", ObjectModel.get_file_name());
								File object_file = new File(ObjectModel.get_path() + "/canny/",
										ObjectModel.get_file_name() + "(canny).txt");
								FileUtils.writeLines(object_file, object_key_points);
							} else {
								List<KeyPoint> scene_key_points = controller.doCanny(ImageModel.get_path(),
										ImageModel.get_file_name());
								File scene_file = new File(ImageModel.get_path() + "/canny/",
										ImageModel.get_file_name() + "(canny).txt");
								FileUtils.writeLines(scene_file, scene_key_points);

								List<KeyPoint> object_key_points = controller.doCanny(ObjectModel.get_path(),
										ObjectModel.get_file_name());
								File object_file = new File(ObjectModel.get_path() + "/canny/",
										ObjectModel.get_file_name() + "(canny).txt");
								FileUtils.writeLines(object_file, object_key_points);
							}
							sendCannyProcessStatus();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					System.out.println("Image processing done");
				} else {
					block();
				}
			}
		});
	}

	/*
	 * return process status to communication agent
	 */
	public void sendCannyProcessStatus() {
		addBehaviour(new OneShotBehaviour(this) {
			@Override
			public void action() {
				// TODO Auto-generated method stub
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(new AID("CommunicationAgent", AID.ISLOCALNAME));
				msg.setContent("success-" + MessageModel.getMessage());
				msg.setConversationId("processed_status");
				send(msg);
				System.out.println("Image information send to Communication Agent\n");
			}
		});
	}

	/**
	 * A class that implements the Java FileFilter interface.
	 */
	public class ImageFileFilter implements FileFilter {
		private final String[] okFileExtensions = new String[] { "jpg", "png", "tif" };

		public boolean accept(File file) {
			for (String extension : okFileExtensions) {
				if (file.getName().toLowerCase().endsWith(extension)) {
					return true;
				}
			}
			return false;
		}
	}
}
