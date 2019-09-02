import data.implementation.DatabaseManager
import java.io.BufferedReader

class FaceClusteringApplication : Application {
    override fun run(addressString: String) {
        val database = DatabaseManager.getInstance()
        database.closeConnection()

        val command = "/usr/local/bin/python3 " + file.FilePath.ENCODE_FACES.file
        val param = " -d " + file.FilePath.SQLITE_DATABASE.file

        print(param)
        val process = Runtime.getRuntime().exec(command + param)

        val allText = process.inputStream.bufferedReader().use(BufferedReader::readText)
        print(allText)

        process.waitFor()
    }
}