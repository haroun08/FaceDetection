import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
// ctrl + shift +O 

public class FaceDetectionMain {
    public static String pathFace = "src/main/resources/haarcascade_frontalface_alt2.xml";
    public static String pathEye = "src/main/resources/haarcascade_eye_tree_eyeglasses.xml";
    public static String pythonScript = "/src/main/resources/Emotion.py";
    public static String pythonCommand = "python " + pythonScript;
    public static String messageFace = "FaceCascade successfully Loaded";
    public static String messageEye = "EyeCascade successfully Loaded";
    public static String error = "⚠️ Failed to Load Cascades";
    public static String webCamError = "⚠️ Failed to open the webcam";
    public static String webcamReadError = "⚠️ Failed to read from the webcam";

    private static List<DetectedFace> detectedFaces = new ArrayList<>();
  //  private static ProcessBuilder processBuilder = new ProcessBuilder("cmd","python", pythonScript);
    private static ProcessBuilder processBuilder = new ProcessBuilder(pythonCommand.split(" "));
    private static Logger demoLogger = LogManager.getLogger(FaceDetectionMain.class);
	// packaging ( war web )
    boolean getBool(){return true;}
    
    public static void main(String[] args) {
       demoLogger.info("------------------------\nProgram launched");

        System.out.println("Welcome");
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        CascadeClassifier faceCascade = new CascadeClassifier();
        faceCascade.load(pathFace);
        if (faceCascade.empty()) {
            demoLogger.error(error);
            return;
        } else {
            demoLogger.info(messageFace);
        }

        CascadeClassifier eyeCascade = new CascadeClassifier();

        // Load eye cascade only if face cascade is loaded
        if (!faceCascade.empty()) {
            eyeCascade.load(pathEye);
            if (!eyeCascade.empty()) {
                demoLogger.info(messageEye);
            } else {
                demoLogger.error(error);
                return;
            }
        }

        loadSavedFaces();

        VideoCapture videoCapture = new VideoCapture(0);
        videoCapture.set(Videoio.CAP_PROP_FRAME_WIDTH, 1920);
        videoCapture.set(Videoio.CAP_PROP_FRAME_HEIGHT, 1080);
        if (!videoCapture.isOpened()) {
            System.err.println(webCamError);
            return;
        }

        JFrame frame = new JFrame("HarFoun Program");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1920, 1080);
        frame.setLayout(null); 
        frame.setVisible(true);
        JLabel label = new JLabel();
        label.setBounds(0, 0, 1920, 1080); 
        frame.add(label);
        
        JButton DisplayEmotion = new JButton("Display Emotion");
        JButton saveButton = new JButton("Save Face");
        JButton previewButton = new JButton("Preview Faces");
        saveButton.setBounds(10, 10, 120, 30); 
        previewButton.setBounds(140, 10, 120, 30); 
        DisplayEmotion.setBounds(280, 10, 120, 30); 
        frame.add(saveButton);
        frame.add(previewButton);
        frame.add(DisplayEmotion);
        Mat frameMat = new Mat();

