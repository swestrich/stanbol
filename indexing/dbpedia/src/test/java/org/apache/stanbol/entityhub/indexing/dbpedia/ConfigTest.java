package org.apache.stanbol.entityhub.indexing.dbpedia;

import org.apache.stanbol.entityhub.indexing.core.EntityIterator;
import org.apache.stanbol.entityhub.indexing.core.IndexerFactory;
import org.apache.stanbol.entityhub.indexing.core.EntityIterator.EntityScore;
import org.apache.stanbol.entityhub.indexing.core.config.IndexingConfig;
import org.apache.stanbol.entityhub.indexing.core.normaliser.ScoreNormaliser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;

public class ConfigTest {

    private final static Logger log = LoggerFactory.getLogger(ConfigTest.class);
    
    /**
     * mvn copies the resources in "src/test/resources" to target/test-classes.
     */
    private static final String TEST_ROOT = "/target/test-classes";
    private static String  userDir;
    private static String testRoot;
    /**
     * The methods resets the "user.dir" system property
     */
    @BeforeClass
    public static void initTestRootFolder(){
        String baseDir = System.getProperty("basedir");
        if(baseDir == null){
            baseDir = System.getProperty("user.dir");
        }
        //store the current user.dir
        userDir = System.getProperty("user.dir");
        testRoot = baseDir+TEST_ROOT;
        log.info("ConfigTest Root : "+testRoot);
        //set the user.dir to the testRoot (needed to test loading of missing
        //configurations via classpath
        //store the current user.dir and reset it after the tests
        System.setProperty("user.dir", testRoot);
    }
    /**
     * resets the "user.dir" system property the the original value
     */
    @AfterClass
    public static void cleanup(){
        System.setProperty("user.dir", userDir);
    }
    @Test
    public void testEntityIdIteratorConfig(){
        IndexingConfig config = new IndexingConfig();
        EntityIterator iterator = config.getEntityIdIterator();
        ScoreNormaliser normaliser = config.getNormaliser();
        if(iterator.needsInitialisation()){
            iterator.initialise();
        }
        float lastScore = Float.MAX_VALUE;
        float lastNormalisedScore = 1f;
        while(iterator.hasNext()){
            EntityScore entity = iterator.next();
            assertNotNull(entity);
            assertNotNull(entity.id);
            assertNotNull(entity.score);
            //log.info("Entity: {}",entity);
            assertTrue(entity.id.startsWith("http://dbpedia.org/resource/"));
            float score = entity.score.floatValue();
            assertTrue(score > 0);
            assertTrue(score <=lastScore);
            lastScore = score;
            Float normalisedScore = normaliser.normalise(entity.score);
            assertNotNull(normalisedScore);
            float nScore = normalisedScore.floatValue();
            assertTrue(nScore <= lastNormalisedScore);
            if(score < 2){ //the value of "min-score" in minIncomming
                log.info("score="+score+" nScore="+nScore);
                assertTrue(nScore < 0);
                return;
            } else {
                assertTrue(nScore > 0);
            }
        }
    }
}
