/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.cvng.servicelevelobjective.services.impl;

import static io.harness.cvng.servicelevelobjective.entities.SLIRecord.SLIState.BAD;
import static io.harness.cvng.servicelevelobjective.entities.SLIRecord.SLIState.GOOD;
import static io.harness.cvng.servicelevelobjective.entities.SLIRecord.SLIState.NO_DATA;
import static io.harness.data.structure.UUIDGenerator.generateUuid;
import static io.harness.persistence.HQuery.excludeAuthority;
import static io.harness.rule.OwnerRule.ARPITJ;
import static io.harness.rule.OwnerRule.DEEPAK_CHHIKARA;
import static io.harness.rule.OwnerRule.KAMAL;
import static io.harness.rule.OwnerRule.KAPIL;
import static io.harness.rule.OwnerRule.KARAN_SARASWAT;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.mockito.Mockito.when;

import io.harness.CvNextGenTestBase;
import io.harness.category.element.UnitTests;
import io.harness.cvng.BuilderFactory;
import io.harness.cvng.core.beans.monitoredService.MonitoredServiceDTO;
import io.harness.cvng.core.beans.params.MonitoredServiceParams;
import io.harness.cvng.core.beans.params.TimeRangeParams;
import io.harness.cvng.core.entities.EntityDisableTime;
import io.harness.cvng.core.entities.MonitoredService;
import io.harness.cvng.core.services.api.EntityDisabledTimeService;
import io.harness.cvng.core.services.api.monitoredService.MonitoredServiceService;
import io.harness.cvng.core.utils.DateTimeUtils;
import io.harness.cvng.servicelevelobjective.beans.SLIMissingDataType;
import io.harness.cvng.servicelevelobjective.beans.SLODashboardWidget.Point;
import io.harness.cvng.servicelevelobjective.beans.SLODashboardWidget.SLOGraphData;
import io.harness.cvng.servicelevelobjective.beans.ServiceLevelIndicatorDTO;
import io.harness.cvng.servicelevelobjective.entities.SLIRecord;
import io.harness.cvng.servicelevelobjective.entities.SLIRecord.SLIRecordKeys;
import io.harness.cvng.servicelevelobjective.entities.SLIRecord.SLIRecordParam;
import io.harness.cvng.servicelevelobjective.entities.SLIRecord.SLIState;
import io.harness.cvng.servicelevelobjective.entities.ServiceLevelIndicator;
import io.harness.cvng.servicelevelobjective.services.api.ServiceLevelIndicatorService;
import io.harness.persistence.HPersistence;
import io.harness.rule.Owner;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mongodb.morphia.query.Sort;

public class SLIRecordServiceImplTest extends CvNextGenTestBase {
  @Spy @Inject private SLIRecordServiceImpl sliRecordService;
  @Inject private Clock clock;
  @Inject private HPersistence hPersistence;

  @Inject private ServiceLevelIndicatorService serviceLevelIndicatorService;
  @Inject private MonitoredServiceService monitoredServiceService;
  @Inject private EntityDisabledTimeService entityDisabledTimeService;

  private String verificationTaskId;
  private String sliId;

  private BuilderFactory builderFactory;

  private ServiceLevelIndicator serviceLevelIndicator;

  private MonitoredService monitoredService;

  @Before
  public void setup() {
    builderFactory = BuilderFactory.getDefault();
    MockitoAnnotations.initMocks(this);
    SLIRecordServiceImpl.MAX_NUMBER_OF_POINTS = 5;
    verificationTaskId = generateUuid();
    /*sliId = generateUuid();*/
    monitoredService = createMonitoredService();
    sliId = createServiceLevelIndicator();
    serviceLevelIndicator = getServiceLevelIndicator();
  }
  @Test
  @Owner(developers = KAMAL)
  @Category(UnitTests.class)
  public void testCreate_multipleSaves() {
    Instant startTime = Instant.parse("2020-07-27T10:50:00Z").minus(Duration.ofMinutes(10));
    List<SLIState> sliStates = Arrays.asList(BAD, GOOD, GOOD, NO_DATA, GOOD, GOOD, BAD, BAD, BAD, BAD);
    createData(startTime, sliStates);
    SLIRecord lastRecord = getLastRecord(serviceLevelIndicator.getUuid());
    assertThat(lastRecord.getRunningBadCount()).isEqualTo(5);
    assertThat(lastRecord.getRunningGoodCount()).isEqualTo(4);
    createData(startTime.plus(Duration.ofMinutes(10)), sliStates);
    lastRecord = getLastRecord(serviceLevelIndicator.getUuid());
    assertThat(lastRecord.getRunningBadCount()).isEqualTo(10);
    assertThat(lastRecord.getRunningGoodCount()).isEqualTo(8);
  }

