/*
 * Copyright 2020 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package software.wings.search;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.OwnedBy;

import software.wings.search.framework.AdvancedSearchQuery;
import software.wings.search.framework.SearchResults;

import org.hibernate.validator.constraints.NotBlank;

@OwnedBy(PL)
public interface SearchService {
  SearchResults getSearchResults(@NotBlank String query, @NotBlank String accountId);

  SearchResults getSearchResults(@NotBlank String accountId, AdvancedSearchQuery advancedSearchQuery);
}
