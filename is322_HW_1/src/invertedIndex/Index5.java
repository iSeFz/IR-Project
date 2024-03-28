package invertedIndex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author ehab
 */
public class Index5 {
    // Attributes to manipulate the inverted index
    public Map<Integer, SourceRecord> sources; // Store the doc_id and the file name.
    public HashMap<String, DictEntry> index; // The inverted index
    String currentDirectory; // Get the current working directory
    File rootDirectory; // Navigate up to the root directory of the project

    // Default constructor to initialize the attributes
    public Index5() {
        sources = new HashMap<Integer, SourceRecord>();
        index = new HashMap<String, DictEntry>();
        currentDirectory = System.getProperty("user.dir");
        rootDirectory = new File(currentDirectory).getParentFile().getParentFile().getParentFile();
    }

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

    // Output the dictionary of terms and document frequency
    public void printDictionary() {
        Iterator it = index.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            DictEntry dd = (DictEntry) pair.getValue();
            // print the term and the document frequency
            System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "]       =--> ");
            // print the posting list
            printPostingList(dd.pList);
        }
        // print the number of terms
        System.out.println("------------------------------------------------------");
        System.out.println("*** Number of terms = " + index.size());
    }

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

    // Search for a phrase in the index to get the result of the query
    public String find_24_01(String phrase) { // any mumber of terms non-optimized search
        String result = "";
        String[] words = phrase.split("\\W+");
        int len = words.length;

        Posting posting = null;
        int i = 0;
        while (i < len) {
            // If the word is a stop word, skip it
            if (stopWord(words[i].toLowerCase())) {
                i++;
                continue;
            }
            // If the word is not in the index, return an error message
            if (!index.containsKey(words[i].toLowerCase())) {
                return "Word not found in the index";
            }
            if (posting == null)
                posting = index.get(words[i].toLowerCase()).pList;
            // Otherwise intersect the posting list with the current word
            posting = intersect(posting, index.get(words[i].toLowerCase()).pList);
            i++;
        }
        while (posting != null) {
            // System.out.println("\t" + sources.get(num));
            result += "\t" + posting.docId + " - " + sources.get(posting.docId).title + " - "
                    + sources.get(posting.docId).length + "\n";
            posting = posting.next;
        }
        return result;
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

    // Load index from hard disk into memory
    public HashMap<String, DictEntry> load(String storageName) {
        try {
            String pathToStorage = rootDirectory.toPath().resolve("tmp11/rl/" + storageName).toString();
            // String pathToStorage = "/home/ehab/tmp11/rl/" + storageName;
            sources = new HashMap<Integer, SourceRecord>();
            index = new HashMap<String, DictEntry>();
            BufferedReader file = new BufferedReader(new FileReader(pathToStorage));
            String ln = "";
            int flen = 0;
            while ((ln = file.readLine()) != null) {
                if (ln.equalsIgnoreCase("section2")) {
                    break;
                }
                String[] ss = ln.split(",");
                int fid = Integer.parseInt(ss[0]);
                try {
                    System.out.println("**>>" + fid + " " + ss[1] + " " + ss[2].replace('~', ',') + " " + ss[3] + " ["
                            + ss[4] + "]   " + ss[5].replace('~', ','));

                    SourceRecord sr = new SourceRecord(fid, ss[1], ss[2].replace('~', ','), Integer.parseInt(ss[3]),
                            Double.parseDouble(ss[4]), ss[5].replace('~', ','));
                    // System.out.println("**>>"+fid+" "+ ss[1]+" "+ ss[2]+" "+ ss[3]+" ["+
                    // Double.parseDouble(ss[4])+ "] \n"+ ss[5]);
                    sources.put(fid, sr);
                } catch (Exception e) {

                    System.out.println(fid + "  ERROR  " + e.getMessage());
                    e.printStackTrace();
                }
            }
            while ((ln = file.readLine()) != null) {
                // System.out.println(ln);
                if (ln.equalsIgnoreCase("end")) {
                    break;
                }
                String[] ss1 = ln.split(";");
                String[] ss1a = ss1[0].split(",");
                String[] ss1b = ss1[1].split(":");
                index.put(ss1a[0], new DictEntry(Integer.parseInt(ss1a[1]), Integer.parseInt(ss1a[2])));
                String[] ss1bx; // posting
                for (int i = 0; i < ss1b.length; i++) {
                    ss1bx = ss1b[i].split(",");
                    if (index.get(ss1a[0]).pList == null) {
                        index.get(ss1a[0]).pList = new Posting(Integer.parseInt(ss1bx[0]), Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).pList;
                    } else {
                        index.get(ss1a[0]).last.next = new Posting(Integer.parseInt(ss1bx[0]),
                                Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).last.next;
                    }
                }
            }
            System.out.println("============= END LOAD =============");
            // printDictionary();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return index;
    }
}