  @Test
  @Owner(developers = DEEPAK_CHHIKARA)
  @Category(UnitTests.class)
  public void testUpdate_completeOverlap() {
    Instant startTime = Instant.parse("2020-07-27T10:50:00Z");
    List<SLIState> sliStates = Arrays.asList(BAD, GOOD, GOOD, NO_DATA, GOOD, GOOD, BAD, BAD, BAD, BAD);
    List<SLIRecordParam> sliRecordParams = getSLIRecordParam(startTime, sliStates);
    sliRecordService.create(sliRecordParams, sliId, verificationTaskId, 0);
    SLIRecord lastRecord = getLastRecord(sliId);
    assertThat(lastRecord.getRunningBadCount()).isEqualTo(5);
    assertThat(lastRecord.getRunningGoodCount()).isEqualTo(4);
    assertThat(lastRecord.getSliVersion()).isEqualTo(0);
    List<SLIState> updatedSliStates = Arrays.asList(GOOD, BAD, BAD, NO_DATA, GOOD, BAD, BAD, BAD, BAD, BAD);
    List<SLIRecordParam> updatedSliRecordParams = getSLIRecordParam(startTime, updatedSliStates);
    sliRecordService.create(updatedSliRecordParams, sliId, verificationTaskId, 1);
    SLIRecord updatedLastRecord = getLastRecord(sliId);
    assertThat(updatedLastRecord.getRunningBadCount()).isEqualTo(7);
    assertThat(updatedLastRecord.getRunningGoodCount()).isEqualTo(2);
    assertThat(updatedLastRecord.getSliVersion()).isEqualTo(1);
  }

  @Test
  @Owner(developers = DEEPAK_CHHIKARA)
  @Category(UnitTests.class)
  public void testUpdate_partialOverlap() {
    Instant startTime = Instant.parse("2020-07-27T10:50:00Z");
    List<SLIState> sliStates = Arrays.asList(BAD, GOOD, GOOD, NO_DATA, GOOD, GOOD, BAD, BAD, BAD, BAD);
    List<SLIRecordParam> sliRecordParams = getSLIRecordParam(startTime, sliStates);
    sliRecordService.create(sliRecordParams, sliId, verificationTaskId, 0);
    SLIRecord lastRecord = getLastRecord(sliId);
    assertThat(lastRecord.getRunningBadCount()).isEqualTo(5);
    assertThat(lastRecord.getRunningGoodCount()).isEqualTo(4);
    assertThat(lastRecord.getSliVersion()).isEqualTo(0);
    Instant updatedStartTime = Instant.parse("2020-07-27T10:55:00Z");
    List<SLIState> updatedSliStates = Arrays.asList(BAD, BAD, BAD, BAD, BAD);
    List<SLIRecordParam> updatedSliRecordParams = getSLIRecordParam(updatedStartTime, updatedSliStates);
    sliRecordService.create(updatedSliRecordParams, sliId, verificationTaskId, 1);
    SLIRecord updatedLastRecord = getLastRecord(sliId);
    assertThat(updatedLastRecord.getRunningBadCount()).isEqualTo(6);
    assertThat(updatedLastRecord.getRunningGoodCount()).isEqualTo(3);
    assertThat(updatedLastRecord.getSliVersion()).isEqualTo(1);
  }

  @Test
  @Owner(developers = DEEPAK_CHHIKARA)
  @Category(UnitTests.class)
  public void testUpdate_MissingRecords() {
    Instant startTime = Instant.parse("2020-07-27T10:00:00Z");
    List<SLIState> sliStates = Arrays.asList(BAD, GOOD, GOOD, NO_DATA, GOOD, GOOD, BAD, BAD, BAD, BAD);
    List<SLIRecordParam> sliRecordParams = getSLIRecordParam(startTime, sliStates);
    sliRecordService.create(sliRecordParams, sliId, verificationTaskId, 0);
    SLIRecord lastRecord = getLastRecord(sliId);
    assertThat(lastRecord.getRunningBadCount()).isEqualTo(5);
    assertThat(lastRecord.getRunningGoodCount()).isEqualTo(4);
    assertThat(lastRecord.getSliVersion()).isEqualTo(0);
    Instant updatedStartTime = Instant.parse("2020-07-27T10:05:00Z");
    List<SLIState> updatedSliStates =
        Arrays.asList(BAD, BAD, BAD, BAD, BAD, NO_DATA, NO_DATA, NO_DATA, NO_DATA, NO_DATA);
    List<SLIRecordParam> updatedSliRecordParams = getSLIRecordParam(updatedStartTime, updatedSliStates);
    sliRecordService.create(updatedSliRecordParams, sliId, verificationTaskId, 1);
    SLIRecord updatedLastRecord = getLastRecord(sliId);
    assertThat(updatedLastRecord.getRunningBadCount()).isEqualTo(6);
    assertThat(updatedLastRecord.getRunningGoodCount()).isEqualTo(3);
    assertThat(updatedLastRecord.getSliVersion()).isEqualTo(1);
    assertThat(updatedLastRecord.getTimestamp()).isEqualTo(Instant.parse("2020-07-27T10:14:00Z"));
  }

