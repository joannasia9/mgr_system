# USAGE
# python cluster_faces.py

from sklearn.cluster import DBSCAN
import numpy as np
import pickle
import os
import sqlite3
import cv2
import face_recognition

from itertools import groupby
from operator import itemgetter


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


def update_frame(conn, frame):
    sql = "UPDATE frame SET frame_ordered = ? WHERE frame_id = ?"
    cur = conn.cursor()
    cur.execute(sql, frame)
    conn.commit()


def get_all_from_person(conn):
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM person")
    people = cursor.fetchall()

    return people


def find_frames_by_id(conn, ids):
    frame_ids = ','.join(map(lambda id: str(id), ids))
    sql = "SELECT frame_id,frame_image FROM frame WHERE frame_id IN ({0})".format(frame_ids)
    cursor = conn.cursor()
    cursor.execute(sql)
    frames = cursor.fetchall()

    return frames


def load_data():
    encodings_path = "%s/cluster_faces/encodings.pickle" % os.getcwd()
    data = pickle.loads(open(encodings_path, "rb").read())

    return np.array(data)


def load_encodings(data):
    encodings = [d["encoding"] for d in data]

    return encodings


def load_frame_ids(data):
    ids = [d["id"] for d in data]

    return ids


def cluster_faces(encodings):
    clt = DBSCAN(metric="euclidean", min_samples=2, n_jobs=-1)
    clt.fit(encodings)

    return clt.labels_


def filter_ids(labels, frames_ids):
    tuples = zip(labels, frames_ids)
    groups = [(label, list(list(zip(*index))[1])) for label, index in groupby(tuples, itemgetter(0))]
    outliers = [group[1] for group in groups if group[0] == -1]
    groups = [group for group in groups if group[0] != -1]

    ids_to_take = []
    for outlier_group in outliers:
        ids_to_take.extend(outlier_group)

    ids_to_take.extend([group[1][0] for group in groups])
    return ids_to_take


def set_groups(conn, labels, frames_ids):
    tuples = zip(labels, frames_ids)
    groups = [(label, list(list(zip(*index))[1])) for label, index in groupby(tuples, itemgetter(0))]

    for group in groups:
        for id in group[1]:
            new_id = min(group[1])
            set_person_id(conn, id, new_id)

    return groups


def set_person_id(conn, old_id, new_id):
    sql = "UPDATE frame SET person_id = '%s' WHERE frame_id = '%s'" % (new_id, old_id)
    cursor = conn.cursor()
    cursor.execute(sql)
    conn.commit()


def encode(frames):
    data = []

    for frame in frames:
        image = np.frombuffer(frame[1], dtype=np.uint8)
        rgb = cv2.imdecode(image, cv2.IMREAD_COLOR)
        resized = cv2.resize(rgb, (350, 350))

        boxes = [(0, 350, 350, 0)]
        encodings = face_recognition.face_encodings(resized, boxes)

        d = [{"encoding": encodings[0], "id": frame[0]}]
        data.extend(d)

    return data


def encode_and_change_ids(frames):
    data = []

    for frame in frames:
        image = np.frombuffer(frame[1], dtype=np.uint8)
        rgb = cv2.imdecode(image, cv2.IMREAD_COLOR)
        resized = cv2.resize(rgb, (350, 350))

        boxes = [(0, 350, 350, 0)]
        encodings = face_recognition.face_encodings(resized, boxes)

        d = [{"encoding": encodings[0], "id": -1 * frame[0]}]
        data.extend(d)

    return data


def save_person(conn, image):
    sql = "INSERT INTO person VALUES ({0}, ?)".format(str(image[0]))
    cursor = conn.cursor()
    cursor.execute(sql, (sqlite3.Binary(image[1]),))
    conn.commit()

    cursor.close()


def fetch_frames(conn):
    cursor = conn.cursor()
    cursor.execute("SELECT frame_id, frame_image FROM frame WHERE frame_ordered='0'")
    frames = cursor.fetchall()

    return frames

def main():
    print("[INFO] Connecting to SQLite...")
    database = "%s/database/test_database.db" % os.getcwd()
    conn = create_connection(database)

    print("[INFO] Quantifying faces...")
    frames = fetch_frames(conn)
    data = encode(frames)

    print("[INFO] Loading encodings...")
    encodings = load_encodings(data)
    frames_ids = load_frame_ids(data)

    print("[INFO] Clustering faces...")
    labels = cluster_faces(encodings)

    print("[INFO] Person ID attachment...")
    set_groups(conn, labels, frames_ids)

    print("[INFO] Checking if found people exist in PERSON table...")
    people = get_all_from_person(conn)
    people_data = encode_and_change_ids(people)

    ids_to_save = filter_ids(labels, frames_ids)
    detected_frames = find_frames_by_id(conn, ids_to_save)
    detections_data = encode(detected_frames)

    people_data.extend(detections_data)

    prepared_encodings = load_encodings(people_data)
    prepared_ids = load_frame_ids(people_data)

    labels = cluster_faces(prepared_encodings)
    ids = filter_ids(labels, prepared_ids)

    filtered = [id for id in ids if id > -1]

    images_to_save = [frame for frame in detected_frames if frame[0] in filtered]

    print("[INFO] Saving new people in the PERSON table...")
    for image in images_to_save:
        save_person(conn, image)

    print("[INFO] Marking frames as ORDERED...")
    for frame_id in frames_ids:
        update_frame(conn, (1, frame_id))

    close_connection(conn)
    print("[SUCCESS] Clustering process finished successfully.\n")


if __name__ == '__main__':
    main()
