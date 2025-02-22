-- Copyright 2020 Harness Inc. All rights reserved.
-- Use of this source code is governed by the PolyForm Shield 1.0.0 license
-- that can be found in the licenses directory at the root of this repository, also available at
-- https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.

---------- VERIFICATION_WORKFLOW_STATS TABLE START -----------
BEGIN;
CREATE TABLE IF NOT EXISTS VERIFICATION_WORKFLOW_STATS (
	ACCOUNT_ID TEXT NOT NULL,
	APP_ID TEXT NOT NULL,
	SERVICE_ID TEXT NOT NULL,
	WORKFLOW_ID TEXT,
    WORKFLOW_EXECUTION_ID TEXT,
    STATE_EXECUTION_ID TEXT,
    CV_CONFIG_ID TEXT,
    STATE_TYPE TEXT NOT NULL,
	START_TIME TIMESTAMP NOT NULL,
	END_TIME TIMESTAMP NOT NULL,
	STATUS VARCHAR(20),
	IS_247 BOOL,
	HAS_DATA BOOL,
	IS_ROLLED_BACK BOOL
);
COMMIT;


SELECT CREATE_HYPERTABLE('VERIFICATION_WORKFLOW_STATS','end_time',if_not_exists => TRUE);
BEGIN;
CREATE INDEX IF NOT EXISTS VWS_ACCOUNT_INDEX ON VERIFICATION_WORKFLOW_STATS(ACCOUNT_ID,END_TIME DESC);
CREATE INDEX IF NOT EXISTS VWS_APP_ID_INDEX ON VERIFICATION_WORKFLOW_STATS(APP_ID,END_TIME DESC);
CREATE INDEX IF NOT EXISTS VWS_STATUS_INDEX ON VERIFICATION_WORKFLOW_STATS(STATUS,END_TIME DESC);
CREATE INDEX IF NOT EXISTS VWS_TYPE_INDEX ON VERIFICATION_WORKFLOW_STATS(STATE_TYPE,END_TIME DESC);
CREATE INDEX IF NOT EXISTS VWS_DATA_INDEX ON VERIFICATION_WORKFLOW_STATS(HAS_DATA,END_TIME DESC);
CREATE INDEX IF NOT EXISTS VWS_ROLL_INDEX ON VERIFICATION_WORKFLOW_STATS(IS_ROLLED_BACK,END_TIME DESC);

CREATE INDEX IF NOT EXISTS VWS_COMPOSITE_INDEX ON VERIFICATION_WORKFLOW_STATS(STATUS, HAS_DATA, IS_ROLLED_BACK, STATE_TYPE,END_TIME DESC);

COMMIT;

---------- VERIFICATION_WORKFLOW_STATS TABLE END -----------
