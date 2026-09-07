package ru.wartesd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.wartesd.entity.Record;
import ru.wartesd.entity.RecordStatus;




@Repository
public interface RecordRepository extends JpaRepository<Record, Integer>{
    @Query("UPDATE Record SET status = :status WHERE id = :id ")
    @Modifying
    void update(int id,@Param("status") RecordStatus newStatus);
}