  @Test
  @Owner(developers = DEEPAK_CHHIKARA)
  @Category(UnitTests.class)
  public void testUpdate_NotSyncedRecords() {
    Instant startTime = Instant.parse("2020-07-27T10:00:00Z");
    List<SLIState> sliStates = Arrays.asList(BAD, GOOD, GOOD, NO_DATA, GOOD, GOOD, BAD, BAD, BAD, BAD);
    List<SLIRecordParam> sliRecordParams = getSLIRecordParam(startTime, sliStates);
    sliRecordService.create(sliRecordParams, sliId, verificationTaskId, 0);
    SLIRecord lastRecord = getLastRecord(sliId);
    assertThat(lastRecord.getRunningBadCount()).isEqualTo(5);
    assertThat(lastRecord.getRunningGoodCount()).isEqualTo(4);
    assertThat(lastRecord.getSliVersion()).isEqualTo(0);
    Instant updatedStartTime = Instant.parse("2020-07-27T10:05:00Z");
    List<SLIState> updatedSliStates = Arrays.asList(BAD, BAD, BAD, BAD, BAD, GOOD, BAD, GOOD, BAD, GOOD);
    List<SLIRecordParam> updatedSliRecordParams = getSLIRecordParam(updatedStartTime, updatedSliStates);
    sliRecordService.create(updatedSliRecordParams, sliId, verificationTaskId, 1);
    SLIRecord updatedLastRecord = getLastRecord(sliId);
    assertThat(updatedLastRecord.getRunningBadCount()).isEqualTo(8);
    assertThat(updatedLastRecord.getRunningGoodCount()).isEqualTo(6);
    assertThat(updatedLastRecord.getSliVersion()).isEqualTo(1);
    assertThat(updatedLastRecord.getTimestamp()).isEqualTo(Instant.parse("2020-07-27T10:14:00Z"));
  }

  @Test
  @Owner(developers = DEEPAK_CHHIKARA)
  @Category(UnitTests.class)
  public void testUpdate_retryConcurrency() {
    Instant startTime = Instant.parse("2020-07-27T10:00:00Z");
    List<SLIState> sliStates = Arrays.asList(BAD, GOOD, GOOD, NO_DATA, GOOD, GOOD, BAD, BAD, BAD, BAD);
    List<SLIRecordParam> sliRecordParams = getSLIRecordParam(startTime, sliStates);
    Instant endTime = sliRecordParams.get(sliRecordParams.size() - 1).getTimeStamp();
    sliRecordService.create(sliRecordParams, sliId, verificationTaskId, 0);
    SLIRecord lastRecord = getLastRecord(sliId);
    assertThat(lastRecord.getRunningBadCount()).isEqualTo(5);
    assertThat(lastRecord.getRunningGoodCount()).isEqualTo(4);
    assertThat(lastRecord.getSliVersion()).isEqualTo(0);
    List<SLIRecord> firstTimeResponse = sliRecordService.getSLIRecords(lastRecord.getSliId(), startTime, endTime);
    for (SLIRecord sliRecord : firstTimeResponse) {
      sliRecord.setVersion(-1);
    }
    List<SLIRecord> secondTimeResponse = sliRecordService.getSLIRecords(lastRecord.getSliId(), startTime, endTime);
    Instant updatedStartTime = Instant.parse("2020-07-27T10:05:00Z");
    List<SLIState> updatedSliStates = Arrays.asList(BAD, BAD, BAD, BAD, BAD, GOOD, BAD, GOOD, BAD, GOOD);
    List<SLIRecordParam> updatedSliRecordParams = getSLIRecordParam(updatedStartTime, updatedSliStates);
    when(sliRecordService.getSLIRecords(sliId, updatedStartTime,
             updatedSliRecordParams.get(updatedSliRecordParams.size() - 1).getTimeStamp().plus(1, ChronoUnit.MINUTES)))
        .thenReturn(firstTimeResponse, secondTimeResponse);
    sliRecordService.create(updatedSliRecordParams, sliId, verificationTaskId, 1);
    SLIRecord updatedLastRecord = getLastRecord(sliId);
    assertThat(updatedLastRecord.getRunningBadCount()).isEqualTo(8);
    assertThat(updatedLastRecord.getRunningGoodCount()).isEqualTo(6);
    assertThat(updatedLastRecord.getSliVersion()).isEqualTo(1);
    assertThat(updatedLastRecord.getTimestamp()).isEqualTo(Instant.parse("2020-07-27T10:14:00Z"));
  }

