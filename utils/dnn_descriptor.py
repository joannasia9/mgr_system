import glob
import cv2
import numpy as np
import os
import argparse

ap = argparse.ArgumentParser()
ap.add_argument("-d", "--directory", required=True, help="directory of files")
args = vars(ap.parse_args())


people = ['{0:05}'.format(num) for num in range(1, 1209)]
prototxt = "deploy.prototxt"
model = "res10_300x300_ssd_iter_140000.caffemodel"
net = cv2.dnn.readNetFromCaffe(prototxt, model)


def detect_faces():
    files = glob.glob("%s/*" %args["directory"])

    for f in files:
        filename, extension = os.path.splitext(f)
        image = cv2.imread(f)
        (h, w) = image.shape[:2]
        blob = cv2.dnn.blobFromImage(cv2.resize(image, (300, 300)), 1.0, (300, 300), (103.93, 116.77, 123.68))

        print("[INFO] computing object detections...")
        net.setInput(blob)
        blob = cv2.dnn.blobFromImage(cv2.resize(image, (300, 300)), 1.0, (300, 300), (104, 177, 123))
        net.setInput(blob)
        detections = net.forward()
        if detections.any():
            confidence = detections[0, 0, 0, 2]
            if confidence > 0.9:
                box = detections[0, 0, 0, 3:7] * np.array([w, h, w, h])
                (startX, startY, endX, endY) = box.astype("int")
                cropped = image[startY:endY, startX:endX]
                (h, w) = cropped.shape[:2]
                if not (w == 0 or h == 0):
                    gray = cv2.cvtColor(cropped, cv2.COLOR_BGR2GRAY)
                    resized = cv2.resize(gray, (300, 300))
                    cv2.imwrite("%s.jpg" % filename, resized)
                    print("proceed file: %s" % f)
        os.remove(f)

detect_faces()


