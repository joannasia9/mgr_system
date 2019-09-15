# USAGE
# python encode_faces.py

import face_recognition
import pickle
import cv2
import os
import sqlite3
import numpy as np

def create_connection(db_file):
    conn = None
    try:
        conn = sqlite3.connect(db_file)
    except sqlite3.Error as e:
        print(e)

    return conn


def close_connection(conn):
    if (conn):
        conn.close()

def fetch_frames(conn):
    cursor = conn.cursor()
    cursor.execute("SELECT frame_image, frame_id FROM frame WHERE frame_ordered='0'")
    frames = cursor.fetchall()

    return frames

def encode(frames):
    data = []

    for frame in frames:
        image = np.frombuffer(frame[0], dtype=np.uint8)
        rgb = cv2.imdecode(image, cv2.IMREAD_COLOR)
        resized = cv2.resize(rgb, (350, 350))

        boxes = [(0, 350, 350, 0)]
        encodings = face_recognition.face_encodings(resized, boxes)

        d = [{"loc": box, "encoding": enc, "id": id} for (box, enc, id) in zip(boxes, encodings, [frame[1]])]
        data.extend(d)

    return data


def serialize_encodings(data):
    encodings_path = "%s/cluster_faces/encodings.pickle" % os.getcwd()
    f = open(encodings_path, "wb")
    f.write(pickle.dumps(data))
    f.close()


def main():
    print("[INFO] Connecting to SQLite")
    database_dir = "%s/database/test_database.db" % os.getcwd()
    conn = create_connection(database_dir)

    print("[INFO] Quantifying faces...")
    frames = fetch_frames(conn)
    close_connection(conn)

    print("[INFO] Encoding %s faces..." %len(frames))
    data = encode(frames)

    print("[INFO] Serializing encodings...")
    serialize_encodings(data)

    print("[SUCCESS] Encoding process finished successfully.\n")


if __name__ == '__main__':
    main()







