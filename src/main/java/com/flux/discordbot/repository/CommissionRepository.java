package com.flux.discordbot.repository;

import com.flux.discordbot.entities.Commission;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommissionRepository extends MongoRepository<Commission, String> {
    Commission findCommissionByChannelId(long id);
    Commission findCommissionByUserId(long id);
}
