/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.repositories.instancestats;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.timescaledb.DBUtils;
import io.harness.timescaledb.TimeScaleDBService;

import com.google.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor(onConstructor = @__({ @Inject }))
@Slf4j
@OwnedBy(HarnessTeam.PL)
public class InstanceStatsRepositoryImpl implements InstanceStatsRepository {
  private TimeScaleDBService timeScaleDBService;
  private static final int MAX_RETRY_COUNT = 3;

  public Timestamp getLastSnapshotTime(String accountId, String orgId, String projectId) throws Exception {
    int totalTries = 0;
    while (totalTries <= MAX_RETRY_COUNT) {
      ResultSet resultSet = null;
      try (
          Connection dbConnection = timeScaleDBService.getDBConnection();
          PreparedStatement statement = dbConnection.prepareStatement(InstanceStatsQuery.FETCH_LATEST_RECORD.query())) {
        statement.setString(1, accountId);
        statement.setString(2, orgId);
        statement.setString(3, projectId);
        resultSet = statement.executeQuery();
        return parseInstanceStatsRecord(resultSet);
      } catch (SQLException ex) {
        if (totalTries == MAX_RETRY_COUNT) {
          log.error("Error while fetching latest instance stats record after all retries");
          throw ex;
        }
        log.warn("Could not fetch latest instance stats record. Retrying again. Retry number: {}", totalTries, ex);
        totalTries++;
      } catch (Exception ex) {
        log.error("Error while fetching latest instance stats record");
        throw ex;
      } finally {
        DBUtils.close(resultSet);
      }
    }
    return null;
  }

  // ------------------------------- PRIVATE METHODS ------------------------------

  private Timestamp parseInstanceStatsRecord(ResultSet resultSet) throws SQLException {
    if (resultSet != null && resultSet.next()) {
      return resultSet.getTimestamp(InstanceStatsFields.REPORTEDAT.fieldName());
    }
    return null;
  }
}
