import glob
import cv2
from skimage.feature import hog
from skimage import exposure
import argparse

ap = argparse.ArgumentParser()
ap.add_argument("-d", "--directory", required=True, help="directory of files")
args = vars(ap.parse_args())

files = glob.glob("%s/*" %args["directory"])
i = 0
for file in files:
    image = cv2.imread(file)
    (H, hog_image) = hog(image, orientations=9, pixels_per_cell=(8, 8), cells_per_block=(2, 2), transform_sqrt=True,
                         block_norm="L1", visualise=True)
    hog_image = exposure.rescale_intensity(hog_image, out_range=(0, 255))
    hog_image = hog_image.astype("uint8")

    filename = "s2/%s.jpg" % i
    i = i + 1
    cv2.imwrite(filename, hog_image)
    print("Proceed: %s" % file)
