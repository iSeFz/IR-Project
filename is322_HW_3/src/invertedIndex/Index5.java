/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package invertedIndex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Math.log10;
import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.io.PrintWriter;

/**
 *
 * @author ehab
 */
public class Index5 {

    // --------------------------------------------
    int N = 10;
    // --------------------------------------------
    // Attributes to manipulate the inverted index
    public Map<Integer, SourceRecord> sources; // Store the doc_id and the file name.
    public HashMap<String, DictEntry> index; // The inverted index
    String currentDirectory; // Get the current working directory
    File rootDirectory; // Navigate up to the root directory of the project

    SortedScore sortedScore;

    
    // Default constructor to initialize the attributes
    public Index5() {
        sources = new HashMap<Integer, SourceRecord>();
        index = new HashMap<String, DictEntry>();
        currentDirectory = System.getProperty("user.dir");
        rootDirectory = new File(currentDirectory);
    }

    public void setN(int n) {
        N = n;
    }

    // ###################################################################
    // Output the posting list of a term
    public void printPostingList(Posting p) {
        // Iterator<Integer> it2 = hset.iterator();
        System.out.print("[");
        while (p != null) {
            // print the document id
            System.out.print("" + p.docId);
            p = p.next;
            // add comma if not the last element
            if (p != null) {
                System.out.print(", ");
            }
        }
        System.out.println("]");
    }

