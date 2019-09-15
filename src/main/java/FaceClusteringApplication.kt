import java.io.BufferedReader

class FaceClusteringApplication : Application {
    override fun run(addressString: String) {

        val clusterCommand = "/usr/local/bin/python3 " + file.FilePath.CLUSTER_FACES.file

        val process = Runtime.getRuntime().exec(clusterCommand)

        val allText = process.inputStream.bufferedReader().use(BufferedReader::readText)
        print(allText)

        process.waitFor()

    }
}