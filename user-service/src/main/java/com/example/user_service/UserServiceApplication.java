package com.example.user_service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Service;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;

import lombok.RequiredArgsConstructor;



@Table("users")
class User {

    @PrimaryKey
    private UUID id;
    private String name;
    private String email;

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // toString
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}


interface UserRepository extends CassandraRepository<User, UUID> {
    // CassandraRepository provides basic CRUD operations out of the box
}



@Service
class UserService {

     @Autowired
    private CassandraTemplate cassandraTemplate;

    public void saveUserWithConsistency(User user) {

        SimpleStatement statement = SimpleStatement.builder("INSERT INTO users (id, name, email) VALUES (?, ?, ?)")
                .setConsistencyLevel(ConsistencyLevel.TWO)
                .addPositionalValues(user.getId(), user.getName(), user.getEmail())
                .build();

        cassandraTemplate.getCqlOperations().execute(statement);
    }
    

    //  @Autowired
    // private CassandraOperations cassandraOperations;

    // public void customInsert(User user) {
    //     SimpleStatement stmt = SimpleStatement.builder("INSERT INTO users (id, name, email) VALUES (?, ?, ?)")
    //             .setConsistencyLevel(ConsistencyLevel.ALL) // Set specific consistency level for this query
    //             .addPositionalValues(user.getId(), user.getName(), user.getEmail())
    //             .build();

    //     cassandraOperations.execute(stmt);
    // }

}



@SpringBootApplication
@RequiredArgsConstructor
public class UserServiceApplication implements CommandLineRunner {

    private final UserRepository userRepository;

    private final UserService userService;

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {

        // 1. Create and Save a new User
        User newUser = new User();
        newUser.setId(UUID.randomUUID());
        newUser.setName("Nag");
        newUser.setEmail("nag@example.com");

        //userRepository.save(newUser);
        userService.saveUserWithConsistency(newUser);
        System.out.println("User created: " + newUser);

        // // 2. Fetch all Users
        // Iterable<User> users = userRepository.findAll();
        // System.out.println("All Users:");
        // users.forEach(user -> System.out.println(user));

        // // 3. Find User by ID
        User foundUser = userRepository.findById(newUser.getId()).orElse(null);
        System.out.println("Found User: " + foundUser);

        // // 4. Update User
        // foundUser.setEmail("nag@updated.com");
        // userRepository.save(foundUser);
        // System.out.println("Updated User: " + foundUser);

        // // 5. Delete User
        //userRepository.delete(foundUser);
        //System.out.println("Deleted User: " + foundUser);

    }

}