    // ---------------------------------------------
    public void printDictionary() {
        Iterator it = index.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            DictEntry dd = (DictEntry) pair.getValue();
            System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "]       =--> ");
            printPostingList(dd.pList);
        }
        System.out.println("------------------------------------------------------");
        System.out.println("*** Number of terms = " + index.size());
    }

    // ----------------------------------------------------------------------------
    public int buildIndex(String ln, int fid) {
        int flen = 0;

        String[] words = ln.split("\\W+");
        // String[] words = ln.replaceAll("(?:[^a-zA-Z0-9 -]|(?<=\\w)-(?!\\S))", "
        // ").toLowerCase().split("\\s+");
        flen += words.length;
        for (String word : words) {
            word = word.toLowerCase();
            if (stopWord(word)) {
                continue;
            }
            word = stemWord(word);
            // check to see if the word is not in the dictionary
            if (!index.containsKey(word)) {
                index.put(word, new DictEntry());
            }
            // add document id to the posting list
            if (!index.get(word).postingListContains(fid)) {
                index.get(word).doc_freq += 1; // set doc freq to the number of doc that contain the term
                if (index.get(word).pList == null) {
                    index.get(word).pList = new Posting(fid);
                    index.get(word).last = index.get(word).pList;
                } else {
                    index.get(word).last.next = new Posting(fid);
                    index.get(word).last = index.get(word).last.next;
                }
            } else {
                index.get(word).last.dtf += 1;
            }
            // set the term_fteq in the collection
            index.get(word).term_freq += 1;
            if (word.equalsIgnoreCase("lattice")) {

                System.out.println("  <<" + index.get(word).getPosting(1) + ">> " + ln);
            }

        }
        return flen;
    }

    // ###############################################################################
    // Build index from a list of files from disk not from the internet
    public void buildIndex(String[] files) {
        // `fid` will be the doc_id, it will be incremented for each file to simulate
        // its id
        int fid = 0;
        // iterate over the files' names
        for (String fileName : files) {
            try (BufferedReader file = new BufferedReader(new FileReader(fileName))) {
                // if the file is not in the sources map, add it
                if (!sources.containsKey(fileName)) {
                    sources.put(fid, new SourceRecord(fid, fileName, fileName, "notext"));
                }
                String ln;
                int flen = 0;
                // read the file line by line
                while ((ln = file.readLine()) != null) {
                    // index the line
                    flen += indexOneLine(ln, fid);
                }

                // set the number of words in the file
                sources.get(fid).length = flen;

            } catch (IOException e) {
                System.out.println("File " + fileName + " not found. Skip it");
            }
            fid++;
        }
    }

    // ##############################################################################
    // Manipulate terms, stemming, stop words, and build the index
    public int indexOneLine(String ln, int fid) {
        int flen = 0;

        // split the line into words
        String[] words = ln.split("\\W+");

        // get the number of words in the line
        flen += words.length;
        for (String word : words) {
            // convert the word to lowercase to make the search case-insensitive
            word = word.toLowerCase();

            // skip stop words
            if (stopWord(word)) {
                continue;
            }

            // stem the word to get its root
            word = stemWord(word);

            // check to see if the word is not in the dictionary
            // if not add it
            if (!index.containsKey(word)) {
                index.put(word, new DictEntry());
            }

            // add document id to the posting list
            if (!index.get(word).postingListContains(fid)) {
                index.get(word).doc_freq += 1; // set doc freq to the number of doc that contain the term
                if (index.get(word).pList == null) {
                    index.get(word).pList = new Posting(fid);
                    index.get(word).last = index.get(word).pList;
                } else {
                    index.get(word).last.next = new Posting(fid);
                    index.get(word).last = index.get(word).last.next;
                }
            } else {
                index.get(word).last.dtf += 1;
            }

            // set the term_fteq in the collection
            index.get(word).term_freq += 1;
            if (word.equalsIgnoreCase("lattice")) {
                System.out.println("  <<" + index.get(word).getPosting(1) + ">> " + ln);
            }

        }
        return flen;
    }

    // Check for stop words that are repeated & not useful for searching
    boolean stopWord(String word) {
        if (word.equals("the") || word.equals("to") || word.equals("be") || word.equals("for") || word.equals("from")
                || word.equals("in") || word.equals("a") || word.equals("into") || word.equals("by")
                || word.equals("or") || word.equals("and") || word.equals("that")) {
            return true;
        }
        if (word.length() < 2) {
            return true;
        }
        return false;

    }
    // ----------------------------------------------------------------------------

    String stemWord(String word) { // skip for now
        return word;
        // Stemmer s = new Stemmer();
        // s.addString(word);
        // s.stem();
        // return s.toString();
    }

    // Intersect two posting lists & get the resulting common docs
    Posting intersect(Posting pL1, Posting pL2) {
        /// **** -1- complete after each comment ****
        // INTERSECT ( p1 , p2 )
        // 1 answer ← {}
        // Posting answer = null;
        // Posting last = null;
        // 2 while p1 != NIL and p2 != NIL

        // 3 do if docID ( p 1 ) = docID ( p2 )

        // 4 then ADD ( answer, docID ( p1 ))
        // answer.add(pL1.docId);

        // 5 p1 ← next ( p1 )
        // 6 p2 ← next ( p2 )

        // 7 else if docID ( p1 ) < docID ( p2 )

        // 8 then p1 ← next ( p1 )
        // 9 else p2 ← next ( p2 )

        // 10 return answer

        Posting answer = null;
        Posting last = null;
        while (pL1 != null && pL2 != null) {
            if (pL1.docId == pL2.docId) {
                if (answer == null) {
                    answer = new Posting(pL1.docId, pL1.dtf);
                    last = answer;
                } else {
                    last.next = new Posting(pL1.docId, pL1.dtf);
                    last = last.next;
                }
                pL1 = pL1.next;
                pL2 = pL2.next;
            } else if (pL1.docId < pL2.docId) {
                pL1 = pL1.next;
            } else {
                pL2 = pL2.next;
            }
        }
        return answer;
    }

    // Bubble sort the terms in the dictionary
    String[] sort(String[] words) {
        boolean sorted = false;
        String sTmp;
        // Loop while the array is not sorted
        while (!sorted) {
            sorted = true;
            for (int i = 0; i < words.length - 1; i++) {
                int compare = words[i].compareTo(words[i + 1]);
                if (compare > 0) {
                    sTmp = words[i];
                    words[i] = words[i + 1];
                    words[i + 1] = sTmp;
                    sorted = false;
                }
            }
        }
        return words;
    }

    // ==========================================================
    public String find_07a(String phrase) {
        System.out.println("-------------------------  find_07 -------------------------");

        String result = "";
        String[] words = phrase.split("\\W+");
        Set<Integer> visitedDocs = new TreeSet<>();
        sortedScore = new SortedScore();

        double[] scores = new double[N];
        double[] Scores = new double[N];

        // 1 float Scores[N] = 0
        for (int i = 0; i < N; i++) {
            scores[i] = 0;
        }
        // 2 Initialize Length[N]
        double[] length = new double[N];
        // 3 for each query term t
        for (String term : words) {
            // 4 do calculate w t, q and fetch postings list for t
            term = term.toLowerCase();
            DictEntry wordDictEntry = index.get(term);
            if (wordDictEntry == null) {
                continue;
            }
            int tdf = index.get(term).doc_freq; // number of documents that contains the term
            // int ttf = index.get(term).term_freq; //
            // 4.a compute idf
            double idf = log10(N / (double) tdf); // can be computed earlier
            // 5 for each pair(doc_id, dtf ) in postings list
            Posting p = index.get(term).pList;
            while (p != null) {
                visitedDocs.add(p.docId);
                // 6 add the term score for (term/doc) to score of each doc
                scores[p.docId] += (1 + log10((double) p.dtf)) * idf;
                // Normalize for the length of the doc
                length[p.docId] += p.dtf * p.dtf;
                p = p.next;
            }
        }
        // 7 Read the array Length[d]
        // 8 for each d
        for (Integer docId : visitedDocs) {
            // 9 do Scores[d] = Scores[d]/Length[d]
            Scores[docId] = scores[docId] / length[docId];
            sortedScore.insertScoreRecord(Scores[docId], sources.get(docId).URL, sources.get(docId).title, "");
        }
        // 10 return Top K components of Scores[]
        result = sortedScore.printScores();
        return result;
    }

    ///// ---------------------------------
    public void searchLoop() {

        String phrase;
        do {
            System.out.println("Print search phrase: ");
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            try {
                phrase = in.readLine();
                find_07a(phrase);
                // find_08(phrase);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        } while (!phrase.isEmpty());

    }

    // Store the inverted index to the hard disk
    public void store(String storageName) {
        try {
            // TODO: Change the path to the storage file to the correct path
            String pathToStorage = rootDirectory.toPath().resolve("tmp11/rl/" + storageName).toString();

            // String pathToStorage = "/home/ehab/tmp11/rl/" + storageName;
            Writer wr = new FileWriter(pathToStorage);
            for (Map.Entry<Integer, SourceRecord> entry : sources.entrySet()) {
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue().URL + ", Value = "
                        + entry.getValue().title + ", Value = " + entry.getValue().text);
                wr.write(entry.getKey().toString() + ",");
                wr.write(entry.getValue().URL.toString() + ",");
                wr.write(entry.getValue().title.replace(',', '~') + ",");
                wr.write(entry.getValue().length + ","); // String formattedDouble = String.format("%.2f", fee );
                wr.write(String.format("%4.4f", entry.getValue().norm) + ",");
                wr.write(entry.getValue().text.toString().replace(',', '~') + "\n");
            }
            wr.write("section2" + "\n");

            Iterator it = index.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                DictEntry dd = (DictEntry) pair.getValue();
                // System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "] <" +
                // dd.term_freq + "> =--> ");
                wr.write(pair.getKey().toString() + "," + dd.doc_freq + "," + dd.term_freq + ";");
                Posting p = dd.pList;
                while (p != null) {
                    // System.out.print( p.docId + "," + p.dtf + ":");
                    wr.write(p.docId + "," + p.dtf + ":");
                    p = p.next;
                }
                wr.write("\n");
            }
            wr.write("end" + "\n");
            wr.close();
            System.out.println("=============END STORE=============");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Check if the storage file exists
    public boolean storageFileExists(String storageName) {
        File f = rootDirectory.toPath().resolve("tmp11/rl/" + storageName).toFile();
        System.out.println("storageFileExists: " + f.toString());

        if (f.exists() && !f.isDirectory())
            return true;
        return false;

    }

    // Create the storage file with the specified name
    public void createStore(String storageName) {
        try {
            String pathToStorage = rootDirectory.toPath().resolve("tmp11/" + storageName).toString();
            // String pathToStorage = "/home/ehab/tmp11/" + storageName;
            Writer wr = new FileWriter(pathToStorage);
            wr.write("end" + "\n");
            wr.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

// =====================================================================