  @Test
  @Owner(developers = KAMAL)
  @Category(UnitTests.class)
  public void testGetGraphData_noData() {
    assertThat(
        sliRecordService
            .getGraphData(serviceLevelIndicator, clock.instant(), clock.instant(), 10, SLIMissingDataType.GOOD, 0)
            .getSloPerformanceTrend())
        .isEmpty();
  }

  @Test
  @Owner(developers = KAMAL)
  @Category(UnitTests.class)
  public void testGetGraphData_withAllStates() {
    Instant startTime = Instant.parse("2020-07-27T10:50:00Z").minus(Duration.ofMinutes(10));
    List<SLIState> sliStates = Arrays.asList(BAD, GOOD, GOOD, NO_DATA, GOOD, GOOD, BAD, BAD, BAD, BAD);
    createData(startTime, sliStates);

    assertThat(sliRecordService
                   .getGraphData(serviceLevelIndicator, startTime.minus(Duration.ofHours(1)),
                       startTime.plus(Duration.ofMinutes(11)), 10, SLIMissingDataType.GOOD, 0)
                   .getSloPerformanceTrend())
        .hasSize(6);
  }

  @Test
  @Owner(developers = KARAN_SARASWAT)
  @Category(UnitTests.class)
  public void testGetGraphData_withAllStates_WithRangedErrorBudgetBurned() {
    Instant startTime = Instant.parse("2020-07-27T10:50:00Z").minus(Duration.ofMinutes(15));
    List<SLIState> sliStates =
        Arrays.asList(BAD, GOOD, GOOD, BAD, NO_DATA, NO_DATA, BAD, BAD, BAD, BAD, GOOD, NO_DATA, BAD, BAD, GOOD);
    createData(startTime, sliStates);

    SLOGraphData sloGraphData = sliRecordService.getGraphData(serviceLevelIndicator,
        startTime.minus(Duration.ofHours(1)), startTime.plus(Duration.ofMinutes(16)), 15, SLIMissingDataType.GOOD, 0,
        TimeRangeParams.builder()
            .startTime(startTime.plus(Duration.ofMinutes(2)))
            .endTime(startTime.plus(Duration.ofMinutes(12)))
            .build());
    assertThat(sloGraphData.getSloPerformanceTrend()).hasSize(6);
    assertThat(sloGraphData.getErrorBudgetBurned())
        .isEqualTo(5); // as sliRecord last in range timestamp is less than the filter end time

    sloGraphData = sliRecordService.getGraphData(serviceLevelIndicator, startTime.minus(Duration.ofHours(1)),
        startTime.plus(Duration.ofMinutes(11)), 10, SLIMissingDataType.GOOD, 0,
        TimeRangeParams.builder()
            .startTime(startTime.plus(Duration.ofSeconds(121)))
            .endTime(startTime.plus(Duration.ofSeconds(750)))
            .build());
    assertThat(sloGraphData.getErrorBudgetBurned()).isEqualTo(5);
  }

  @Test
  @Owner(developers = KAMAL)
  @Category(UnitTests.class)
  public void testGetGraphData_9TotalPointsWith5AsMaxValue() {
    Instant startTime = Instant.parse("2020-07-27T10:50:00Z").minus(Duration.ofMinutes(10));
    List<SLIState> sliStates = Arrays.asList(BAD, GOOD, GOOD, NO_DATA, GOOD, GOOD, BAD, BAD, BAD, BAD);
    createData(startTime, sliStates);

    assertThat(sliRecordService
                   .getGraphData(serviceLevelIndicator, startTime.minus(Duration.ofHours(10)),
                       startTime.plus(Duration.ofMinutes(1000)), 10, SLIMissingDataType.GOOD, 0)
                   .getSloPerformanceTrend())
        .hasSize(6);
  }

  @Test
  @Owner(developers = KAMAL)
  @Category(UnitTests.class)
  public void testGetGraphData_recalculation() {
    Instant startTime = Instant.parse("2020-07-27T10:50:00Z").minus(Duration.ofMinutes(10));
    List<SLIState> sliStates = Arrays.asList(BAD, GOOD, GOOD, NO_DATA, GOOD, GOOD, BAD, BAD, BAD, BAD);
    createData(startTime, sliStates);

    assertThat(sliRecordService
                   .getGraphData(serviceLevelIndicator, startTime, startTime.plus(Duration.ofMinutes(20)), 10,
                       SLIMissingDataType.GOOD, 1)
                   .isRecalculatingSLI())
        .isTrue();
  }

