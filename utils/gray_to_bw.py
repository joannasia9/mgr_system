import glob
from PIL import Image
import argparse

ap = argparse.ArgumentParser()
ap.add_argument("-d", "--directory", required=True, help="directory of files")
args = vars(ap.parse_args())

files = glob.glob("%s/*" %args["directory"])
i=0
for file in files:
	image_file = Image.open(file)
	image_file = image_file.convert('1') # convert image to black and white
	filename = "h2/%s.jpg" %i
	i=i+1
	image_file.save(filename)
	print("Proceed: %s" %file)