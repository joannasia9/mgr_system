import glob
import argparse
import os

ap = argparse.ArgumentParser()
ap.add_argument("-d", "--directory", required=True, help="directory of files")
args = vars(ap.parse_args())

directory = args["directory"]

files = glob.glob("%s/*" % directory)

num = 0
for file in files:
    filename, extension = os.path.splitext(file)
    number = '{0:09}'.format(num)
    num = num + 1
    dest_path = "%s/%s%s%s" % (directory, directory, number, extension)
    os.rename(file, dest_path)
    print("Proceed: %s" % dest_path)
