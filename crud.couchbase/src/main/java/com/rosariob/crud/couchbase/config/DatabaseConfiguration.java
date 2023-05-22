package com.rosariob.crud.couchbase.config;

import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Configuration
@EnableConfigurationProperties(CouchbaseProperties.class)
@EnableCouchbaseRepositories(basePackages = "com/rosariob/crud/couchbase.repository")
@EnableTransactionManagement
public class DatabaseConfiguration extends AbstractCouchbaseConfiguration {

    private final CouchbaseProperties couchbaseProperties;

    private final ApplicationProperties applicationProperties;
    public DatabaseConfiguration(CouchbaseProperties couchbaseProperties, ApplicationProperties applicationProperties) {
        this.couchbaseProperties = couchbaseProperties;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public String getConnectionString() {
        return couchbaseProperties.getConnectionString();
    }

    @Override
    public String getUserName() {
        return couchbaseProperties.getUsername();
    }

    @Override
    public String getPassword() {
        return couchbaseProperties.getPassword();
    }

    @Override
    public String getBucketName() {
        return applicationProperties.getBucket();
    }
}