  @Test
  @Owner(developers = KAMAL)
  @Category(UnitTests.class)
  public void testGetGraphData_perMinute() {
    List<SLIState> sliStates = Arrays.asList(GOOD, NO_DATA, BAD, GOOD);
    List<Double> expectedSLITrend = Lists.newArrayList(100.0, 100.0, 66.66, 75.0);
    List<Double> expectedBurndown = Lists.newArrayList(100.0, 100.0, 99.0, 99.0);
    testGraphCalculation(sliStates, expectedSLITrend, expectedBurndown, 99);
  }

  @Test
  @Owner(developers = KAMAL)
  @Category(UnitTests.class)
  public void testGetGraphData_allBad() {
    List<SLIState> sliStates = Arrays.asList(BAD, BAD, BAD, BAD);
    List<Double> expectedSLITrend = Lists.newArrayList(0.0, 0.0, 0.0, 0.0);
    List<Double> expectedBurndown = Lists.newArrayList(99.0, 98.0, 97.0, 96.0);
    testGraphCalculation(sliStates, expectedSLITrend, expectedBurndown, 96);
  }

  @Test
  @Owner(developers = KAMAL)
  @Category(UnitTests.class)
  public void testGetGraphData_noDataConsideredBad() {
    List<SLIState> sliStates = Arrays.asList(GOOD, NO_DATA, BAD, GOOD);
    List<Double> expectedSLITrend = Lists.newArrayList(100.0, 50.0, 33.33, 50.0);
    List<Double> expectedBurndown = Lists.newArrayList(100.0, 99.0, 98.0, 98.0);
    testGraphCalculation(sliStates, SLIMissingDataType.BAD, expectedSLITrend, expectedBurndown, 98);
  }

  @Test
  @Owner(developers = ARPITJ)
  @Category(UnitTests.class)
  public void testGetGraphData_customTimePerMinute() {
    SLIRecordServiceImpl.MAX_NUMBER_OF_POINTS = 15;
    List<SLIState> sliStates =
        Arrays.asList(GOOD, NO_DATA, BAD, GOOD, GOOD, NO_DATA, BAD, GOOD, GOOD, NO_DATA, BAD, GOOD);
    List<Double> expectedSLITrend =
        Lists.newArrayList(100.0, 100.0, 66.66, 75.0, 80.0, 83.33, 71.42, 75.0, 77.77, 80.0, 72.72, 75.0);
    List<Double> expectedBurndown =
        Lists.newArrayList(100.0, 100.0, 99.0, 99.0, 99.0, 99.0, 98.0, 98.0, 98.0, 98.0, 97.0, 97.0);
    testGraphCalculation(sliStates, SLIMissingDataType.GOOD, expectedSLITrend, expectedBurndown, 97, 0, 0);
  }

  @Test
  @Owner(developers = ARPITJ)
  @Category(UnitTests.class)
  public void testGetGraphData_withDisabledTimes() {
    SLIRecordServiceImpl.MAX_NUMBER_OF_POINTS = 15;
    List<SLIState> sliStates =
        Arrays.asList(GOOD, NO_DATA, BAD, GOOD, GOOD, NO_DATA, NO_DATA, NO_DATA, NO_DATA, NO_DATA, NO_DATA, GOOD);
    List<Double> expectedSLITrend =
        Lists.newArrayList(100.0, 100.0, 66.66, 75.0, 80.0, 83.33, 83.33, 83.33, 83.33, 83.33, 83.33, 85.71);
    List<Double> expectedBurndown =
        Lists.newArrayList(100.0, 100.0, 99.0, 99.0, 99.0, 99.0, 99.0, 99.0, 99.0, 99.0, 99.0, 99.0);
    entityDisabledTimeService.save(EntityDisableTime.builder()
                                       .entityUUID(monitoredService.getUuid())
                                       .accountId(monitoredService.getAccountId())
                                       .startTime(clock.instant().minus(Duration.ofMinutes(6)).toEpochMilli())
                                       .endTime(clock.instant().minus(Duration.ofMinutes(2)).toEpochMilli())
                                       .build());

    testGraphCalculation(sliStates, SLIMissingDataType.GOOD, expectedSLITrend, expectedBurndown, 99, 0, 0);
  }

  @Test
  @Owner(developers = ARPITJ)
  @Category(UnitTests.class)
  public void testGetGraphData_AllGood() {
    SLIRecordServiceImpl.MAX_NUMBER_OF_POINTS = 100;
    List<SLIState> sliStates = new ArrayList<>();
    List<Double> expectedSLITrend = new ArrayList<>();
    List<Double> expectedBurndown = new ArrayList<>();
    for (int i = 0; i < 65; i++) {
      sliStates.add(GOOD);
      if (i < 65) {
        expectedSLITrend.add(100.0);
        expectedBurndown.add(100.0);
      }
    }
    testGraphCalculation(sliStates, SLIMissingDataType.GOOD, expectedSLITrend, expectedBurndown, 100, 0, 0);
  }

