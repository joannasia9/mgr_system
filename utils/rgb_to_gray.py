import glob
import cv2
import argparse

ap = argparse.ArgumentParser()
ap.add_argument("-d", "--directory", required=True, help="directory of files")
args = vars(ap.parse_args())

directory = args["directory"]

files = glob.glob("%s/*" % directory)

files = glob.glob("temporary/*")
for file in files:
    image = cv2.imread(file)
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    cv2.imwrite(file, gray)
    print("Proceed: %s" % file)
