package by.t1d.type1diabetes.service;

import by.t1d.type1diabetes.dao.CarbEntryDao;
import by.t1d.type1diabetes.dao.InsulinEntryDao;
import by.t1d.type1diabetes.dto.ForecastGlucoseResultDto;
import by.t1d.type1diabetes.dto.ForecastInsulinResultDto;
import by.t1d.type1diabetes.dto.GlucoseInsulinSubsystemDto;
import by.t1d.type1diabetes.mapper.CarbEntryMapper;
import by.t1d.type1diabetes.mapper.InsulinEntryMapper;
import by.t1d.type1diabetes.math.calculation.FoodIngestionMathCalculation;
import by.t1d.type1diabetes.math.calculation.InsulinMathCalculation;
import by.t1d.type1diabetes.model.CarbEntry;
import by.t1d.type1diabetes.math.calculation.GlucoseMathCalculation;
import by.t1d.type1diabetes.model.InsulinEntry;
import by.t1d.type1diabetes.util.MathCalculationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class GlucoseInsulinSubsystemServiceImpl implements GlucoseInsulinSubsystemService {

    private static final Long HOURS_IN_A_DAY = 24L;

    private final CarbEntryDao carbEntryDao;

    private final InsulinEntryDao insulinEntryDao;

    private final CarbEntryMapper carbEntryMapper;

    private final InsulinEntryMapper insulinEntryMapper;

    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    @Override
    public GlucoseInsulinSubsystemDto forecast(GlucoseInsulinSubsystemDto subsystemDto) {
        saveNewEntries(subsystemDto);

        final List<ForecastGlucoseResultDto> resultsForEachCarbEntry = new CopyOnWriteArrayList<>();

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        getCarbEntriesFromLast24Hours(subsystemDto).forEach(
                carbEntry -> {
                    ForecastGlucoseResultDto resultDto = new ForecastGlucoseResultDto();

                    CompletableFuture<Void> future = CompletableFuture.supplyAsync(() ->
                                    GlucoseMathCalculation.forecastGlucoseDistribution(carbEntry, resultDto), executorService)
                            .thenApply(GlucoseMathCalculation::forecastGlucoseAbsorption)
                            .thenApplyAsync(currentResult ->
                                    FoodIngestionMathCalculation.forecast(carbEntry, currentResult), executorService)
                            .thenAccept(resultsForEachCarbEntry::add);
                    futures.add(future);
                }
        );

        final List<ForecastInsulinResultDto> resultsForEachInsulinEntry = new CopyOnWriteArrayList<>();

        getInsulinEntriesFromLast24Hours(subsystemDto).forEach(
                insulinEntry -> {
                    ForecastInsulinResultDto resultDto = new ForecastGlucoseResultDto();

                    CompletableFuture<Void> future = CompletableFuture.supplyAsync(() ->
                            InsulinMathCalculation.forecastInjection(insulinEntry, resultDto), executorService)
                            .thenApply(currentResult -> InsulinMathCalculation.forecastS1AndS2(insulinEntry, currentResult))
                            .thenApply(InsulinMathCalculation::forecastUI)
                            .thenApply(currentResult -> InsulinMathCalculation.forecastI(insulinEntry, resultDto))
                            .thenApply(currentResult -> InsulinMathCalculation.forecastX1AndX2AndX3(insulinEntry, resultDto))
                            .thenAccept(resultsForEachInsulinEntry::add);

                    futures.add(future);
                }
        );

        futures.forEach(CompletableFuture::join);

        fillGlucoseInsulinSubsystemDto(subsystemDto, resultsForEachCarbEntry, resultsForEachInsulinEntry);

        return subsystemDto;
    }

    private void saveNewEntries(GlucoseInsulinSubsystemDto subsystemDto) {
        if (subsystemDto.getIsCreateEntry()) {
            carbEntryDao.save(carbEntryMapper.toEntity(subsystemDto));

            if (subsystemDto.getInsulinDose() > 0) {
                insulinEntryDao.save(insulinEntryMapper.toEntity(subsystemDto));
            }
        }
    }

    private List<CarbEntry> getCarbEntriesFromLast24Hours(GlucoseInsulinSubsystemDto subsystemDto) {
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

    private List<InsulinEntry> getInsulinEntriesFromLast24Hours(GlucoseInsulinSubsystemDto subsystemDto) {
        LocalDateTime timeThreshold = LocalDateTime.now().minusHours(HOURS_IN_A_DAY);

        List<InsulinEntry> insulinEntries = insulinEntryDao.findEntriesFromLast24Hours(timeThreshold);

        if (!subsystemDto.getIsCreateEntry() && subsystemDto.getInsulinDose() > 0) {
            //not entity, using only on math calculations
            insulinEntries.add(new InsulinEntry(subsystemDto.getStartTimeI(),
                    subsystemDto.getDurationI(),
                    subsystemDto.getInsulinDose()));
        }

        return insulinEntries;
    }

    private void fillGlucoseInsulinSubsystemDto(GlucoseInsulinSubsystemDto subsystemDto,
                                                List<ForecastGlucoseResultDto> resultsForEachCarbEntry,
                                                List<ForecastInsulinResultDto> resultsForEachInsulinEntry) {
        subsystemDto.setTValues(resultsForEachCarbEntry.get(0).getTValues());

        List<CompletableFuture<Void>> futures = new ArrayList<>(Arrays.asList(
                CompletableFuture.supplyAsync(() ->
                        MathCalculationUtil.sumLists(resultsForEachCarbEntry
                                .stream()
                                .map(ForecastGlucoseResultDto::getD1DistributionValues)
                                .collect(Collectors.toList())), executorService)
                .thenAccept(subsystemDto::setD1DistributionValues),
                CompletableFuture.supplyAsync(() ->
                        MathCalculationUtil.sumLists(resultsForEachCarbEntry
                                .stream()
                                .map(ForecastGlucoseResultDto::getD2DistributionValues)
                                .collect(Collectors.toList())), executorService)
                .thenAccept(subsystemDto::setD2DistributionValues),
                CompletableFuture.supplyAsync(() ->
                        MathCalculationUtil.sumLists(resultsForEachCarbEntry
                                .stream()
                                .map(ForecastGlucoseResultDto::getUGAbsorptionValues)
                                .collect(Collectors.toList())), executorService)
                .thenAccept(subsystemDto::setUGAbsorptionValues),
                CompletableFuture.supplyAsync(() ->
                        MathCalculationUtil.sumLists(resultsForEachCarbEntry
                                .stream()
                                .map(ForecastGlucoseResultDto::getFIValues)
                                .collect(Collectors.toList())), executorService)
                .thenAccept(subsystemDto::setFIValues),
                CompletableFuture.supplyAsync(() ->
                        MathCalculationUtil.sumLists(resultsForEachInsulinEntry
                                .stream()
                                .map(ForecastInsulinResultDto::getIIValues)
                                .collect(Collectors.toList())), executorService)
                .thenAccept(subsystemDto::setIIValues),
                CompletableFuture.supplyAsync(() ->
                        MathCalculationUtil.sumLists(resultsForEachInsulinEntry
                                .stream()
                                .map(ForecastInsulinResultDto::getS1Values)
                                .collect(Collectors.toList())), executorService)
                .thenAccept(subsystemDto::setS1Values),
                CompletableFuture.supplyAsync(() ->
                        MathCalculationUtil.sumLists(resultsForEachInsulinEntry
                                .stream()
                                .map(ForecastInsulinResultDto::getS2Values)
                                .collect(Collectors.toList())), executorService)
                .thenAccept(subsystemDto::setS2Values),
                CompletableFuture.supplyAsync(() ->
                                MathCalculationUtil.sumLists(resultsForEachInsulinEntry
                                        .stream()
                                        .map(ForecastInsulinResultDto::getUIValues)
                                        .collect(Collectors.toList())), executorService)
                        .thenAccept(subsystemDto::setUIValues),
                CompletableFuture.supplyAsync(() ->
                                MathCalculationUtil.sumLists(resultsForEachInsulinEntry
                                        .stream()
                                        .map(ForecastInsulinResultDto::getIValues)
                                        .collect(Collectors.toList())), executorService)
                        .thenAccept(subsystemDto::setIValues),
                CompletableFuture.supplyAsync(() ->
                                MathCalculationUtil.sumLists(resultsForEachInsulinEntry
                                        .stream()
                                        .map(ForecastInsulinResultDto::getX1Values)
                                        .collect(Collectors.toList())), executorService)
                        .thenAccept(subsystemDto::setX1Values),
                CompletableFuture.supplyAsync(() ->
                                MathCalculationUtil.sumLists(resultsForEachInsulinEntry
                                        .stream()
                                        .map(ForecastInsulinResultDto::getX2Values)
                                        .collect(Collectors.toList())), executorService)
                        .thenAccept(subsystemDto::setX2Values),
                CompletableFuture.supplyAsync(() ->
                                MathCalculationUtil.sumLists(resultsForEachInsulinEntry
                                        .stream()
                                        .map(ForecastInsulinResultDto::getX3Values)
                                        .collect(Collectors.toList())), executorService)
                        .thenAccept(subsystemDto::setX3Values)));

        futures.forEach(CompletableFuture::join);
    }

}