  @Test
  @Owner(developers = ARPITJ)
  @Category(UnitTests.class)
  public void testGetGraphData_customStartTimeAllGood() {
    SLIRecordServiceImpl.MAX_NUMBER_OF_POINTS = 100;
    List<SLIState> sliStates = new ArrayList<>();
    List<Double> expectedSLITrend = new ArrayList<>();
    List<Double> expectedBurndown = new ArrayList<>();
    for (int i = 0; i < 65; i++) {
      sliStates.add(GOOD);
      if (i < 61) {
        expectedSLITrend.add(100.0);
        expectedBurndown.add(100.0);
      }
    }
    testGraphCalculation(sliStates, SLIMissingDataType.GOOD, expectedSLITrend, expectedBurndown, 100, 4, 0);
  }

  @Test
  @Owner(developers = ARPITJ)
  @Category(UnitTests.class)
  public void testGetGraphData_customBothTimeAllGood() {
    SLIRecordServiceImpl.MAX_NUMBER_OF_POINTS = 100;
    List<SLIState> sliStates = new ArrayList<>();
    List<Double> expectedSLITrend = new ArrayList<>();
    List<Double> expectedBurndown = new ArrayList<>();
    for (int i = 0; i < 65; i++) {
      sliStates.add(GOOD);
      if (i < 57) {
        expectedSLITrend.add(100.0);
        expectedBurndown.add(100.0);
      }
    }
    testGraphCalculation(sliStates, SLIMissingDataType.GOOD, expectedSLITrend, expectedBurndown, 100, 4, 5);
  }

  @Test
  @Owner(developers = ARPITJ)
  @Category(UnitTests.class)
  public void testGetGraphData_customStartAllBad() {
    SLIRecordServiceImpl.MAX_NUMBER_OF_POINTS = 100;
    List<SLIState> sliStates = new ArrayList<>();
    List<Double> expectedSLITrend = new ArrayList<>();
    List<Double> expectedBurndown = new ArrayList<>();
    for (int i = 0; i < 65; i++) {
      sliStates.add(BAD);
      if (i < 61) {
        expectedSLITrend.add(0.0);
        expectedBurndown.add(95.0 - i);
      }
    }
    testGraphCalculation(sliStates, SLIMissingDataType.GOOD, expectedSLITrend, expectedBurndown, 35, 4, 0);
  }

  @Test
  @Owner(developers = KAPIL)
  @Category(UnitTests.class)
  public void testGetErrorBudgetBurnRate() {
    Instant startTime = Instant.parse("2020-07-27T10:50:00Z").minus(Duration.ofMinutes(10));
    List<SLIState> sliStates = Arrays.asList(BAD, GOOD, GOOD, NO_DATA, GOOD, GOOD, BAD, BAD, BAD, BAD);
    createData(startTime, sliStates);
    double errorBudgetBurnRate = sliRecordService.getErrorBudgetBurnRate(
        serviceLevelIndicator.getUuid(), Duration.ofMinutes(10).toMillis(), 120);
    assertThat(errorBudgetBurnRate).isCloseTo(3.333, offset(0.001));
  }

  @Test
  @Owner(developers = ARPITJ)
  @Category(UnitTests.class)
  public void testGetDisabledMinBetweenRecords_minDataWithLargeRange() {
    long startTime = 1659345900000L;
    long endTime = 1659345960000L;
    List<EntityDisableTime> disableTimes = new ArrayList<>();
    disableTimes.add(EntityDisableTime.builder().startTime(1659345900000L).endTime(1659345980000L).build());
    Pair<Long, Long> result = sliRecordService.getDisabledMinBetweenRecords(startTime, endTime, 0, disableTimes);
    assertThat(result.getLeft()).isEqualTo(1);
    assertThat(result.getRight()).isEqualTo(1);
  }

  @Test
  @Owner(developers = ARPITJ)
  @Category(UnitTests.class)
  public void testGetDisabledMinBetweenRecords_minDataWithSmallRange() {
    long startTime = 1659345900000L;
    long endTime = 1659345960000L;
    List<EntityDisableTime> disableTimes = new ArrayList<>();
    disableTimes.add(EntityDisableTime.builder().startTime(1659345900000L).endTime(1659345950000L).build());
    Pair<Long, Long> result = sliRecordService.getDisabledMinBetweenRecords(startTime, endTime, 0, disableTimes);
    assertThat(result.getLeft()).isEqualTo(0);
    assertThat(result.getRight()).isEqualTo(1);
  }

