import glob
import cv2
from keras.models import load_model
from keras.preprocessing.image import img_to_array
import numpy as np
import argparse
import os

EMOTIONS = ["Anger", "Disgust", "Fear", "Happiness", "Sadness", "Surprise", "Neutral"]

ap = argparse.ArgumentParser()
ap.add_argument("-e", "--emotion", required=True, help="emotion")
ap.add_argument("-f", "--folder", required=True, help="images directory path")
args = vars(ap.parse_args())

directory = args["folder"]

model = load_model('emotion_model.hdf5', compile=False)


def detect_emotions(emotion):
    files = glob.glob("%s/*" % directory)
    for file in files:
        image = cv2.imread(file)
        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        roi = cv2.resize(gray, (64, 64))
        roi = roi.astype("float") / 255.0
        roi = img_to_array(roi)
        roi = np.expand_dims(roi, axis=0)

        predictions = model.predict(roi)[0]
        emotion_probability = np.max(predictions)
        label = EMOTIONS[predictions.argmax()]
        print("emotion_probability : %s label:  %s" % (emotion_probability, label))
        if not (emotion_probability > 0.5 and label == emotion):
            os.remove(file)
        print("Proceed: %s" % file)


detect_emotions(args["emotion"])
