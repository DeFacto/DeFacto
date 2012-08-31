/**
 * 
 */
package org.aksw.defacto.search.cache;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.cache.Cache;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.search.result.DefaultSearchResult;
import org.aksw.defacto.search.result.SearchResult;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class LuceneSearchResultCache implements Cache<SearchResult> {
    
    /**
     * singleton
     */
    private Logger logger = Logger.getLogger(getClass());
    
    private final String INDEX_DIRECTORY = "resources/cache/websites";
    
    public static FSDirectory index;
    private static IndexWriter writer;
    private final Analyzer analyzer = new LowerCaseWhitespaceAnalyzer();

    public LuceneSearchResultCache() {

        createIndex();
    }
    
    /**
     * Opens and closes an index in the index directory
     */
    public void createIndex() {
        
        // create the index writer configuration and create a new index writer
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_36, analyzer);
        indexWriterConfig.setRAMBufferSizeMB(1024);
        indexWriterConfig.setOpenMode(isIndexExisting(INDEX_DIRECTORY) ? OpenMode.APPEND : OpenMode.CREATE);
        writer = createIndex(INDEX_DIRECTORY, indexWriterConfig);
        closeLuceneIndex();
    }
    
    /**
     * 
     */
    public void openIndexWriter() {
        
        // create the index writer configuration and create a new index writer
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_36, analyzer);
        indexWriterConfig.setRAMBufferSizeMB(1024);
        indexWriterConfig.setOpenMode(OpenMode.APPEND);
        writer = createIndex(INDEX_DIRECTORY, indexWriterConfig);
    }
    
    /**
     * 
     */
    public void closeIndexWriter() {
        
        closeLuceneIndex();
    }
    
    /**
     * Checks if an index exists at the given location.
     * 
     * @param indexDirectory - the directory of the index to be checked
     * @return true if the index exists, false otherwise
     */
    public boolean isIndexExisting(String indexDirectory) {
        
        try {
            
            return IndexReader.indexExists(FSDirectory.open(new File(indexDirectory)));
        }
        catch (IOException e) {
            
            e.printStackTrace();
            String error = "Check if index exists failed: " + indexDirectory;
            throw new RuntimeException(error, e);
        }
    }
    
    public void closeLuceneIndex() {
        
        try {
            
            writer.close();
        }
        catch (CorruptIndexException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * Create a new filesystem lucene index
     * 
     * @param absoluteFilePath - the path where to create/append the index
     * @param indexWriterConfig - the index write configuration
     * @return
     */
    private IndexWriter createIndex(String absoluteFilePath, IndexWriterConfig indexWriterConfig) {

        try {
            
            index = FSDirectory.open(new File(absoluteFilePath));
            return new IndexWriter(index, indexWriterConfig);
        }
        catch (CorruptIndexException e) {
            
            e.printStackTrace();
            throw new RuntimeException("Could not create index", e);
        }
        catch (LockObtainFailedException e) {
            
            e.printStackTrace();
            throw new RuntimeException("Could not create index", e);
        }
        catch (IOException e) {
            
            e.printStackTrace();
            throw new RuntimeException("Could not create index", e);
        }
    }

    @Override
    public boolean contains(String identifier) {

        try {

            TopScoreDocCollector collector = TopScoreDocCollector.create(1, true);
            IndexReader reader = IndexReader.open(index);
            IndexSearcher searcher = new IndexSearcher(reader); 
            searcher.search(new TermQuery(new Term("query", identifier)), collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            
            searcher.close();
            reader.close();
            
            if ( hits == null || hits.length == 0 ) return false;
        }
        catch (IOException e) {
            
            logger.error("Could not execute exists query for uri: " + identifier, e);
            e.printStackTrace();
        }
        
        return true;
    }
    
    public boolean contains(String identifier, IndexSearcher searcher) {

        try {

            TopScoreDocCollector collector = TopScoreDocCollector.create(1, true);
            searcher.search(new TermQuery(new Term("query", identifier)), collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            
            if ( hits == null || hits.length == 0 ) return false;
        }
        catch (IOException e) {
            
            logger.error("Could not execute exists query for uri: " + identifier, e);
            e.printStackTrace();
        }
        
        return true;
    }

    @Override
    public SearchResult getEntry(String identifier) {

        List<WebSite> websites = new ArrayList<WebSite>();
        MetaQuery metaQuery = null;
        Long hitCount = 0L;
        
        try {
            
            TopScoreDocCollector collector = TopScoreDocCollector.create(Defacto.DEFACTO_CONFIG.getIntegerSetting("boa", "NUMBER_OF_BOA_PATTERNS") * 2, true);
            IndexReader reader = IndexReader.open(index);
            IndexSearcher searcher = new IndexSearcher(reader); 
            searcher.search(new TermQuery(new Term("query", identifier)), collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            
            logger.info(String.format("Found %s documents in cache for : %s!", hits.length, identifier));
            
            for ( ScoreDoc hit : hits) {
                
                Document doc = searcher.doc(hit.doc);
                metaQuery = new MetaQuery(doc.get("query"));
                hitCount = Long.valueOf(doc.get("hits"));
                
                if ( !doc.get("url").isEmpty() ) { // empty cache hits should not become a website

                    WebSite site = new WebSite(metaQuery, doc.get("url"));
                    site.setRank(Integer.valueOf(doc.get("rank")));
                    site.setPageRank(Integer.valueOf(doc.get("pagerank")));
                    site.setText(doc.get("content"));
                    site.setTitle(doc.get("title"));
                    site.setCached(true);
                    websites.add(site);
                }
            }
            searcher.close();
            reader.close();
        }
        catch (CorruptIndexException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return new DefaultSearchResult(websites, hitCount, metaQuery);
    }

    @Override
    public SearchResult removeEntryByPrimaryKey(String primaryKey) {

        throw new RuntimeException("not yet implemented");    
    }

    @Override
    public boolean updateEntry(SearchResult object) {

        throw new RuntimeException("not yet implemented");    
    }

    @Override
    public List<SearchResult> addAll(List<SearchResult> listToAdd) {

        openIndexWriter();
        for ( SearchResult result : listToAdd ) {

            try {
                writer.addDocuments(searchResultToDocument(result));
            }
            catch (CorruptIndexException e) {

                e.printStackTrace();
                logger.error("Error writing articles to lucene database!", e);
            }
            catch (IOException e) {

                e.printStackTrace();
                logger.error("Error writing articles to lucene database!", e);
            }
        }
        closeIndexWriter();
        return listToAdd;
    }

    @Override
    public SearchResult add(SearchResult entry) {

        try {

            openIndexWriter();
            writer.addDocuments(searchResultToDocument(entry));
            closeIndexWriter();
        }
        catch (CorruptIndexException e) {

            e.printStackTrace();
            logger.error("Error writing articles to lucene database!", e);
        }
        catch (IOException e) {

            e.printStackTrace();
            logger.error("Error writing articles to lucene database!", e);
        }
        
        return entry;
    }

    private List<Document> searchResultToDocument(SearchResult entry) {

        List<Document> documents = new ArrayList<Document>();
        
        if ( entry.getWebSites().isEmpty() ) {
            
            Document luceneDocument = new Document();
            luceneDocument.add(new NumericField("hits", Field.Store.YES, true).setLongValue(entry.getTotalHitCount()));
            luceneDocument.add(new NumericField("rank", Field.Store.YES, true).setIntValue(-1));
            luceneDocument.add(new NumericField("pagerank", Field.Store.YES, true).setIntValue(-1));
            luceneDocument.add(new NumericField("created", Field.Store.YES, true).setLongValue(new Date().getTime()));
            luceneDocument.add(new Field("url", "", Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
            luceneDocument.add(new Field("title", "", Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
            luceneDocument.add(new Field("content", "", Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
            luceneDocument.add(new Field("query", entry.getQuery().toString(), Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
            documents.add(luceneDocument);
        }
        else {

            for ( WebSite site : entry.getWebSites() ) {
                
                Document luceneDocument = new Document();
                luceneDocument.add(new NumericField("hits", Field.Store.YES, true).setLongValue(entry.getTotalHitCount()));
                luceneDocument.add(new NumericField("rank", Field.Store.YES, true).setIntValue(site.getSearchRank()));
                luceneDocument.add(new NumericField("pagerank", Field.Store.YES, true).setIntValue(site.getPageRank()));
                luceneDocument.add(new NumericField("created", Field.Store.YES, true).setLongValue(new Date().getTime()));
                luceneDocument.add(new Field("url", site.getUrl(), Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
                luceneDocument.add(new Field("title", site.getTitle(), Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
                luceneDocument.add(new Field("content", site.getText(), Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
                luceneDocument.add(new Field("query", entry.getQuery().toString(), Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
                documents.add(luceneDocument);
            }
        }
        
        return documents;
    }

}
