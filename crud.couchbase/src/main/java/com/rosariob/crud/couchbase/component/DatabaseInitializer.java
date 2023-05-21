package com.rosariob.crud.couchbase.component;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.manager.bucket.BucketSettings;
import com.couchbase.client.java.manager.bucket.BucketType;
import com.couchbase.client.java.manager.collection.CollectionManager;
import com.couchbase.client.java.manager.collection.CollectionSpec;
import com.couchbase.client.java.manager.collection.ScopeSpec;
import com.couchbase.client.java.manager.query.CollectionQueryIndexManager;
import com.rosariob.crud.couchbase.config.ApplicationProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.couchbase.CouchbaseClientFactory;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DatabaseInitializer {

    @Autowired
    private CouchbaseTemplate couchbaseTemplate;

    @Autowired
    private ApplicationProperties applicationProperties;

    @PostConstruct
    public void init() {
        //Creates bucket, scope and collection if they do not exist
        CouchbaseClientFactory couchbaseClientFactory = couchbaseTemplate.getCouchbaseClientFactory();
        Cluster cluster = couchbaseClientFactory.getCluster();
        String bucketName = applicationProperties.getBucket();
        String scopeName = applicationProperties.getScope();
        String collectionName = applicationProperties.getCollection();
        if(!cluster.buckets().getAllBuckets().containsKey(bucketName)) {
            //create bucket
            cluster.buckets()
                    .createBucket(BucketSettings.create(bucketName)
                            .bucketType(BucketType.COUCHBASE)
                            .ramQuotaMB(120)
                            .numReplicas(1)
                            .replicaIndexes(true)
                            .flushEnabled(true));
            log.info("Created bucket " + bucketName);
        }

        Bucket bucket = couchbaseClientFactory.getBucket();
        bucket.waitUntilReady(Duration.ofSeconds(10));
        CollectionManager collectionManager = bucket.collections();

        Set<String> scopesNames = collectionManager.getAllScopes().stream().map(ScopeSpec::name).collect(Collectors.toSet());
        if(!scopesNames.contains(scopeName)){
            // create scope
            collectionManager.createScope(scopeName);
            log.info("Created scope " + bucketName);
        }
        ScopeSpec scopeSpec = collectionManager.getScope(scopeName);

        Set<String> collectionsNames = scopeSpec.collections().stream().map(CollectionSpec::name).collect(Collectors.toSet());
        if(!collectionsNames.contains(collectionName)){
            // create collection
            collectionManager.createCollection(CollectionSpec.create(collectionName, scopeName));
            log.info("Created collection " + collectionName);
        }

        Collection collection = couchbaseClientFactory.withScope(scopeName).getCollection(collectionName);
        CollectionQueryIndexManager collectionQueryIndexManager = collection.queryIndexes();

        if(collectionQueryIndexManager.getAllIndexes().size() == 0){
            // create primary index
            collectionQueryIndexManager.createPrimaryIndex();
            log.info("Created primary index on  " + bucketName);
        }
    }
}
