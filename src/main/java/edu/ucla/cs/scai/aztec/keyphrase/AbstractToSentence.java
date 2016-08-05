/**
 * Created by Xinxin on 7/28/2016.
 * currently not in use.
 */
package edu.ucla.cs.scai.aztec.keyphrase;

import java.io.*;
import java.util.*;

public class AbstractToSentence {
    public static void main(String[] args) {
        System.out.println("Enter the file path");
        Scanner scan = new Scanner(System.in);
        String path = scan.nextLine();//Files present in this path will be analysed to count frequency
        File directory = new File(path);
        File[] listOfFiles = directory.listFiles();//To get the list of file-names found at the "directoy"
        BufferedReader br = null;
        String sentences[] = null;
        String line;
        String files;
        try {
            File fileOne = new File("absSentence");
            FileOutputStream fos = new FileOutputStream(fileOne);
            Writer writer = new BufferedWriter(new OutputStreamWriter(fos, "utf-8"));
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    files = file.getName();
                    try {
                        if (files.endsWith(".txt") || files.endsWith(".TXT")) {  //Checks whether an file is an text file
                            br = new BufferedReader(new FileReader(files));      //creates an Buffered Reader to read the contents of the file
                            while ((line = br.readLine()) != null) {
                                line = line.toLowerCase();
                                sentences = line.split("\\.");                   //Splits the abstract with "period" as an delimeter
                                for (String sen : sentences) {
                                    writer.write(sen);
                                    writer.write("\n");
                                }
                            }
                            br.close();
                        }
                    } catch (NullPointerException | IOException e) {
                        System.out.println("I can't read your files:" + e);
                    }

                }
            }
            fos.close();
            writer.close();
        } catch (NullPointerException | IOException e) {
            System.out.println("I can't wirte the file:" + e);
        }

    }
}
