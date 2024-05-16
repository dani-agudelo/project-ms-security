package com.ucaldas.mssecurity.Repositories;

import com.ucaldas.mssecurity.Models.Loyalty;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface LoyaltyRepository extends MongoRepository<Loyalty, String> {
  @Query("{'user._id': ObjectId(?0)}")
  Loyalty getLoyalty(String userId);
}
