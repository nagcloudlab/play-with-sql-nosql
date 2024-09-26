package com.example.hotels_service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.data.UdtValue;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.convert.CassandraCustomConversions;
import org.springframework.data.cassandra.core.mapping.*;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

//----------------------------
// Domain Layer
//----------------------------

@UserDefinedType("address")
@Data
class Address {
	private String street;
	private String city;
	private String state_or_province;
	private String postal_code;
	private String country;

	// Getters and Setters
}

@Table("hotels")
@Data
class Hotel {

    @PrimaryKey
    private String id;
    private String name;
    private String phone;
    @CassandraType(type = CassandraType.Name.UDT, userTypeName = "address")
    private Address address;
    private Set<String> pois; // Points of Interest

    // Getters and Setters

}


//----------------------------
// Data/Repository Layer
//----------------------------

interface HotelRepository extends CassandraRepository<Hotel, String> {
    // Find hotels by POI name from hotels_by_poi table
    @Query("SELECT hotel_id, name, phone, address FROM hotels_by_poi WHERE poi_name = ?0")
    List<Hotel> findHotelsByPoiName(String poiName);
}


//----------------------------
// Service Layer
//----------------------------

@Service
class HotelService {

    @Autowired
    private HotelRepository hotelRepository;

    // Write a hotel to the database
    public Hotel saveHotel(Hotel hotel) {
        return hotelRepository.save(hotel);
    }

    // Read a hotel from the database by ID
    public Hotel getHotelById(String hotelId) {
        return hotelRepository.findById(hotelId).orElse(null);
    }

    // Find hotels by POI name
    public List<Hotel> findHotelsByPoiName(String poiName) {
        return hotelRepository.findHotelsByPoiName(poiName);
    }
   
}


//----------------------------
// REST API Layer
//----------------------------

@RestController
@RequestMapping("/hotels")
@CrossOrigin(origins = "*")
class HotelController {

	@Autowired
    private HotelService hotelService;
    
    @Autowired
    private CassandraTemplate cassandraTemplate;

	// Endpoint to create or update a hotel
	@PostMapping
    public ResponseEntity<Hotel> createOrUpdateHotel(@RequestBody Hotel hotel) {
		
        System.out.println(hotel);
        
        Hotel savedHotel = hotelService.saveHotel(hotel);
        
        /*
          CREATE TABLE hotel.hotels_by_poi (
            poi_name text,
            hotel_id text,
            address frozen<address>,
            name text,
            phone text,
            PRIMARY KEY (poi_name, hotel_id)
        )
         * 
         */
        // Insert into hotels_by_poi table for each POI in the hotel using cassandraTemplate with statement
        for (String poi : hotel.getPois()) {
            cassandraTemplate.getCqlOperations().execute("INSERT INTO hotels_by_poi (poi_name, hotel_id, name, phone) VALUES (?, ?, ?, ?)",
                    poi, hotel.getId(), hotel.getName(), hotel.getPhone());
        }

		return new ResponseEntity<>(savedHotel, HttpStatus.CREATED);
	}

	// Endpoint to get a hotel by ID
	@GetMapping("/{hotelId}")
    public ResponseEntity<Hotel> getHotelById(@PathVariable String hotelId) {
        Hotel hotel = hotelService.getHotelById(hotelId);
        return hotel != null ? ResponseEntity.ok(hotel) : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    
    // Endpoint to find hotels by POI name
    @GetMapping("/poi/{poiName}")
    public ResponseEntity<List<Hotel>> findHotelsByPoiName(@PathVariable String poiName) {
        List<Hotel> hotels = hotelService.findHotelsByPoiName(poiName);
        return hotels != null ? ResponseEntity.ok(hotels) : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

}


// ----------------------------
// Application Configuration
// ----------------------------

@Configuration
class CassandraConfiguration {

	@Bean
    public CqlSession cqlSession() {
        return CqlSession.builder()
                .withKeyspace(CqlIdentifier.fromCql("hotel"))
                .build();
    }

    @Bean
    public CassandraMappingContext cassandraMapping(CqlSession cqlSession) throws ClassNotFoundException {
        CassandraMappingContext mappingContext = new CassandraMappingContext();
        mappingContext.setUserTypeResolver(new SimpleUserTypeResolver(cqlSession, CqlIdentifier.fromCql("hotel")));
        return mappingContext;
    }

    @Bean
    public CassandraCustomConversions customConversions(CqlSession cqlSession) {
        List<Object> converters = new ArrayList<>();
        // Get the UDT for 'address'
        com.datastax.oss.driver.api.core.type.UserDefinedType addressUdt = cqlSession.getMetadata()
                .getKeyspace(CqlIdentifier.fromCql("hotel"))
                .flatMap(ks -> ks.getUserDefinedType("address"))
                .orElseThrow(() -> new IllegalArgumentException("address UDT not found"));
        // Add a converter from Address to UdtValue
        converters.add(new org.springframework.core.convert.converter.Converter<Address, UdtValue>() {
            @Override
            public UdtValue convert(Address source) {
                return addressUdt.newValue()
                        .setString("street", source.getStreet())
                        .setString("city", source.getCity())
                        .setString("state_or_province", source.getState_or_province())
                        .setString("postal_code", source.getPostal_code())
                        .setString("country", source.getCountry());
            }
        });
        return new CassandraCustomConversions(converters);
    }

}


@SpringBootApplication
public class HotelsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(HotelsServiceApplication.class, args);
	}

}
