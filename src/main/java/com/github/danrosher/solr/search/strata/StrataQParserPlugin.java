//Copyright (c) 2021, Dan Rosher
//    All rights reserved.
//
//    This source code is licensed under the BSD-style license found in the
//    LICENSE file in the root directory of this source tree.


package com.github.danrosher.solr.search.strata;

import org.apache.lucene.search.Query;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;

import java.util.Arrays;

public class StrataQParserPlugin extends QParserPlugin {

    static final String STRATA_SORT = "strataSort";
    static final String STRATA = "strata";

    @Override
    public QParser createParser(String query, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
        return new StrataQParser(query, localParams, params, req);
    }

    private static class StrataQParser extends QParser {
        public StrataQParser(String query, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
            super(query, localParams, params, req);
        }

        @Override
        public Query parse() {
            String strataSortString = localParams.get(STRATA_SORT);
            if (strataSortString == null || strataSortString.trim()
                .isEmpty()) {
                throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, STRATA_SORT + " parameter is mandatory");
            }

            String strataStr = localParams.get(STRATA);
            if (strataStr == null || strataStr.trim()
                .isEmpty()) {
                throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, STRATA + " parameter is mandatory");
            }
            int[] strata = Arrays
                .stream(strataStr.split(","))
                .mapToInt(Integer::parseInt)
                .toArray();

            for (int stratum : strata)
                if (stratum == 0)
                    throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, STRATA + " values cannot be zero");

            return new StrataQuery(strataSortString, strata, req);
        }
    }
}
