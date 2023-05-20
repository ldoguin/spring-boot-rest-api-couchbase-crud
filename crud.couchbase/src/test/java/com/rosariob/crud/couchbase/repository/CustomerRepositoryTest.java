package com.rosariob.crud.couchbase.repository;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.kv.ExistsResult;
import com.couchbase.client.java.transactions.Transactions;
import com.rosariob.crud.couchbase.entity.Customer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.couchbase.core.CouchbaseTemplate;

import java.time.Duration;
import java.util.List;

@SpringBootTest(properties = { "application.bucket=customers", "application.collection=_default", "application.scope=_default" })
//@Testcontainers
public class CustomerRepositoryTest {
    @Autowired
    private CustomerRepositoryImpl customerRepository;

    @Autowired
    private CouchbaseTemplate couchbaseTemplate;

    private static final String BUCKET_NAME = "customers";
    private static final String SCOPE_NAME = "_default";
    private static final String COLLECTION_NAME = "_default";
    private static ClusterEnvironment env;
    private static Cluster cluster;
    private static Bucket bucket;
    private static Scope scope;
    private static Collection collection;
    private static String keySpace;

    /*@Container
    private static final CouchbaseContainer container = new CouchbaseContainer(COUCHBASE_IMAGE_ENTERPRISE)
            .withCredentials("Administrator", "password")
            .withServiceQuota(CouchbaseService.SEARCH, 1024)
            .withBucket(new BucketDefinition(BUCKET_NAME).withPrimaryIndex(true)*//*.withFlushEnabled(true).withReplicas(1)*//*)
            .withExposedPorts(8091, 8092, 8093, 8094, 11210)
            .withStartupTimeout(Duration.ofMinutes(15));*/

    @BeforeAll
    public static void setUp() {

        /*container.start();

        Set<SeedNode> seedNodes = Set.of(
                SeedNode.create("127.0.0.1")
                        .withKvPort(container.getMappedPort(11210))
                        .withManagerPort(container.getMappedPort(8091))
        );*/

        /*TransactionKeyspace keyspace = TransactionKeyspace.create(BUCKET_NAME, SCOPE_NAME, COLLECTION_NAME);
        env = ClusterEnvironment.builder().transactionsConfig(TransactionsConfig
                .durabilityLevel(DurabilityLevel.PERSIST_TO_MAJORITY)
                .metadataCollection(keyspace))
                .build();*/

        /*cluster = Cluster.connect(seedNodes,
                ClusterOptions.clusterOptions(container.getUsername(), container.getPassword()).environment(env));*/

        /*cluster = Cluster.connect(container.getConnectionString(),
                ClusterOptions.clusterOptions(container.getUsername(), container.getPassword()).environment(env));*/

        cluster = Cluster.connect("couchbase://localhost", "Administrator", "password");


        cluster.waitUntilReady(Duration.ofMinutes(2));


        bucket = cluster.bucket(BUCKET_NAME);
        scope = bucket.scope(SCOPE_NAME);
        keySpace = String.join(".", BUCKET_NAME, SCOPE_NAME, COLLECTION_NAME);
        collection = scope.collection(COLLECTION_NAME);
    }

    @AfterAll
    public static void tearDown(){
        cluster.close();
        /*env.shutdown();
        container.stop();*/
    }

    @BeforeEach
    public void clearCollection() {
        Transactions transactions = couchbaseTemplate.getCouchbaseClientFactory().getCluster().transactions();
        transactions.run(ctx ->
                scope.query("DELETE FROM " + keySpace)
        );
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
    public void testFindAll() throws InterruptedException {
        Customer alex = new Customer("customer1", "Alex", "Stone");
        Customer jack = new Customer("customer2", "Jack", "Sparrow");
        List<Customer> customerList = List.of(alex, jack);
        Transactions transactions = couchbaseTemplate.getCouchbaseClientFactory().getCluster().transactions();
        transactions.run(ctx ->
                customerList.forEach(customer -> collection.insert(customer.getId(), customer))
        );
        List<Customer> customers = customerRepository.findAll();
        Assertions.assertEquals(customerList, customers);
    }

    @Test
    public void testCreate(){
        Customer alex = new Customer("customer1","Alex", "Stone");
        Transactions transactions = couchbaseTemplate.getCouchbaseClientFactory().getCluster().transactions();
        transactions.run(ctx -> customerRepository.create(alex));
        Customer result = collection.get(alex.getId()).contentAs(Customer.class);
        Assertions.assertEquals(alex, result);
    }

    @Test
    public void testUpdate(){
        Customer alex = new Customer("customer1","Alex", "Stone");
        Customer alex2 = new Customer("customer1","Alex", "Yellow");
        Transactions transactions = couchbaseTemplate.getCouchbaseClientFactory().getCluster().transactions();
        transactions.run(ctx -> collection.insert(alex.getId(), alex));
        transactions.run(ctx -> customerRepository.update(alex2));
        Customer result = collection.get(alex2.getId()).contentAs(Customer.class);
        Assertions.assertEquals(alex2, result);
    }

    @Test
    public void testUpsert(){
        Customer alex = new Customer("customer1","Alex", "Stone");
        Transactions transactions = couchbaseTemplate.getCouchbaseClientFactory().getCluster().transactions();
        transactions.run(ctx -> customerRepository.upsert(alex));
        Customer result = collection.get(alex.getId()).contentAs(Customer.class);
        Assertions.assertEquals(alex, result);
    }

    @Test
    public void testDeleteById(){
        Customer alex = new Customer("customer1","Alex", "Stone");
        Transactions transactions = couchbaseTemplate.getCouchbaseClientFactory().getCluster().transactions();
        transactions.run(ctx -> collection.insert(alex.getId(), alex));
        transactions.run(ctx -> customerRepository.deleteById(alex.getId()));
        ExistsResult exists = collection.exists(alex.getId());
        Assertions.assertFalse(exists.exists());
    }

    @Test
    public void testDeleteAll(){
        Customer alex = new Customer("customer1","Alex", "Stone");
        Customer jack = new Customer("customer2", "Jack", "Sparrow");
        List<Customer> customerList = List.of(alex, jack);
        Transactions transactions = couchbaseTemplate.getCouchbaseClientFactory().getCluster().transactions();
        transactions.run(ctx ->
                customerList.forEach(customer -> collection.insert(customer.getId(), customer))
        );
        transactions.run(ctx -> customerRepository.deleteAll());

        ExistsResult exists = collection.exists(alex.getId());
        Assertions.assertFalse(exists.exists());
        ExistsResult exists2 = collection.exists(jack.getId());
        Assertions.assertFalse(exists2.exists());
    }
}