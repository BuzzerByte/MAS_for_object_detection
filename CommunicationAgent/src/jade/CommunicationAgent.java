package jade;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.opencv.core.KeyPoint;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import model.DirectoryModel;
import model.ImageModel;
import model.ImgInfoModel;
import model.ObjectModel;
import viewController.CommunicationController;
import viewController.CommunicationGUI;

@SuppressWarnings("serial")
public class CommunicationAgent extends Agent {
	private CommunicationController controller;
	private static CommunicationGUI gui;
	String percentages[] = new String[7];
	String tags[] = { "canny", "shi_tomasi", "harris", "HC", "HS", "SC", "HSC" };
	public double accuracy[] = new double[7];
	int i = 0;
	protected String mode = "";
	protected String img_info;
	protected boolean alreadyExecuted = false;
	protected boolean doneDetection = false;
	protected int count = 0, cannyCount = 0, harrisCount = 0, shitomasiCount = 0;

	/*
	 * Get the agent controller
	 */
	public CommunicationController getController() {
		return controller;
	}

	/*
	 * Set the agent controller
	 */
	public void setController(CommunicationController controller) {
		this.controller = controller;
	}

	/*
	 * Setup the agent property
	 */
	protected void setup() {
		gui = new CommunicationGUI();
		gui.setAgent(this);
		new Thread() {
			@Override
			public void run() {
				CommunicationGUI.main(null);
			}
		}.start();
		receiveImageInfo();
		receiveProcessedImage();
		agent_availability();
	}