  @Test
  @Owner(developers = ARPITJ)
  @Category(UnitTests.class)
  public void testGetDisabledMinBetweenRecords_minDataWithExactRange() {
    long startTime = 1659345900000L;
    long endTime = 1659345960000L;
    List<EntityDisableTime> disableTimes = new ArrayList<>();
    disableTimes.add(EntityDisableTime.builder().startTime(1659345900000L).endTime(1659345960000L).build());
    Pair<Long, Long> result = sliRecordService.getDisabledMinBetweenRecords(startTime, endTime, 0, disableTimes);
    assertThat(result.getLeft()).isEqualTo(1);
    assertThat(result.getRight()).isEqualTo(1);
  }

  @Test
  @Owner(developers = ARPITJ)
  @Category(UnitTests.class)
  public void testGetDisabledMinBetweenRecords_minDataWithExtraRanges() {
    long startTime = 1659345900000L;
    long endTime = 1659345960000L;
    List<EntityDisableTime> disableTimes = new ArrayList<>();
    disableTimes.add(EntityDisableTime.builder().startTime(1659344900000L).endTime(1659345680000L).build());
    disableTimes.add(EntityDisableTime.builder().startTime(1659345700000L).endTime(1659345780000L).build());
    disableTimes.add(EntityDisableTime.builder().startTime(1659345800000L).endTime(1659345880000L).build());
    disableTimes.add(EntityDisableTime.builder().startTime(1659345900000L).endTime(1659345940000L).build());
    disableTimes.add(EntityDisableTime.builder().startTime(1659345950000L).endTime(1659345980000L).build());
    disableTimes.add(EntityDisableTime.builder().startTime(1659346500000L).endTime(1659346580000L).build());
    Pair<Long, Long> result = sliRecordService.getDisabledMinBetweenRecords(startTime, endTime, 0, disableTimes);
    assertThat(result.getLeft()).isEqualTo(1);
    assertThat(result.getRight()).isEqualTo(5);
  }

  @Test
  @Owner(developers = ARPITJ)
  @Category(UnitTests.class)
  public void testGetDisabledMinBetweenRecords_withLargeRange() {
    long startTime = 1659345900000L;
    long endTime = 1659346500000L;
    List<EntityDisableTime> disableTimes = new ArrayList<>();
    disableTimes.add(EntityDisableTime.builder().startTime(1659345900000L).endTime(1659346600000L).build());
    Pair<Long, Long> result = sliRecordService.getDisabledMinBetweenRecords(startTime, endTime, 0, disableTimes);
    assertThat(result.getLeft()).isEqualTo(10);
    assertThat(result.getRight()).isEqualTo(0);
  }

  @Test
  @Owner(developers = ARPITJ)
  @Category(UnitTests.class)
  public void testGetDisabledMinBetweenRecords_withSmallRange() {
    long startTime = 1659345900000L;
    long endTime = 1659346500000L;
    List<EntityDisableTime> disableTimes = new ArrayList<>();
    disableTimes.add(EntityDisableTime.builder().startTime(1659345900000L).endTime(1659346300000L).build());
    Pair<Long, Long> result = sliRecordService.getDisabledMinBetweenRecords(startTime, endTime, 0, disableTimes);
    assertThat(result.getLeft()).isEqualTo(6);
    assertThat(result.getRight()).isEqualTo(1);
  }

  @Test
  @Owner(developers = ARPITJ)
  @Category(UnitTests.class)
  public void testGetDisabledMinBetweenRecords_withExactRange() {
    long startTime = 1659345900000L;
    long endTime = 1659346500000L;
    List<EntityDisableTime> disableTimes = new ArrayList<>();
    disableTimes.add(EntityDisableTime.builder().startTime(1659345900000L).endTime(1659346500000L).build());
    Pair<Long, Long> result = sliRecordService.getDisabledMinBetweenRecords(startTime, endTime, 0, disableTimes);
    assertThat(result.getLeft()).isEqualTo(10);
    assertThat(result.getRight()).isEqualTo(1);
  }

  private void createData(Instant startTime, List<SLIState> sliStates) {
    List<SLIRecordParam> sliRecordParams = getSLIRecordParam(startTime, sliStates);
    sliRecordService.create(sliRecordParams, serviceLevelIndicator.getUuid(), verificationTaskId, 0);
  }

  private List<SLIRecordParam> getSLIRecordParam(Instant startTime, List<SLIState> sliStates) {
    List<SLIRecordParam> sliRecordParams = new ArrayList<>();
    for (int i = 0; i < sliStates.size(); i++) {
      SLIState sliState = sliStates.get(i);
      sliRecordParams.add(
          SLIRecordParam.builder().sliState(sliState).timeStamp(startTime.plus(Duration.ofMinutes(i))).build());
    }
    return sliRecordParams;
  }
  private SLIRecord getLastRecord(String sliId) {
    return hPersistence.createQuery(SLIRecord.class, excludeAuthority)
        .filter(SLIRecordKeys.sliId, sliId)
        .order(Sort.descending(SLIRecordKeys.timestamp))
        .get();
  }

