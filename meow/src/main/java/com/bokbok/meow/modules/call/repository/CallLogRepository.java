package com.bokbok.meow.modules.call.repository;

import com.bokbok.meow.modules.call.entity.CallLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CallLogRepository extends JpaRepository<CallLog, String> {
    List<CallLog> findByCallerIdOrReceiverIdOrderByCreatedAtDesc(
            String callerId, String receiverId
    );
}