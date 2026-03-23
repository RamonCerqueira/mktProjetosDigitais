package com.mktplace.repository;

import com.mktplace.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("select m from Message m where m.offer.negotiationKey = :negotiationKey order by m.createdAt asc")
    List<Message> findByNegotiationKeyOrderByCreatedAtAsc(@Param("negotiationKey") String negotiationKey);
}
