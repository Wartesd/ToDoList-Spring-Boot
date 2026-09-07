package ru.wartesd.entity.dto;

import ru.wartesd.entity.Record;

import java.util.List;

public class RecordsContainerDto {
    private final String username;
    private final List<ru.wartesd.entity.Record> records;
    private final int numberOfDoneRecords;
    private final int numberOfActiveRecords;

    public RecordsContainerDto(String username, List<Record> records, int numberOfDoneRecords, int numberOfActiveRecords) {
        this.username = username;
        this.records = records;
        this.numberOfDoneRecords = numberOfDoneRecords;
        this.numberOfActiveRecords = numberOfActiveRecords;
    }

    public List<ru.wartesd.entity.Record> getRecords() {
        return records;
    }

    public int getNumberOfActiveRecords() {
        return numberOfActiveRecords;
    }

    public int getNumberOfDoneRecords() {
        return numberOfDoneRecords;
    }

    public String getUsername() {
        return username;
    }
}
