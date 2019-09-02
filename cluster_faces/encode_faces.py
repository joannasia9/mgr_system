# USAGE
# python encode_faces.py --dataset dataset

# import the necessary packages
from imutils import paths
import face_recognition
import pickle
import cv2
import os
import sqlite3
import numpy as np
import io

def get_parent_dir(directory):
	import os
	return os.path.dirname(directory)

current_dirs_parent = get_parent_dir(os.getcwd())

database_dir = "%s/database/test_database.db" %current_dirs_parent

print("[INFO] Connecting to SQLite")
conn = sqlite3.connect(database_dir)
cursor = conn.cursor()

print("[INFO] quantifying faces...")
cursor.execute("SELECT frame_image FROM frame WHERE frame_ordered='0'")
frames = cursor.fetchall()

if(conn):
	conn.close()

data = []
for (i, frame) in enumerate(frames):
	print("[INFO] processing image {}/{}".format(i + 1, len(frames)))

	image = np.frombuffer(frame[0], dtype=np.uint8)
	rgb = cv2.imdecode(image, cv2.IMREAD_COLOR)
	resized = cv2.resize(rgb,(350,350))

	boxes = [(0,350,350,0)]
	encodings = face_recognition.face_encodings(resized,boxes)

	d = [{"loc": box, "encoding": enc}
		for (box, enc) in zip(boxes, encodings)]
	data.extend(d)

encodings_path = "%s/cluster_faces/encodings.pickle" %current_dirs_parent
print("[INFO] serializing encodings...")

f = open(encodings_path, "wb")
f.write(pickle.dumps(data))
f.close()
