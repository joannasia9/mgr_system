import glob
from PIL import Image
import argparse

ap = argparse.ArgumentParser()
ap.add_argument("-d", "--directory", required=True, help="directory of files")
args = vars(ap.parse_args())

files = glob.glob("%s/*" %args["directory"])

i=0
for file in files:
	img = Image.open(file)
	thresh = 100
	fn = lambda x : 255 if x > thresh else 0
	#image_file = img.convert('L').point(fn, mode='1')
	image_file = img.convert('1')
	filename = "h2/%s.jpg" %i
	i=i+1
	image_file.save(filename)
	print("Proceed: %s" %file)