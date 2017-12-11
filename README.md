# aztec-text-analysis-tools
Libraries for text analysis (e.g., entry similarity evaluation, text summarization, etc.)


# To reindex files
* Download solr database resources to index. (i.e. http://dev.aztec.io:8983/solr/BD2K/select?q=*%3A*&rows=100000&wt=json&indent=true)
* Download libraries and extract to src/lib folder. Download them from original websites, or from https://drive.google.com/file/d/1hpbkpzOUONHogpMe4kjEE5MnDUI3rlK_/view?usp=sharing.
* Move tfidf.data, tfidfk.data, and keywords.data out of the data folder. Keep it backed up somewhere just in case.
* Run the main function for Search.java. This will create the three files above.
* Copy the three files to Aztec-Data in the home directory of the server.