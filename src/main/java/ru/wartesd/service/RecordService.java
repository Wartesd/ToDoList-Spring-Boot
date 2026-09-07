package ru.wartesd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.wartesd.entity.User;
import ru.wartesd.repository.RecordRepository;
import ru.wartesd.entity.Record;
import ru.wartesd.entity.RecordStatus;
import ru.wartesd.entity.dto.RecordsContainerDto;



import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RecordService {
    private final UserService userService;
    private final RecordRepository recordRepository;

    @Autowired
    public RecordService(UserService userService, RecordRepository recordRepository) {
        this.userService = userService;
        this.recordRepository = recordRepository;
    }


    @Transactional(readOnly = true)
    public RecordsContainerDto findAllRecords(String filterMode){
        User user = userService.getCurrentUser();
        List<Record> records = user.getRecords().stream()
                .sorted(Comparator.comparingInt(Record::getId))
                .collect(Collectors.toList());

        int numberOfDoneRecords = (int) records.stream().filter(record -> record.getStatus()== RecordStatus.DONE).count();
        int numberOfActiveRecords = (int) records.stream().filter(record -> record.getStatus()== RecordStatus.ACTIVE).count();

        if (filterMode == null || filterMode.isBlank()) return new RecordsContainerDto(user.getName(),records,numberOfDoneRecords,numberOfActiveRecords);

        String filterModeInUpperCase = filterMode.toUpperCase();
        List<String> allowedFilterModes = Arrays.stream(RecordStatus.values())
                .map(Enum :: name)
                .collect(Collectors.toList());
        if (allowedFilterModes.contains(filterModeInUpperCase)){
            List<Record> filteredRecords = records.stream()
                    .filter(record -> record.getStatus() == RecordStatus.valueOf(filterModeInUpperCase))
                    .collect(Collectors.toList());
            return new RecordsContainerDto(user.getName(),filteredRecords ,numberOfDoneRecords,numberOfActiveRecords);
        }else{
            return new RecordsContainerDto(user.getName(),records,numberOfDoneRecords,numberOfActiveRecords);
        }
    }

    public void saveRecord(String title){
        if (title != null && !title.isBlank()){
            User user = userService.getCurrentUser();
            recordRepository.save(new Record(title,user));
        }
    }

    public void updateRecordStatus(int id, RecordStatus newStatus){
        recordRepository.update(id,newStatus);
    }

    public void deleteRecord(int id){
        recordRepository.deleteById(id);
    }
}
