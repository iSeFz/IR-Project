package invertedIndex;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author ehab
 */
public class Test {
    public static void main(String args[]) throws IOException {
        Index5 index = new Index5();

        // Get the current working directory
        String currentDirectory = System.getProperty("user.dir");

        // Navigate up to the root directory of the project
        File rootDirectory = new File(currentDirectory).getParentFile().getParentFile().getParentFile();

        // Change directory to the collection directory
        String files = rootDirectory.toPath().resolve("tmp11/rl/collection/").toString();

        // String[] list() ==> Returns an array of strings naming the files and
        // Directories in the directory denoted by this abstract pathname.
        String[] fileList = new File(files).list();

        fileList = index.sort(fileList); // Sort the index
        index.N = fileList.length;       // Store the number of documents in the collection

        // Add the path to the file names
        for (int i = 0; i < fileList.length; i++)
            fileList[i] = files + fileList[i];

        // Build the index and save it to disk
        index.buildIndex(fileList);
        index.store("index");
        index.printDictionary();

        String test3 = "data  should plain greatest comif"; // data  should plain greatest comif
        System.out.println("Boo0lean Model result = \n" + index.find_24_01(test3));

        // Take input query from the user
        String phrase = "";
        do {
            System.out.println("Print search phrase: ");
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            phrase = in.readLine();
            // Find the search phrase in the index
            index.find_24_01(phrase);
        } while (!phrase.isEmpty());
    }
}
