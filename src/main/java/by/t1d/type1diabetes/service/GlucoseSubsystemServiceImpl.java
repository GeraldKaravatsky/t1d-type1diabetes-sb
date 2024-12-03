package by.t1d.type1diabetes.service;

import by.t1d.type1diabetes.dao.CarbEntryDao;
import by.t1d.type1diabetes.dto.ForecastResultDto;
import by.t1d.type1diabetes.dto.GlucoseSubsystemDto;
import by.t1d.type1diabetes.mapper.CarbEntryMapper;
import by.t1d.type1diabetes.model.CarbEntry;
import by.t1d.type1diabetes.util.GlucoseMathCalculation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class GlucoseSubsystemServiceImpl implements GlucoseSubsystemService {

    private static final Long HOURS_IN_A_DAY = 24L;

    private final CarbEntryDao carbEntryDao;

    private final CarbEntryMapper carbEntryMapper;

    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    @Override
    public GlucoseSubsystemDto forecastGlucoseDistributionAndAbsorption(GlucoseSubsystemDto subsystemDto) {
        saveNewCarbEntry(subsystemDto);

        final List<ForecastResultDto> forecastResultListForEachCarbEntry = new CopyOnWriteArrayList<>();

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        getEntriesFromLast24Hours(subsystemDto).forEach(
                carbEntry -> {
                    CompletableFuture<Void> future = CompletableFuture.supplyAsync(() ->
                                    GlucoseMathCalculation.forecastGlucoseDistribution(carbEntry), executorService)
                            .thenAccept(forecastResultDto -> forecastResultListForEachCarbEntry.add(
                                    GlucoseMathCalculation.forecastGlucoseAbsorption(forecastResultDto)));
                    futures.add(future);
                }
        );

        futures.forEach(CompletableFuture::join);

        fillGlucoseSubsystemDto(subsystemDto, forecastResultListForEachCarbEntry);

        return subsystemDto;
    }

    private void saveNewCarbEntry(GlucoseSubsystemDto subsystemDto) {
        if (subsystemDto.getIsCreateEntry()) {
            carbEntryDao.save(carbEntryMapper.toEntity(subsystemDto));
        }
    }

    private List<CarbEntry> getEntriesFromLast24Hours(GlucoseSubsystemDto subsystemDto) {
        LocalDateTime timeThreshold = LocalDateTime.now().minusHours(HOURS_IN_A_DAY);

        List<CarbEntry> carbEntries = carbEntryDao.findEntriesFromLast24Hours(timeThreshold);

        if (!subsystemDto.getIsCreateEntry()) {
            //not entity, using only on math calculations
            carbEntries.add(new CarbEntry(subsystemDto.getStartTime(),
                    subsystemDto.getDuration(),
                    subsystemDto.getCarbs()));
        }

        return carbEntries;
    }

    private void fillGlucoseSubsystemDto(GlucoseSubsystemDto subsystemDto,
                                         List<ForecastResultDto> forecastResultListForEachCarbEntry) {
        subsystemDto.setTList(forecastResultListForEachCarbEntry.get(0).getTList());

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() ->
                        GlucoseMathCalculation.sumLists(forecastResultListForEachCarbEntry
                                .stream()
                                .map(ForecastResultDto::getD1List)
                                .collect(Collectors.toList())), executorService)
                .thenAccept(subsystemDto::setD1List);

        futures.add(future);

        future = CompletableFuture.supplyAsync(() ->
                        GlucoseMathCalculation.sumLists(forecastResultListForEachCarbEntry
                                .stream()
                                .map(ForecastResultDto::getD2List)
                                .collect(Collectors.toList())), executorService)
                .thenAccept(subsystemDto::setD2List);

        futures.add(future);

        future = CompletableFuture.supplyAsync(() ->
                        GlucoseMathCalculation.sumLists(forecastResultListForEachCarbEntry
                                .stream()
                                .map(ForecastResultDto::getUGList)
                                .collect(Collectors.toList())), executorService)
                .thenAccept(subsystemDto::setUGList);

        futures.add(future);

        futures.forEach(CompletableFuture::join);
    }

}
