package com.rosariob.crud.couchbase.repository;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.kv.ExistsResult;
import com.couchbase.client.java.transactions.TransactionResult;
import com.couchbase.client.java.transactions.Transactions;
import com.rosariob.crud.couchbase.entity.Customer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.couchbase.BucketDefinition;
import org.testcontainers.couchbase.CouchbaseContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;

@SpringBootTest(properties = { "application.bucket=customers", "application.collection=_default", "application.scope=_default" })
@Testcontainers
public class CustomerRepositoryTest {
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CouchbaseTemplate couchbaseTemplate;
    private static final String BUCKET_NAME = "customers";
    private static final String SCOPE_NAME = "_default";
    private static final String COLLECTION_NAME = "_default";
    private static Cluster cluster;
    private static Bucket bucket;
    private static Scope scope;
    private static Collection collection;
    private static String keySpace;

    private static final DockerImageName COUCHBASE_IMAGE_ENTERPRISE = DockerImageName
            .parse("couchbase:enterprise-7.1.4")
            .asCompatibleSubstituteFor("couchbase/server");
    @Container
    private static final CouchbaseContainer container = new CouchbaseContainer(COUCHBASE_IMAGE_ENTERPRISE)
            .withCredentials("Administrator", "password")
            .withBucket(new BucketDefinition(BUCKET_NAME).withPrimaryIndex(true).withFlushEnabled(true))
            .withStartupTimeout(Duration.ofMinutes(2));

    @DynamicPropertySource
    static void registerCouchbaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.couchbase.connectionString", container::getConnectionString);
        registry.add("spring.couchbase.username", container::getUsername);
        registry.add("spring.couchbase.password", container::getPassword);
    }

    @BeforeAll
    public static void setUp() {
        cluster = Cluster.connect(container.getConnectionString(),
                ClusterOptions.clusterOptions(container.getUsername(), container.getPassword()));
        cluster.waitUntilReady(Duration.ofMinutes(2));
        bucket = cluster.bucket(BUCKET_NAME);
        scope = bucket.scope(SCOPE_NAME);
        keySpace = String.join(".", BUCKET_NAME, SCOPE_NAME, COLLECTION_NAME);
        collection = scope.collection(COLLECTION_NAME);
    }

    @BeforeEach
    public void clearCollection() {
        Transactions transactions = couchbaseTemplate.getCouchbaseClientFactory().getCluster().transactions();
        transactions.run(ctx ->ctx.query("DELETE FROM " + keySpace));
    }
    @Test
    public void testFindById() {
            Customer alex = new Customer("customer1", "Alex", "Stone");
            Transactions transactions = couchbaseTemplate.getCouchbaseClientFactory().getCluster().transactions();
            transactions.run(ctx -> collection.insert(alex.getId(), alex));
            Customer customer = customerRepository.findById(alex.getId());
            Assertions.assertEquals(alex, customer);
    }

    @Test
    public void testFindAll() {
        Customer alex = new Customer("customer1", "Alex", "Stone");
        Customer jack = new Customer("customer2", "Jack", "Sparrow");
        List<Customer> customerList = List.of(alex, jack);
        Transactions transactions = couchbaseTemplate.getCouchbaseClientFactory().getCluster().transactions();
        customerList.forEach(customer ->
                transactions.run(ctx -> ctx.insert(collection, customer.getId(), customer)
                )
        );
        List<Customer> customers = customerRepository.findAll();
        Assertions.assertEquals(customerList, customers);
    }

    @Test
    public void testCreate(){
        Customer alex = new Customer("customer1","Alex", "Stone");
        customerRepository.create(alex);
        Customer result = collection.get(alex.getId()).contentAs(Customer.class);
        Assertions.assertEquals(alex, result);
    }

    @Test
    public void testUpdate(){
        Customer alex = new Customer("customer1","Alex", "Stone");
        Customer alex2 = new Customer("customer1","Alex", "Yellow");
        Transactions transactions = couchbaseTemplate.getCouchbaseClientFactory().getCluster().transactions();
        transactions.run(ctx -> ctx.insert(collection, alex.getId(), alex));
        customerRepository.update(alex2);
        Customer result = collection.get(alex2.getId()).contentAs(Customer.class);
        Assertions.assertEquals(alex2, result);
    }

    @Test
    public void testUpsert(){
        Customer alex = new Customer("customer1","Alex", "Stone");
        customerRepository.upsert(alex);
        Customer result = collection.get(alex.getId()).contentAs(Customer.class);
        Assertions.assertEquals(alex, result);
    }

    @Test
    public void testDeleteById(){
        Customer alex = new Customer("customer1","Alex", "Stone");
        Transactions transactions = couchbaseTemplate.getCouchbaseClientFactory().getCluster().transactions();
        transactions.run(ctx -> collection.insert(alex.getId(), alex));
        customerRepository.deleteById(alex.getId());
        ExistsResult exists = collection.exists(alex.getId());
        Assertions.assertFalse(exists.exists());
    }

    @Test
    public void testDeleteAll(){
        Customer alex = new Customer("customer1","Alex", "Stone");
        Customer jack = new Customer("customer2", "Jack", "Sparrow");
        List<Customer> customerList = List.of(alex, jack);
        Transactions transactions = couchbaseTemplate.getCouchbaseClientFactory().getCluster().transactions();
        customerList.forEach(customer -> transactions.run(ctx ->
                ctx.insert(collection, customer.getId(), customer))
        );
        customerRepository.deleteAll();
        ExistsResult exists = collection.exists(alex.getId());
        Assertions.assertFalse(exists.exists());
        ExistsResult exists2 = collection.exists(jack.getId());
        Assertions.assertFalse(exists2.exists());
    }
}