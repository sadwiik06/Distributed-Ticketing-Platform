package com.sai.ticketing.event.repository;

import com.sai.ticketing.event.model.Event;
import org.springframework.data.mongodb.repository.MongoRepository;
public interface EventRepository extends MongoRepository<Event,String>{
}