        DisplayEmotion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	demoLogger.info("python code running");
            	System.out.println("Python running--------");
                Thread emotionThread = new Thread(() -> {
                    try {
                        Process process = processBuilder.start();
                        process.waitFor();
                        demoLogger.info("Python script execution completed.");
                    	System.out.println("Python running--------TEST---------");

                    } catch (IOException | InterruptedException ex) {
                        demoLogger.error("Error executing Python script: " + ex.getMessage());
                    }
                });
                emotionThread.start();
            }
        });

       
        
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	demoLogger.info("save Button Pressed");
                String name = JOptionPane.showInputDialog(frame, "Enter the name for the detected face:", "Save Face", JOptionPane.INFORMATION_MESSAGE);
                if (name == null || name.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Name cannot be empty. Please enter a valid name.", "Error", JOptionPane.ERROR_MESSAGE);
                    demoLogger.error("Failed to save image");
                    return;
                }

                String id = JOptionPane.showInputDialog(frame, "Enter the ID for the detected face:", "Save Face", JOptionPane.INFORMATION_MESSAGE);
                if (id == null || id.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "ID cannot be empty. Please enter a valid ID.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Mat grayFrame = new Mat(); // Create grayFrame here
                if (videoCapture.read(frameMat)) {
                    Imgproc.cvtColor(frameMat, grayFrame, Imgproc.COLOR_BGR2GRAY);
                    Imgproc.equalizeHist(grayFrame, grayFrame);
                    MatOfRect faces = new MatOfRect();
                    faceCascade.detectMultiScale(grayFrame, faces);
                    saveDetectedFace(faces.toArray(), frameMat, name, id);
   //               demoLogger.info("Face successfully saved ");
                } else {
                    demoLogger.error(webcamReadError);
                }
            }
        });
        previewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	demoLogger.info("preview  Button Pressed");
                Mat grayFrame = new Mat(); 
                if (videoCapture.read(frameMat)) {
                    Imgproc.cvtColor(frameMat, grayFrame, Imgproc.COLOR_BGR2GRAY);
                    Imgproc.equalizeHist(grayFrame, grayFrame);
                    MatOfRect faces = new MatOfRect();
                    faceCascade.detectMultiScale(grayFrame, faces);
                    displayDetectedFaces(faces.toArray(), frameMat);
                } else {
                    demoLogger.error(webcamReadError);
                }
            }
        });

        while (true) {
            if (!videoCapture.read(frameMat)) { // read from video capture and store in frameMat
                demoLogger.error(webcamReadError);
                break;
            }

            Mat grayFrame = new Mat();
            Imgproc.cvtColor(frameMat, grayFrame, Imgproc.COLOR_BGR2GRAY);// convert frameMat to grayFrame
            Imgproc.equalizeHist(grayFrame, grayFrame);

            MatOfRect faces = new MatOfRect();
            faceCascade.detectMultiScale(grayFrame, faces);

            int closestFaceIndex = -1;
            double closestFaceDistance = Double.MAX_VALUE;

            for (int faceCounter = 0; faceCounter < faces.toArray().length; faceCounter++) {
                Rect rect = faces.toArray()[faceCounter];
                Imgproc.rectangle(frameMat, rect.tl(), rect.br(), new Scalar(0, 255, 0), 3);

                if (rect.area() > 0) {
                    double distance = calculateDistance(rect);
                    if (distance < closestFaceDistance) {
                        closestFaceDistance = distance;
                        closestFaceIndex = faceCounter;
                    }
                }
            }

            if (closestFaceIndex >= 0) {
                Rect closestRect = faces.toArray()[closestFaceIndex];
                Imgproc.rectangle(frameMat, closestRect.tl(), closestRect.br(), new Scalar(255, 0, 0), 2);

                if (!eyeCascade.empty()) {
                    Mat faceROI = grayFrame.submat(closestRect);
                    MatOfRect eyes = new MatOfRect();
                    eyeCascade.detectMultiScale(faceROI, eyes);
                    for (Rect rect : eyes.toArray()) {
                        Rect eyeRect = new Rect(closestRect.x + rect.x, closestRect.y + rect.y, rect.width,
                                rect.height);
                        Imgproc.rectangle(frameMat, eyeRect.tl(), eyeRect.br(), new Scalar(0, 0, 255), 1);
                    }
                }
            }

            BufferedImage image = matToBufferedImage(frameMat);
            label.setIcon(new ImageIcon(image));
            label.repaint();
        }

        videoCapture.release();
        frame.dispose();
    }

    private static void loadSavedFaces() {
        File folder = new File("C:/temp");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        detectedFaces.clear(); // Clear the list before loading new faces

        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    String filename = file.getName();
                    if (filename.startsWith("face_") && filename.endsWith(".png")) {
                        String[] parts = filename.split("_");
                        if (parts.length == 4) {
                            String name = parts[1];
                            String id = parts[2];
                            String filePath = file.getAbsolutePath();
                            DetectedFace detectedFace = new DetectedFace(name, id, filePath);
                            detectedFaces.add(detectedFace);
                        }
                    }
                }
            }
        }
    }

    private static BufferedImage matToBufferedImage(Mat mat) {
        int width = mat.cols();
        int height = mat.rows();
        int channels = mat.channels();
        byte[] sourceData = new byte[width * height * channels];
        mat.get(0, 0, sourceData);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        final byte[] targetData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(sourceData, 0, targetData, 0, sourceData.length);

        return image;
    }

    private static void saveDetectedFace(Rect[] faces, Mat frameMat, String name, String id) {
        File folder = new File("C:/temp");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        for (int i = 0; i < faces.length; i++) {
            Rect faceRect = faces[i];
            Mat faceROI = frameMat.submat(faceRect);
            File outputImage = new File("C:/temp/face_" + name + "_" + id + "_" + i + ".png");
            BufferedImage bufferedImage = matToBufferedImage(faceROI);
            try {
                ImageIO.write(bufferedImage, "png", outputImage);
                demoLogger.info("Face " + name + " with ID " + id + " saved successfully.");
            } catch (IOException ex) {
                demoLogger.error("Error saving face " + name + " with ID " + id + ": " + ex.getMessage());
            }
        }
    }

    private static void displayDetectedFaces(Rect[] faces, Mat frameMat) {
        Mat previewFrame = frameMat.clone();
        for (Rect faceRect : faces) {
            Imgproc.rectangle(previewFrame, faceRect.tl(), faceRect.br(), new Scalar(255, 0, 0), 2);
        }

        BufferedImage previewImage = matToBufferedImage(previewFrame);

        JFrame previewFrameDisplay = new JFrame("Detected Faces Preview");
        previewFrameDisplay.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        previewFrameDisplay.setSize(800, 600);

        JLabel previewLabel = new JLabel(new ImageIcon(previewImage));
        previewFrameDisplay.add(previewLabel);
        previewFrameDisplay.setVisible(true);

        // Show face name and ID when clicking on the preview
        previewLabel.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent e) {
        	    int mouseX = e.getX();
        	    int mouseY = e.getY();
        	    for (DetectedFace detectedFace : detectedFaces) {
        	        Rect faceRect = detectedFace.getFaceRect();
        	        if (faceRect != null) {
        	            int faceX = faceRect.x;
        	            int faceY = faceRect.y;
        	            int faceWidth = faceRect.width;
        	            int faceHeight = faceRect.height;

        	            if (mouseX >= faceX && mouseX <= faceX + faceWidth && mouseY >= faceY && mouseY <= faceY + faceHeight) {
        	                JOptionPane.showMessageDialog(
        	                    previewFrameDisplay,
        	                    "Name: " + detectedFace.getName() + "\nID: " + detectedFace.getId(),
        	                    "Detected Face Information",
        	                    JOptionPane.INFORMATION_MESSAGE
        	                );
        	                break; // No need to check other faces once a match is found
        	            }
        	        }
        	    }
        	}
        });
    }
    private static double calculateDistance(Rect rect) {
        double focalLength = 1000.0;
        double averageFaceWidth = 0.15;
        double faceWidth = rect.width;
        double distance = (averageFaceWidth * focalLength) / faceWidth;

        return distance;
    }
}