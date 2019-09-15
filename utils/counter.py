import glob

emotions = ["anger", "anxiety", "contempt", "despair", "tiredness", "disgust", "fear", "fun", "happiness", "horror",
            "neutral", "pain", "pensive", "sadness", "surprise", "uncertain"]

for emotion in emotions:
    files = glob.glob("%s/*" % emotion)
    size = len(files)
    with open('cnt.txt', 'a') as the_file:
        the_file.write("%s: \t %s \n" % (emotion, size))