  private void testGraphCalculation(List<SLIState> sliStates, SLIMissingDataType sliMissingDataType,
      List<Double> expectedSLITrend, List<Double> expectedBurndown, int expectedErrorBudgetRemaining) {
    testGraphCalculation(
        sliStates, sliMissingDataType, expectedSLITrend, expectedBurndown, expectedErrorBudgetRemaining, 0, 0);
  }

  private void testGraphCalculation(List<SLIState> sliStates, List<Double> expectedSLITrend,
      List<Double> expectedBurndown, int expectedErrorBudgetRemaining) {
    testGraphCalculation(
        sliStates, SLIMissingDataType.GOOD, expectedSLITrend, expectedBurndown, expectedErrorBudgetRemaining);
  }

  private void testGraphCalculation(List<SLIState> sliStates, SLIMissingDataType sliMissingDataType,
      List<Double> expectedSLITrend, List<Double> expectedBurndown, int expectedErrorBudgetRemaining,
      long customMinutesStart, long customMinutesEnd) {
    Instant startTime =
        DateTimeUtils.roundDownTo1MinBoundary(clock.instant().minus(Duration.ofMinutes(sliStates.size())));
    createData(startTime.minus(Duration.ofMinutes(4)), Arrays.asList(GOOD, NO_DATA, BAD, GOOD));
    createData(startTime, sliStates);

    Instant customStartTime = startTime.plus(Duration.ofMinutes(customMinutesStart));
    Instant customEndTime = startTime.plus(Duration.ofMinutes(sliStates.size() - customMinutesEnd + 1));

    SLOGraphData sloGraphData = sliRecordService.getGraphData(serviceLevelIndicator, startTime,
        startTime.plus(Duration.ofMinutes(sliStates.size() + 1)), 100, sliMissingDataType, 0,
        TimeRangeParams.builder().startTime(customStartTime).endTime(customEndTime).build());
    Duration duration = Duration.between(customStartTime, customEndTime);
    if (customMinutesEnd == 0) {
      assertThat(sloGraphData.getSloPerformanceTrend()).hasSize((int) duration.toMinutes() - 1);
    } else {
      assertThat(sloGraphData.getSloPerformanceTrend()).hasSize((int) duration.toMinutes());
    }
    List<Point> sloPerformanceTrend = sloGraphData.getSloPerformanceTrend();
    List<Point> errorBudgetBurndown = sloGraphData.getErrorBudgetBurndown();

    for (int i = 1; i < expectedSLITrend.size(); i++) {
      assertThat(sloPerformanceTrend.get(i).getTimestamp())
          .isEqualTo(customStartTime.plus(Duration.ofMinutes(i)).toEpochMilli());
      assertThat(sloPerformanceTrend.get(i).getValue()).isCloseTo(expectedSLITrend.get(i), offset(0.01));
      assertThat(errorBudgetBurndown.get(i).getTimestamp())
          .isEqualTo(customStartTime.plus(Duration.ofMinutes(i)).toEpochMilli());
      assertThat(errorBudgetBurndown.get(i).getValue()).isCloseTo(expectedBurndown.get(i), offset(0.01));
    }

    assertThat(sloGraphData.getErrorBudgetRemainingPercentage())
        .isCloseTo(expectedBurndown.get(errorBudgetBurndown.size() - 1), offset(0.01));
    assertThat(sloGraphData.getErrorBudgetRemaining()).isEqualTo(expectedErrorBudgetRemaining);
    assertThat(sloGraphData.isRecalculatingSLI()).isFalse();
  }

  private MonitoredService createMonitoredService() {
    MonitoredServiceDTO monitoredServiceDTO =
        builderFactory.monitoredServiceDTOBuilder().identifier("monitoredServiceIdentifier").build();
    monitoredServiceDTO.setSources(MonitoredServiceDTO.Sources.builder().build());
    monitoredServiceService.create(builderFactory.getContext().getAccountId(), monitoredServiceDTO);
    return monitoredServiceService.getMonitoredService(
        MonitoredServiceParams.builderWithProjectParams(builderFactory.getProjectParams())
            .monitoredServiceIdentifier("monitoredServiceIdentifier")
            .build());
  }

  private String createServiceLevelIndicator() {
    ServiceLevelIndicatorDTO serviceLevelIndicatorDTO =
        builderFactory.getThresholdServiceLevelIndicatorDTOBuilder().build();
    List<String> sliId = serviceLevelIndicatorService.create(builderFactory.getProjectParams(),
        Collections.singletonList(serviceLevelIndicatorDTO), "slo", "monitoredServiceIdentifier", null);
    return sliId.get(0);
  }

  private ServiceLevelIndicator getServiceLevelIndicator() {
    return serviceLevelIndicatorService.getServiceLevelIndicator(builderFactory.getProjectParams(), sliId);
  }
}
