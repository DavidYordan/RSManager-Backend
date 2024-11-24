package com.rsmanager.repository.local;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rsmanager.model.ApplicationPaymentRecord;
import com.rsmanager.model.ApplicationProcessRecord;

import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationPaymentRecordRepository extends JpaRepository<ApplicationPaymentRecord, Long>, JpaSpecificationExecutor<ApplicationPaymentRecord> {
    
    Optional<ApplicationPaymentRecord> findByPaymentId(Long paymentId);

    List<ApplicationPaymentRecord> findAllByApplicationProcessRecord_UserIdIn(List<Long> userIds);

    List<ApplicationPaymentRecord> findAllByApplicationProcessRecord_InviterIdIn(List<Long> inviterIds);

    List<ApplicationPaymentRecord> findByApplicationProcessRecord_UserIdAndPaymentTimeAfter(Long userId, LocalDate startDate);

    List<ApplicationPaymentRecord> findByApplicationProcessRecord_UserIdInAndPaymentTimeAfter(List<Long> userIds, LocalDate startDate);

    @Query("SELECT apr FROM ApplicationPaymentRecord apr WHERE apr.paymentId = :paymentId")
    Optional<ApplicationProcessRecord> findApplicationProcessRecordByPaymentId(Long paymentId);

    @Query(
        value = "SELECT currency_name, SUM(payment_amount) AS totalAmount " +
                "FROM application_payment_record " +
                "WHERE status = true AND process_id = :processId " +
                "GROUP BY currency_name",
        nativeQuery = true
    )
    List<Object[]> findTotalPaymentAmountByCurrencyAndProcessId(@Param("processId") Long processId);
}