	/*
	 * Agent clean up operation
	 */
	protected void takeDown() {
		gui.dispose();
		System.out.println("Pre-Processing agent " + getAID().getName() + " terminating.");
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
				msg.setContent("Communication agent activated");
				msg.setConversationId("availability");
				send(msg);
			}
		});
	}

	/*
	 * perform image processing task when message is received from pre-processing
	 * agent
	 */
	public void receiveImageInfo() {
		addBehaviour(new CyclicBehaviour(this) {
			@Override
			public void action() {
				MessageTemplate mt = MessageTemplate.MatchConversationId("img_info");
				ACLMessage msg = receive(mt);
				if (msg != null) {
					alreadyExecuted = false;
					String img_info = msg.getContent();
					System.out.println(img_info);
					ImgInfoModel.setImgInfo(img_info);
					String img_info_array[] = img_info.split("-");

					if (img_info_array.length <= 4) {
						mode = "dataset";
						String selected_option = img_info_array[img_info_array.length - 2];
						ObjectModel.set_path(img_info_array[img_info_array.length - 4]);
						ObjectModel.set_file_name(img_info_array[img_info_array.length - 3]);
						DirectoryModel.setDir(img_info_array[img_info_array.length - 1]);
						selection(selected_option, img_info);
					} else {
						mode = "normal";
						String selected_option = img_info_array[img_info_array.length - 6];
						ImageModel.setRow(Integer.parseInt(img_info_array[img_info_array.length - 5]));
						ImageModel.setCol(Integer.parseInt(img_info_array[img_info_array.length - 4]));
						ImageModel.setType(Integer.parseInt(img_info_array[img_info_array.length - 3]));
						ImageModel.set_file_name(img_info_array[img_info_array.length - 1]);
						ImageModel.set_path(img_info_array[img_info_array.length - 2]);
						ObjectModel.set_path(img_info_array[img_info_array.length - 8]);
						ObjectModel.set_file_name(img_info_array[img_info_array.length - 7]);
						selection(selected_option, img_info);
					}
				} else {
					block();
				}
			}
		});
	}

	public void selection(String selected_option, String img_info) {
		System.out.println(selected_option);
		// extract content of image information and perform specific conversation
		switch (selected_option) {
		case "recomm":
			mode = "sys_recomm";
			sendInfoToHarris(img_info);
			sendInfoToShiTomasi(img_info);
			sendInfoToCanny(img_info);
			break;
		case "canny":
			sendInfoToCanny(img_info);
			break;
		case "harris":
			sendInfoToHarris(img_info);
			break;
		case "shi_tomasi":
			sendInfoToShiTomasi(img_info);
			break;
		case "HC":
			final CyclicBarrier gate = new CyclicBarrier(3);

			Thread t1 = new Thread() {
				public void run() {
					try {
						gate.await();
						sendInfoToHarris(img_info);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (BrokenBarrierException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// do stuff
				}
			};
			Thread t2 = new Thread() {
				public void run() {
					try {
						gate.await();
						sendInfoToCanny(img_info);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (BrokenBarrierException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// do stuff
				}
			};

			t1.start();
			t2.start();
			try {
				gate.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BrokenBarrierException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("all threads started");
			break;
		case "HS":
			sendInfoToHarris(img_info);
			sendInfoToShiTomasi(img_info);
			break;
		case "SC":
			sendInfoToShiTomasi(img_info);
			sendInfoToCanny(img_info);
			break;
		case "HSC":
			sendInfoToHarris(img_info);
			sendInfoToShiTomasi(img_info);
			sendInfoToCanny(img_info);
			break;
		default:
			System.out.println(selected_option);
			System.out.println("nothing is selected");
		}
		System.out.println("Image information received at communication agent");
	}

	/*
	 * Receive result from recognition agents
	 */
	private void receiveProcessedImage() {
		// TODO Auto-generated method stub
		addBehaviour(new CyclicBehaviour(this) {
			@Override
			public void action() {
				MessageTemplate mt = MessageTemplate.MatchConversationId("processed_status");
				ACLMessage msg = receive(mt);
				if (msg != null) {

					String msg_info = msg.getContent();
					String msg_info_array[] = msg_info.split("-");
					String msg_hybrid = msg_info_array[msg_info_array.length - 1];
					String img_info = ImgInfoModel.getImgInfo();
					String img_info_array[] = img_info.split("-");
					switch (mode) {
					case "sys_recomm":
						if (!alreadyExecuted) {
							alreadyExecuted = true;
							CommunicationController.setHarrisScenePt(CommunicationController.keypoint_extraction(
									ImageModel.get_path() + "/harris/" + ImageModel.get_file_name(), "harris"));
							CommunicationController.setHarrisObjPt(CommunicationController.keypoint_extraction(
									ObjectModel.get_path() + "/harris/" + ObjectModel.get_file_name(), "harris"));
							CommunicationController.setShiTomasiScenePt(CommunicationController.keypoint_extraction(
									ImageModel.get_path() + "/shitomasi/" + ImageModel.get_file_name(), "shi_tomasi"));
							CommunicationController.setShiTomasiObjPt(CommunicationController.keypoint_extraction(
									ObjectModel.get_path() + "/shitomasi/" + ObjectModel.get_file_name(),
									"shi_tomasi"));
							CommunicationController.setCannyScenePt(CommunicationController.keypoint_extraction(
									ImageModel.get_path() + "/canny/" + ImageModel.get_file_name(), "canny"));
							CommunicationController.setCannyObjPt(CommunicationController.keypoint_extraction(
									ObjectModel.get_path() + "/canny/" + ObjectModel.get_file_name(), "canny"));

							List<KeyPoint> HCmergeObjPts = new ArrayList<KeyPoint>();
							HCmergeObjPts.addAll(CommunicationController.getHarrisObjPt());
							HCmergeObjPts.addAll(CommunicationController.getCannyObjPt());

							List<KeyPoint> HCmergeScenePts = new ArrayList<KeyPoint>();
							HCmergeScenePts.addAll(CommunicationController.getHarrisScenePt());
							HCmergeScenePts.addAll(CommunicationController.getCannyScenePt());

							List<KeyPoint> HSmergeObjPts = new ArrayList<KeyPoint>();
							HSmergeObjPts.addAll(CommunicationController.getHarrisScenePt());
							HSmergeObjPts.addAll(CommunicationController.getShiTomasiScenePt());

							List<KeyPoint> HSmergeScenePts = new ArrayList<KeyPoint>();
							HSmergeScenePts.addAll(CommunicationController.getHarrisScenePt());
							HSmergeScenePts.addAll(CommunicationController.getShiTomasiScenePt());

							List<KeyPoint> SCmergeObjPts = new ArrayList<KeyPoint>();
							SCmergeObjPts.addAll(CommunicationController.getShiTomasiScenePt());
							SCmergeObjPts.addAll(CommunicationController.getCannyScenePt());

							List<KeyPoint> SCmergeScenePts = new ArrayList<KeyPoint>();
							SCmergeScenePts.addAll(CommunicationController.getShiTomasiScenePt());
							SCmergeScenePts.addAll(CommunicationController.getCannyScenePt());

							List<KeyPoint> HSCmergeObjPts = new ArrayList<KeyPoint>();
							HSCmergeObjPts.addAll(CommunicationController.getHarrisScenePt());
							HSCmergeObjPts.addAll(CommunicationController.getShiTomasiScenePt());
							HSCmergeObjPts.addAll(CommunicationController.getCannyScenePt());

							List<KeyPoint> HSCmergeScenePts = new ArrayList<KeyPoint>();
							HSCmergeScenePts.addAll(CommunicationController.getHarrisScenePt());
							HSCmergeScenePts.addAll(CommunicationController.getShiTomasiScenePt());
							HSCmergeScenePts.addAll(CommunicationController.getCannyScenePt());

							String temp;
							CommunicationController.matched_corners(CommunicationController.getCannyScenePt(),
									CommunicationController.getCannyObjPt(), msg_hybrid);
							temp = Double.toString(CommunicationController.computeAccuracy());
							percentages[0] = temp;
							System.out.println("canny: " + percentages[0]);

							CommunicationController.matched_corners(CommunicationController.getHarrisScenePt(),
									CommunicationController.getHarrisObjPt(), msg_hybrid);
							temp = Double.toString(CommunicationController.computeAccuracy());
							percentages[2] = temp;
							System.out.println("Harris: " + percentages[2]);

							CommunicationController.matched_corners(CommunicationController.getShiTomasiScenePt(),
									CommunicationController.getShiTomasiObjPt(), msg_hybrid);
							temp = Double.toString(CommunicationController.computeAccuracy());
							percentages[1] = temp;
							System.out.println("Shi-Tomasi: " + percentages[1]);

							CommunicationController.matched_corners(HCmergeScenePts, HCmergeObjPts, msg_hybrid);
							temp = Double.toString(CommunicationController.computeAccuracy());
							percentages[3] = temp;
							System.out.println("HC: " + percentages[3]);

							CommunicationController.matched_corners(HSmergeScenePts, HSmergeObjPts, msg_hybrid);
							temp = Double.toString(CommunicationController.computeAccuracy());
							percentages[4] = temp;
							System.out.println("HS: " + percentages[4]);

							CommunicationController.matched_corners(SCmergeScenePts, SCmergeObjPts, msg_hybrid);
							temp = Double.toString(CommunicationController.computeAccuracy());
							percentages[5] = temp;
							System.out.println("SC: " + percentages[5]);

							CommunicationController.matched_corners(HSCmergeScenePts, HSCmergeObjPts, msg_hybrid);
							temp = Double.toString(CommunicationController.computeAccuracy());
							percentages[6] = temp;
							System.out.println("HSC: " + percentages[6]);

							int highest = CommunicationController.find_path(percentages);
							String new_info = img_info.replace(img_info_array[img_info_array.length - 6],
									tags[highest]);
							selection(tags[highest], new_info);
							mode = "normal";
						}
						break;
					case "normal":
						String temp;
						switch (msg_hybrid) {
						case "canny":
							CommunicationController.setCannyScenePt(CommunicationController.keypoint_extraction(
									ImageModel.get_path() + "/canny/" + ImageModel.get_file_name(), "canny"));
							CommunicationController.setCannyObjPt(CommunicationController.keypoint_extraction(
									ObjectModel.get_path() + "/canny/" + ObjectModel.get_file_name(), "canny"));

							CommunicationController.matched_corners(CommunicationController.getCannyScenePt(),
									CommunicationController.getCannyObjPt(), msg_hybrid);
							temp = Double.toString(CommunicationController.computeAccuracy());
							percentages[0] = temp;
							System.out.println("canny: " + percentages[0]);
							sendProcessedImage(temp + "-canny");
							break;
						case "shi_tomasi":
							CommunicationController.setShiTomasiScenePt(CommunicationController.keypoint_extraction(
									ImageModel.get_path() + "/shitomasi/" + ImageModel.get_file_name(), "shi_tomasi"));
							CommunicationController.setShiTomasiObjPt(CommunicationController.keypoint_extraction(
									ObjectModel.get_path() + "/shitomasi/" + ObjectModel.get_file_name(),
									"shi_tomasi"));

							CommunicationController.matched_corners(CommunicationController.getShiTomasiScenePt(),
									CommunicationController.getShiTomasiObjPt(), msg_hybrid);
							temp = Double.toString(CommunicationController.computeAccuracy());
							percentages[1] = temp;
							System.out.println("Shi-Tomasi: " + percentages[1]);
							sendProcessedImage(temp + "-shi_tomasi");
							break;
						case "harris":
							CommunicationController.setHarrisScenePt(CommunicationController.keypoint_extraction(
									ImageModel.get_path() + "/harris/" + ImageModel.get_file_name(), "harris"));
							CommunicationController.setHarrisObjPt(CommunicationController.keypoint_extraction(
									ObjectModel.get_path() + "/harris/" + ObjectModel.get_file_name(), "harris"));

							CommunicationController.matched_corners(CommunicationController.getHarrisScenePt(),
									CommunicationController.getHarrisObjPt(), msg_hybrid);
							temp = Double.toString(CommunicationController.computeAccuracy());
							percentages[2] = temp;
							System.out.println("Harris: " + percentages[2]);
							sendProcessedImage(temp + "-harris");
							break;
						case "HC":
							if (!alreadyExecuted) {
								alreadyExecuted = true;
								CommunicationController.setHarrisScenePt(CommunicationController.keypoint_extraction(
										ImageModel.get_path() + "/harris/" + ImageModel.get_file_name(), "harris"));
								CommunicationController.setHarrisObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/harris/" + ObjectModel.get_file_name(), "harris"));

								CommunicationController.setCannyScenePt(CommunicationController.keypoint_extraction(
										ImageModel.get_path() + "/canny/" + ImageModel.get_file_name(), "canny"));
								CommunicationController.setCannyObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/canny/" + ObjectModel.get_file_name(), "canny"));

								List<KeyPoint> mergeObjPts = new ArrayList<KeyPoint>();
								mergeObjPts.addAll(CommunicationController.getHarrisObjPt());
								mergeObjPts.addAll(CommunicationController.getCannyObjPt());

								List<KeyPoint> mergeScenePts = new ArrayList<KeyPoint>();
								mergeScenePts.addAll(CommunicationController.getHarrisScenePt());
								mergeScenePts.addAll(CommunicationController.getCannyScenePt());

								CommunicationController.matched_corners(mergeScenePts, mergeObjPts, msg_hybrid);
								temp = Double.toString(CommunicationController.computeAccuracy());
								percentages[3] = temp;
								System.out.println("HC: " + percentages[3]);
								sendProcessedImage(temp + "-HC");
							}
							break;
						case "HS":
							if (!alreadyExecuted) {
								alreadyExecuted = true;
								CommunicationController.setHarrisScenePt(CommunicationController.keypoint_extraction(
										ImageModel.get_path() + "/harris/" + ImageModel.get_file_name(), "harris"));
								CommunicationController.setHarrisObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/harris/" + ObjectModel.get_file_name(), "harris"));

								CommunicationController.setShiTomasiScenePt(CommunicationController.keypoint_extraction(
										ImageModel.get_path() + "/shitomasi/" + ImageModel.get_file_name(),
										"shi_tomasi"));
								CommunicationController.setShiTomasiObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/shitomasi/" + ObjectModel.get_file_name(),
										"shi_tomasi"));

								List<KeyPoint> mergeObjPts = new ArrayList<KeyPoint>();
								mergeObjPts.addAll(CommunicationController.getHarrisObjPt());
								mergeObjPts.addAll(CommunicationController.getShiTomasiObjPt());

								List<KeyPoint> mergeScenePts = new ArrayList<KeyPoint>();
								mergeScenePts.addAll(CommunicationController.getHarrisScenePt());
								mergeScenePts.addAll(CommunicationController.getShiTomasiScenePt());

								CommunicationController.matched_corners(mergeScenePts, mergeObjPts, msg_hybrid);
								temp = Double.toString(CommunicationController.computeAccuracy());
								percentages[4] = temp;
								System.out.println("HS: " + percentages[4]);
								sendProcessedImage(temp + "-HS");
							}
							break;
						case "SC":
							if (!alreadyExecuted) {
								alreadyExecuted = true;
								CommunicationController.setShiTomasiScenePt(CommunicationController.keypoint_extraction(
										ImageModel.get_path() + "/shitomasi/" + ImageModel.get_file_name(),
										"shi_tomasi"));
								CommunicationController.setShiTomasiObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/shitomasi/" + ObjectModel.get_file_name(),
										"shi_tomasi"));

								CommunicationController.setCannyScenePt(CommunicationController.keypoint_extraction(
										ImageModel.get_path() + "/canny/" + ImageModel.get_file_name(), "canny"));
								CommunicationController.setCannyObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/canny/" + ObjectModel.get_file_name(), "canny"));

								List<KeyPoint> mergeObjPts = new ArrayList<KeyPoint>();
								mergeObjPts.addAll(CommunicationController.getCannyObjPt());
								mergeObjPts.addAll(CommunicationController.getShiTomasiObjPt());

								List<KeyPoint> mergeScenePts = new ArrayList<KeyPoint>();
								mergeScenePts.addAll(CommunicationController.getCannyScenePt());
								mergeScenePts.addAll(CommunicationController.getShiTomasiScenePt());

								CommunicationController.matched_corners(mergeScenePts, mergeObjPts, msg_hybrid);
								temp = Double.toString(CommunicationController.computeAccuracy());
								percentages[5] = temp;
								System.out.println("SC: " + percentages[5]);
								sendProcessedImage(temp + "-SC");
							}
							break;
						case "HSC":
							if (!alreadyExecuted) {
								alreadyExecuted = true;
								CommunicationController.setHarrisScenePt(CommunicationController.keypoint_extraction(
										ImageModel.get_path() + "/harris/" + ImageModel.get_file_name(), "harris"));
								CommunicationController.setHarrisObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/harris/" + ObjectModel.get_file_name(), "harris"));
								CommunicationController.setShiTomasiScenePt(CommunicationController.keypoint_extraction(
										ImageModel.get_path() + "/shitomasi/" + ImageModel.get_file_name(),
										"shi_tomasi"));
								CommunicationController.setShiTomasiObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/shitomasi/" + ObjectModel.get_file_name(),
										"shi_tomasi"));
								CommunicationController.setCannyScenePt(CommunicationController.keypoint_extraction(
										ImageModel.get_path() + "/canny/" + ImageModel.get_file_name(), "canny"));
								CommunicationController.setCannyObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/canny/" + ObjectModel.get_file_name(), "canny"));

								List<KeyPoint> mergeObjPts = new ArrayList<KeyPoint>();
								mergeObjPts.addAll(CommunicationController.getHarrisObjPt());
								mergeObjPts.addAll(CommunicationController.getShiTomasiObjPt());
								mergeObjPts.addAll(CommunicationController.getCannyObjPt());

								List<KeyPoint> mergeScenePts = new ArrayList<KeyPoint>();
								mergeScenePts.addAll(CommunicationController.getHarrisScenePt());
								mergeScenePts.addAll(CommunicationController.getShiTomasiScenePt());
								mergeScenePts.addAll(CommunicationController.getCannyScenePt());

								CommunicationController.matched_corners(mergeScenePts, mergeObjPts, msg_hybrid);
								temp = Double.toString(CommunicationController.computeAccuracy());
								percentages[6] = temp;
								System.out.println("HSC: " + percentages[6]);
								sendProcessedImage(temp + "-HSC");
							}
							break;
						default:
							System.out.println("Nothing is selected");
						}
						break;
					case "dataset":
						switch (msg_hybrid) {
						case "canny":
							System.out.println("Canny Detector Complete Scanning");
							if (doneDetection) {
								doneDetection = false;
								sendProcessedImage(count + "-canny");
							} else {
								try {
									wait();
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							count = 0;
							break;
						case "shi_tomasi":
							System.out.println("Shi-Tomasi Detector Complete Scanning");
							if (doneDetection) {
								doneDetection = false;
								sendProcessedImage(count + "-shitomasi");
							} else {
								try {
									wait();
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							count = 0;
							break;
						case "harris":
							System.out.println("Harris Detector Complete Scanning");
							if (doneDetection) {
								doneDetection = false;
								sendProcessedImage(count + "-harris");
							} else {
								try {
									wait();
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							count = 0;
							break;
						case "HC":
							if (!alreadyExecuted) {
								alreadyExecuted = true;
								System.out.println("Harris and Canny Detector Complete Scanning");
								if (doneDetection) {
									doneDetection = false;
									count = cannyCount + harrisCount;
									sendProcessedImage(count + "-HC");
								} else {
									try {
										wait();
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								cannyCount = 0;
								harrisCount = 0;
								count = 0;
							}
							break;
						case "HS":
							if (!alreadyExecuted) {
								alreadyExecuted = true;
								System.out.println("Harris and Shi-Tomasi Detector Complete Scanning");
								if (doneDetection) {
									doneDetection = false;
									count = harrisCount + shitomasiCount;
									sendProcessedImage(count + "-HS");
								} else {
									try {
										wait();
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								harrisCount = 0;
								shitomasiCount = 0;
								count = 0;
							}
							break;
						case "SC":
							if (!alreadyExecuted) {
								alreadyExecuted = true;
								System.out.println("Shi-Tomasi and Canny Detector Complete Scanning");
								if (doneDetection) {
									doneDetection = false;
									count = cannyCount + shitomasiCount;
									sendProcessedImage(count + "-SC");
								} else {
									try {
										wait();
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								cannyCount = 0;
								shitomasiCount = 0;
								count = 0;
							}
							break;
						case "HSC":
							if (!alreadyExecuted) {
								alreadyExecuted = true;
								System.out.println("Harris,Shi-Tomasi and Canny Detector Complete Scanning");
								if (doneDetection) {
									doneDetection = false;
									count = cannyCount + shitomasiCount + harrisCount;
									sendProcessedImage(count + "-HSC");
								} else {
									try {
										wait();
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								harrisCount = 0;
								cannyCount = 0;
								shitomasiCount = 0;
								count = 0;
							}
							break;
						default:
							System.out.println("There is some error occured, please restart");
						}
						break;
					default:
						System.out.println("There is some error occured, please restart");
					}
				} else {
					block();
				}
			}

		});

	}

	/*
	 * Send image information to Canny Agent
	 */
	public void sendInfoToCanny(String img_info) {
		addBehaviour(new OneShotBehaviour(this) {
			@Override
			public void action() {
				// TODO Auto-generated method stub

				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(new AID("CannyAgent", AID.ISLOCALNAME));
				msg.setContent(img_info);
				msg.setConversationId("canny_img_info");
				String img_info_array[] = img_info.split("-");

				if (img_info_array.length <= 4) {
					File folder = new File(DirectoryModel.getDir());
					File[] listOfFiles = folder.listFiles(new ImageFileFilter());
					String temp;
					int starter;
					String selected_option = img_info_array[img_info_array.length - 2];
					switch (selected_option) {
					case "canny":
						send(msg);
						CommunicationController.setCannyObjPt(CommunicationController.keypoint_extraction(
								ObjectModel.get_path() + "/canny/" + ObjectModel.get_file_name(), "canny"));
						for (File file : listOfFiles) {
							if (file.isFile()) {
								DirectoryModel.set_file_name(file.getName());

								CommunicationController.setCannyScenePt(CommunicationController.keypoint_extraction(
										DirectoryModel.getDir() + "/canny/" + DirectoryModel.get_file_name(), "canny"));
								CommunicationController.matched_corners_in_dataset(
										CommunicationController.getCannyScenePt(),
										CommunicationController.getCannyObjPt(), "canny");

								temp = Double.toString(CommunicationController.computeAccuracy());

								if (Double.valueOf(temp) >= 90 && Double.valueOf(temp) <= 100) {
									count++;
									System.out.println("Detected: " + count);
								} else {
									System.out.println("No Detected");
								}
								System.out.println(file.getName() + ":" + temp);
							} else if (file.isDirectory()) {
								System.out.println("Directory " + file.getName());
							}
						}
						doneDetection = true;
						break;
					case "HC":
						send(msg);
						CommunicationController.setCannyObjPt(CommunicationController.keypoint_extraction(
								ObjectModel.get_path() + "/canny/" + ObjectModel.get_file_name(), "canny"));
						starter = listOfFiles.length;
						for (int i = (listOfFiles.length / 2); i < starter; i++) {
							File file = listOfFiles[i];
							if (file.isFile()) {
								DirectoryModel.set_file_name(file.getName());

								CommunicationController.setCannyScenePt(CommunicationController.keypoint_extraction(
										DirectoryModel.getDir() + "/canny/" + DirectoryModel.get_file_name(), "canny"));
								CommunicationController.matched_corners_in_dataset(
										CommunicationController.getCannyScenePt(),
										CommunicationController.getCannyObjPt(), "canny");

								temp = Double.toString(CommunicationController.computeAccuracy());

								if (Double.valueOf(temp) >= 90 && Double.valueOf(temp) <= 100) {
									cannyCount++;
									System.out.println("Detected: " + cannyCount);
								} else {
									System.out.println("No Detected");
								}
								System.out.println(file.getName() + ":" + temp);
							} else if (file.isDirectory()) {
								System.out.println("Directory " + file.getName());
							}
						}
						doneDetection = true;
						break;
					case "SC":
						send(msg);
						CommunicationController.setCannyObjPt(CommunicationController.keypoint_extraction(
								ObjectModel.get_path() + "/canny/" + ObjectModel.get_file_name(), "canny"));
						starter = listOfFiles.length;
						for (int i = (listOfFiles.length / 2); i < starter; i++) {
							File file = listOfFiles[i];
							if (file.isFile()) {
								DirectoryModel.set_file_name(file.getName());

								CommunicationController.setCannyScenePt(CommunicationController.keypoint_extraction(
										DirectoryModel.getDir() + "/canny/" + DirectoryModel.get_file_name(), "canny"));
								CommunicationController.matched_corners_in_dataset(
										CommunicationController.getCannyScenePt(),
										CommunicationController.getCannyObjPt(), "canny");

								temp = Double.toString(CommunicationController.computeAccuracy());

								if (Double.valueOf(temp) >= 90 && Double.valueOf(temp) <= 100) {
									cannyCount++;
									System.out.println("Detected: " + cannyCount);
								} else {
									System.out.println("No Detected");
								}
								System.out.println(file.getName() + ":" + temp);
							} else if (file.isDirectory()) {
								System.out.println("Directory " + file.getName());
							}
						}
						doneDetection = true;
						break;
					case "HSC":
						send(msg);
						CommunicationController.setCannyObjPt(CommunicationController.keypoint_extraction(
								ObjectModel.get_path() + "/canny/" + ObjectModel.get_file_name(), "canny"));
						starter = listOfFiles.length;
						for (int i = (listOfFiles.length / 3) + (listOfFiles.length / 3); i < starter; i++) {
							File file = listOfFiles[i];
							if (file.isFile()) {
								DirectoryModel.set_file_name(file.getName());

								CommunicationController.setCannyScenePt(CommunicationController.keypoint_extraction(
										DirectoryModel.getDir() + "/canny/" + DirectoryModel.get_file_name(), "canny"));
								CommunicationController.matched_corners_in_dataset(
										CommunicationController.getCannyScenePt(),
										CommunicationController.getCannyObjPt(), "canny");

								temp = Double.toString(CommunicationController.computeAccuracy());

								if (Double.valueOf(temp) >= 90 && Double.valueOf(temp) <= 100) {
									cannyCount++;
									System.out.println("Detected: " + cannyCount);
								} else {
									System.out.println("No Detected");
								}
								System.out.println(file.getName() + ":" + temp);
							} else if (file.isDirectory()) {
								System.out.println("Directory " + file.getName());
							}
						}
						doneDetection = true;
						break;
					default:
						System.out.println("System error, please restart");
					}
				} else {
					send(msg);
					System.out.println("Image information send to Canny Agent");
				}
			}
		});
	}

	/*
	 * Send image information to Harris Agent
	 */
	public void sendInfoToHarris(String img_info) {
		addBehaviour(new OneShotBehaviour(this) {
			@Override
			public void action() {
				// TODO Auto-generated method stub
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(new AID("HarrisAgent", AID.ISLOCALNAME));
				msg.setContent(img_info);
				msg.setConversationId("harris_img_info");

				String img_info_array[] = img_info.split("-");

				if (img_info_array.length <= 4) {

					File folder = new File(DirectoryModel.getDir());
					File[] listOfFiles = folder.listFiles(new ImageFileFilter());
					String temp;
					int starter;
					String selected_option = img_info_array[img_info_array.length - 2];
					switch (selected_option) {
					case "harris":
						send(msg);
						CommunicationController.setHarrisObjPt(CommunicationController.keypoint_extraction(
								ObjectModel.get_path() + "/harris/" + ObjectModel.get_file_name(), "harris"));
						for (File file : listOfFiles) {
							if (file.isFile()) {
								DirectoryModel.set_file_name(file.getName());

								CommunicationController.setHarrisScenePt(CommunicationController.keypoint_extraction(
										DirectoryModel.getDir() + "/harris/" + DirectoryModel.get_file_name(),
										"harris"));
								CommunicationController.matched_corners_in_dataset(
										CommunicationController.getHarrisScenePt(),
										CommunicationController.getHarrisObjPt(), "harris");

								temp = Double.toString(CommunicationController.computeAccuracy());

								if (Double.valueOf(temp) >= 90 && Double.valueOf(temp) <= 100) {
									count++;
									System.out.println("Detected: " + count);
								} else {
									System.out.println("No Detected");
								}
								System.out.println(file.getName() + ":" + temp);
							} else if (file.isDirectory()) {
								System.out.println("Directory " + file.getName());
							}
						}
						doneDetection = true;
						break;
					case "HC":
						send(msg);
						CommunicationController.setHarrisObjPt(CommunicationController.keypoint_extraction(
								ObjectModel.get_path() + "/harris/" + ObjectModel.get_file_name(), "harris"));
						starter = listOfFiles.length;
						for (int i = 0; i < (starter / 2); i++) {
							File file = listOfFiles[i];
							if (file.isFile()) {
								DirectoryModel.set_file_name(file.getName());

								CommunicationController.setHarrisScenePt(CommunicationController.keypoint_extraction(
										DirectoryModel.getDir() + "/harris/" + DirectoryModel.get_file_name(),
										"harris"));
								CommunicationController.matched_corners_in_dataset(
										CommunicationController.getHarrisScenePt(),
										CommunicationController.getHarrisObjPt(), "harris");

								temp = Double.toString(CommunicationController.computeAccuracy());

								if (Double.valueOf(temp) >= 90 && Double.valueOf(temp) <= 100) {
									harrisCount++;
									System.out.println("Detected: " + harrisCount);
								} else {
									System.out.println("No Detected");
								}
								System.out.println(file.getName() + ":" + temp);
							} else if (file.isDirectory()) {
								System.out.println("Directory " + file.getName());
							}
						}
						doneDetection = true;
						break;
					case "HS":
						send(msg);
						CommunicationController.setHarrisObjPt(CommunicationController.keypoint_extraction(
								ObjectModel.get_path() + "/harris/" + ObjectModel.get_file_name(), "harris"));
						starter = listOfFiles.length;
						for (int i = 0; i < (starter / 2); i++) {
							File file = listOfFiles[i];
							if (file.isFile()) {
								DirectoryModel.set_file_name(file.getName());

								CommunicationController.setHarrisScenePt(CommunicationController.keypoint_extraction(
										DirectoryModel.getDir() + "/harris/" + DirectoryModel.get_file_name(),
										"harris"));
								CommunicationController.matched_corners_in_dataset(
										CommunicationController.getHarrisScenePt(),
										CommunicationController.getHarrisObjPt(), "harris");

								temp = Double.toString(CommunicationController.computeAccuracy());

								if (Double.valueOf(temp) >= 90 && Double.valueOf(temp) <= 100) {
									harrisCount++;
									System.out.println("Detected: " + harrisCount);
								} else {
									System.out.println("No Detected");
								}
								System.out.println(file.getName() + ":" + temp);
							} else if (file.isDirectory()) {
								System.out.println("Directory " + file.getName());
							}
						}
						doneDetection = true;
						break;
					case "HSC":
						send(msg);
						CommunicationController.setHarrisObjPt(CommunicationController.keypoint_extraction(
								ObjectModel.get_path() + "/harris/" + ObjectModel.get_file_name(), "harris"));
						starter = listOfFiles.length;
						for (int i = 0; i < (starter / 3); i++) {
							File file = listOfFiles[i];
							if (file.isFile()) {
								DirectoryModel.set_file_name(file.getName());

								CommunicationController.setHarrisScenePt(CommunicationController.keypoint_extraction(
										DirectoryModel.getDir() + "/harris/" + DirectoryModel.get_file_name(),
										"harris"));
								CommunicationController.matched_corners_in_dataset(
										CommunicationController.getHarrisScenePt(),
										CommunicationController.getHarrisObjPt(), "harris");

								temp = Double.toString(CommunicationController.computeAccuracy());

								if (Double.valueOf(temp) >= 90 && Double.valueOf(temp) <= 100) {
									harrisCount++;
									System.out.println("Detected: " + harrisCount);
								} else {
									System.out.println("No Detected");
								}
								System.out.println(file.getName() + ":" + temp);
							} else if (file.isDirectory()) {
								System.out.println("Directory " + file.getName());
							}
						}
						doneDetection = true;
						break;
					default:
						System.out.println("System error, please restart");
					}
				} else {
					send(msg);
					System.out.println("Image information send to Harris Agent");
				}
			}
		});
	}

	/*
	 * Send image information to Shi-Tomasi Agent
	 */
	public void sendInfoToShiTomasi(String img_info) {
		addBehaviour(new OneShotBehaviour(this) {
			@Override
			public void action() {
				// TODO Auto-generated method stub
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(new AID("ShiTomasiAgent", AID.ISLOCALNAME));
				msg.setContent(img_info);
				msg.setConversationId("shitomasi_img_info");
				String img_info_array[] = img_info.split("-");

				if (img_info_array.length <= 4) {
					File folder = new File(DirectoryModel.getDir());
					File[] listOfFiles = folder.listFiles(new ImageFileFilter());
					String temp;
					int starter;
					String selected_option = img_info_array[img_info_array.length - 2];
					switch (selected_option) {
					case "shi_tomasi":
						send(msg);
						CommunicationController.setShiTomasiObjPt(CommunicationController.keypoint_extraction(
								ObjectModel.get_path() + "/shitomasi/" + ObjectModel.get_file_name(), "shi_tomasi"));
						for (File file : listOfFiles) {
							if (file.isFile()) {
								DirectoryModel.set_file_name(file.getName());

								CommunicationController.setShiTomasiScenePt(CommunicationController.keypoint_extraction(
										DirectoryModel.getDir() + "/shitomasi/" + DirectoryModel.get_file_name(),
										"shi_tomasi"));
								CommunicationController.matched_corners_in_dataset(
										CommunicationController.getShiTomasiScenePt(),
										CommunicationController.getShiTomasiObjPt(), "shi_tomasi");

								temp = Double.toString(CommunicationController.computeAccuracy());

								if (Double.valueOf(temp) >= 90 && Double.valueOf(temp) <= 100) {
									count++;
									System.out.println("Detected: " + count);
								} else {
									System.out.println("No Detected");
								}
								System.out.println(file.getName() + ":" + temp);
							} else if (file.isDirectory()) {
								System.out.println("Directory " + file.getName());
							}
						}
						doneDetection = true;
						break;
					case "HS":
						send(msg);
						CommunicationController.setHarrisObjPt(CommunicationController.keypoint_extraction(
								ObjectModel.get_path() + "/harris/" + ObjectModel.get_file_name(), "harris"));
						starter = listOfFiles.length;
						for (int i = 0; i < (starter / 2); i++) {
							File file = listOfFiles[i];
							if (file.isFile()) {
								DirectoryModel.set_file_name(file.getName());

								CommunicationController.setHarrisScenePt(CommunicationController.keypoint_extraction(
										DirectoryModel.getDir() + "/harris/" + DirectoryModel.get_file_name(),
										"harris"));
								CommunicationController.matched_corners_in_dataset(
										CommunicationController.getHarrisScenePt(),
										CommunicationController.getHarrisObjPt(), "harris");

								temp = Double.toString(CommunicationController.computeAccuracy());

								if (Double.valueOf(temp) >= 90 && Double.valueOf(temp) <= 100) {
									shitomasiCount++;
									System.out.println("Detected: " + shitomasiCount);
								} else {
									System.out.println("No Detected");
								}
								System.out.println(file.getName() + ":" + temp);
							} else if (file.isDirectory()) {
								System.out.println("Directory " + file.getName());
							}
						}
						doneDetection = true;
						break;
					case "SC":
						send(msg);
						CommunicationController.setShiTomasiObjPt(CommunicationController.keypoint_extraction(
								ObjectModel.get_path() + "/shitomasi/" + ObjectModel.get_file_name(), "shi_tomasi"));
						starter = listOfFiles.length;
						for (int i = 0; i < (starter / 2); i++) {
							File file = listOfFiles[i];
							if (file.isFile()) {
								DirectoryModel.set_file_name(file.getName());

								CommunicationController.setShiTomasiScenePt(CommunicationController.keypoint_extraction(
										DirectoryModel.getDir() + "/shitomasi/" + DirectoryModel.get_file_name(),
										"shi_tomasi"));
								CommunicationController.matched_corners_in_dataset(
										CommunicationController.getShiTomasiScenePt(),
										CommunicationController.getShiTomasiObjPt(), "shi_tomasi");

								temp = Double.toString(CommunicationController.computeAccuracy());

								if (Double.valueOf(temp) >= 90 && Double.valueOf(temp) <= 100) {
									shitomasiCount++;
									System.out.println("Detected: " + shitomasiCount);
								} else {
									System.out.println("No Detected");
								}
								System.out.println(file.getName() + ":" + temp);
							} else if (file.isDirectory()) {
								System.out.println("Directory " + file.getName());
							}
						}
						doneDetection = true;
						break;
					case "HSC":
						send(msg);
						CommunicationController.setShiTomasiObjPt(CommunicationController.keypoint_extraction(
								ObjectModel.get_path() + "/shitomasi/" + ObjectModel.get_file_name(), "shi_tomasi"));
						starter = listOfFiles.length;
						for (int i = ((starter / 3) + (starter / 3)); i < starter; i++) {
							File file = listOfFiles[i];
							if (file.isFile()) {
								DirectoryModel.set_file_name(file.getName());

								CommunicationController.setShiTomasiScenePt(CommunicationController.keypoint_extraction(
										DirectoryModel.getDir() + "/shitomasi/" + DirectoryModel.get_file_name(),
										"shi_tomasi"));
								CommunicationController.matched_corners_in_dataset(
										CommunicationController.getShiTomasiScenePt(),
										CommunicationController.getShiTomasiObjPt(), "shi_tomasi");

								temp = Double.toString(CommunicationController.computeAccuracy());

								if (Double.valueOf(temp) >= 90 && Double.valueOf(temp) <= 100) {
									shitomasiCount++;
									System.out.println("Detected: " + shitomasiCount);
								} else {
									System.out.println("No Detected");
								}
								System.out.println(file.getName() + ":" + temp);
							} else if (file.isDirectory()) {
								System.out.println("Directory " + file.getName());
							}
						}
						doneDetection = true;
						break;
					default:
						System.out.println("System error, please restart");
					}
				} else {
					send(msg);
					System.out.println("Image information send to ShiTomasi Agent");
				}
			}
		});
	}

	/*
	 * When the object detection is completed, return the result to pre-processing
	 * agent
	 */
	protected void sendProcessedImage(String content) {
		// TODO Auto-generated method stub
		addBehaviour(new OneShotBehaviour(this) {
			@Override
			public void action() {
				// TODO Auto-generated method stub
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(new AID("preprocessing", AID.ISLOCALNAME));
				msg.setContent(content);
				msg.setConversationId("processed_result");
				send(msg);
				System.out.println("Process_status_send\n");
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
