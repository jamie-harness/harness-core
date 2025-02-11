-- Copyright 2021 Harness Inc. All rights reserved.
-- Use of this source code is governed by the PolyForm Shield 1.0.0 license
-- that can be found in the licenses directory at the root of this repository, also available at
-- https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.

BEGIN;

CREATE TABLE IF NOT EXISTS public.cg_pipelines (
    id text NOT NULL,
    account_id text NOT NULL,
    app_id text NOT NULL,
    name text,
    env_ids text[],
    workflow_ids text[],
    created_at bigint,
    last_updated_at bigint,
    created_by text,
    last_updated_by text,

    PRIMARY KEY(id)
);

COMMIT;
