package file

enum class FilePath(val file : String) {
    USER_DIR(System.getProperty("user.dir")),
    ASSETS("${USER_DIR.file}/src/main/java/assets"),
    EMOTION_MODEL_PATH("${ASSETS.file}/emotion_model.hdf5"),
    SQLITE_DATABASE("${USER_DIR.file}/database/test_database.db"),
    DATABASE_URL("jdbc:sqlite:${SQLITE_DATABASE.file}"),
    SQLITE_DRIVER("org.sqlite.JDBC"),
    FACES_DATA("${USER_DIR.file}/cluster_faces"),
    ENCODE_FACES("${FACES_DATA.file}/encode_faces.py"),
    CLUSTER_FACES("${FACES_DATA.file}/cluster_faces.py"),
    CAFFEE_MODEL("${ASSETS.file}/res10_300x300_ssd_iter_140000.caffemodel"),
    PROTOBUF("${ASSETS.file}/deploy.prototxt.txt"),
    DEMO("${ASSETS.file}/demo.mp4"),
    EMOTICON_SAD("${ASSETS.file}/sad.png"),
    EMOTICON_ANGRY("${ASSETS.file}/angry.png")
}