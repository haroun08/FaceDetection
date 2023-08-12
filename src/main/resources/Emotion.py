import cv2
import matplotlib.pyplot as plt
from deepface import DeepFace

# Load the cascade classifier
face_cascade = cv2.CascadeClassifier(r'C:\Users\kunha\Videos\Java\Face-eye-detection-using-Haar-Cascade-classifier-master\Face-eye-detection-using-Haar-Cascade-classifier-master\haarcascade_frontalface_default.xml')
if face_cascade.empty():
    print("⚠️ Failed to Load Classifier ")

# Set the video resolution to 1920x1080
cap = cv2.VideoCapture(0)
cap.set(cv2.CAP_PROP_FRAME_WIDTH, 1920)
cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 1080)

while True:
    ret, frame = cap.read()

    # Perform emotion analysis on the frame
    result = DeepFace.analyze(frame, actions=['emotion'], enforce_detection=False)
    result_dict = result[0]
    emotion_str = result_dict['dominant_emotion']
    emotion_percent = result_dict['emotion'][emotion_str]

    # Detect faces
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    faces = face_cascade.detectMultiScale(gray, scaleFactor=1.1, minNeighbors=4, minSize=(30, 30))

    # Draw rectangles around the faces and display emotion label
    font = cv2.FONT_HERSHEY_DUPLEX
    for (x, y, w, h) in faces:
        cv2.rectangle(frame, (x, y), (x+w, y+h), (0, 255, 0), 2)
        text = f"{emotion_str} ({emotion_percent:.2f}%)"
        text_size, _ = cv2.getTextSize(text, font, 1, 2)
        text_x = int(x + (w - text_size[0]) / 2)
        text_y = int(y + h + text_size[1])
        cv2.putText(frame, text, (text_x, text_y), font, 1, (0, 0, 255), 2)

    # Display the frame
    cv2.imshow('original video ', frame)

    # Exit loop if 'q' is pressed
    if cv2.waitKey(2) & 0xFF == ord('q'):
        break

# Release video capture and close the window
cap.release()
cv2.destroyAllWindows()
