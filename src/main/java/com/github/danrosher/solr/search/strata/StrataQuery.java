//Copyright (c) 2021, Dan Rosher
//    All rights reserved.
//
//    This source code is licensed under the BSD-style license found in the
//    LICENSE file in the root directory of this source tree.


package com.github.danrosher.solr.search.strata;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.TopDocsCollector;
import org.apache.lucene.search.Weight;
import org.apache.solr.handler.component.MergeStrategy;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QueryCommand;
import org.apache.solr.search.RankQuery;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class StrataQuery extends RankQuery {

    private final String strataStr;
    private final int[] strata;
    private final SolrQueryRequest req;
    private Query mainQuery;

    public StrataQuery(String strataStr, int[] strata,SolrQueryRequest req) {
        this.strataStr = strataStr;
        this.strata = strata;
        this.req = req;
    }

    @Override
    public TopDocsCollector getTopDocsCollector(int len, QueryCommand cmd, IndexSearcher searcher) throws IOException {
        return new StrataCollector(len,cmd,searcher,strataStr,strata,req);
    }

    @Override
    public MergeStrategy getMergeStrategy() {
        return null;
    }

    @Override
    public RankQuery wrap(Query _mainQuery) {
        if(_mainQuery != null){
            this.mainQuery = _mainQuery;
        }
        return this;
    }

    @Override
    public Weight createWeight(IndexSearcher searcher, ScoreMode scoreMode, float boost) throws IOException{
        return mainQuery.createWeight(searcher, scoreMode, boost);
    }

    @Override
    public Query rewrite(IndexReader reader) throws IOException {
        Query q = mainQuery.rewrite(reader);
        if (q != mainQuery) {
            return rewrite(q);
        }
        return super.rewrite(reader);
    }

    protected Query rewrite(Query rewrittenMainQuery) {
        return new StrataQuery(strataStr,strata,req).wrap(rewrittenMainQuery);
    }

    @Override
    public String toString(String field) {
        return "stratify(strata='"+ Arrays.toString(strata)+"' strataSort='"+strataStr+"')";
    }

    @Override
    public boolean equals(Object o) {
        if (this.getClass() != o.getClass()) return false;
        StrataQuery other = (StrataQuery) o;
        return this.strataStr
            .equals(other.strataStr)
            && Arrays.equals(strata,other.strata)
            && mainQuery.equals(other.mainQuery);
    }

    @Override
    public int hashCode() {
        return Objects.hash(strataStr,strata,mainQuery);
    }
}
