//Copyright (c) 2021, Dan Rosher
//    All rights reserved.
//
//    This source code is licensed under the BSD-style license found in the
//    LICENSE file in the root directory of this source tree.


package com.github.danrosher.solr.search.strata;

import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntSet;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Scorable;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopDocsCollector;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QueryCommand;
import org.apache.solr.search.SortSpecParsing;

import java.io.IOException;

public class StrataCollector extends TopDocsCollector {

    private final TopDocsCollector mainCollector;
    private final TopDocsCollector strataCollector;
    private final int[] strata;
    private int strataCount;

    public StrataCollector(int len, QueryCommand cmd, IndexSearcher searcher,
                           String strataStr, int[] strata,
                           SolrQueryRequest req) throws IOException {
        super(null);
        this.strata = strata;

        for(int i=0;i<strata.length;i+=2){
            strataCount += strata[i];
        }

        Sort sort = cmd.getSort();
        if (null == sort) {
            mainCollector = TopScoreDocCollector.create(len, Integer.MAX_VALUE);
        } else {
            // we have a sort
            final Sort mainWeightedSort = sort.rewrite(searcher);
            mainCollector = TopFieldCollector.create(mainWeightedSort, len, null, Integer.MAX_VALUE);
        }

        Sort strataSort = SortSpecParsing.parseSortSpec(strataStr, req)
            .getSort();
        final Sort strataWeightedSort = strataSort.rewrite(searcher);
        strataCollector = TopFieldCollector.create(strataWeightedSort, Math.min(len, strataCount), null, Integer.MAX_VALUE);
    }

    @Override
    public LeafCollector getLeafCollector(LeafReaderContext context) throws IOException {
        final LeafCollector mainLeafCollector = mainCollector.getLeafCollector(context);
        final LeafCollector strataLeafCollector = strataCollector.getLeafCollector(context);
        return new LeafCollector() {
            @Override
            public void setScorer(Scorable scorer) throws IOException {
                mainLeafCollector.setScorer(scorer);
                strataLeafCollector.setScorer(scorer);
            }

            @Override
            public void collect(int doc) throws IOException {
                mainLeafCollector.collect(doc);
                strataLeafCollector.collect(doc);
            }
        };
    }

    @Override
    public ScoreMode scoreMode() {
        return ScoreMode.COMPLETE;
    }

    @Override
    public int getTotalHits() {
        return mainCollector.getTotalHits();
    }

    @Override
    public TopDocs topDocs(int start, int howMany) {


        TopDocs mainDocs = mainCollector.topDocs(0, start + howMany);
        if (mainDocs.totalHits.value == 0 || mainDocs.scoreDocs.length == 0) {
            return mainDocs;
        }

        ScoreDoc[] mainScoreDocs = mainDocs.scoreDocs;
        int mainScoreDocsLen = mainScoreDocs.length;

        //Note: number of strataDocs == number of mainDocs, just reordered
        TopDocs strataDocs = strataCollector.topDocs(0, Math.min(start + howMany, strataCount));
        ScoreDoc[] strataScoreDocs = strataDocs.scoreDocs;

        int strataScoreDocsLen = strataScoreDocs.length;

        ScoreDoc[] reRankScoreDocs = new ScoreDoc[mainScoreDocs.length];
        int sPos = 0, mPos = 0, pos = 0;
        IntSet seen = new IntHashSet();
        for (int i = 0; i < strata.length; i++) {

            int len = strata[i];

            //from strataScoreDocs
            if (i % 2 == 0) {
                for (int fin = sPos + len; sPos < fin; sPos++) {
                    if (sPos == strataScoreDocsLen) break;
                    ScoreDoc sdoc = strataScoreDocs[sPos];
                    //add if not already seen
                    if (!seen.contains(sdoc.doc)) {
                        reRankScoreDocs[pos++] = sdoc;
                        seen.add(sdoc.doc);
                    }
                    //push fin out further
                    else {
                        fin++;
                    }
                }
            }

            //from mainScoreDocs
            else {
                for (int fin = mPos + len; mPos < fin; mPos++) {
                    if (mPos == mainScoreDocsLen) break;
                    ScoreDoc sdoc = mainScoreDocs[mPos];
                    //add if not already seen
                    if (!seen.contains(sdoc.doc)) {
                        reRankScoreDocs[pos++] = sdoc;
                        seen.add(sdoc.doc);
                    }
                    //push fin out further
                    else {
                        fin++;
                    }
               }
            }
        }

        for (; mPos < mainScoreDocsLen && pos < mainScoreDocsLen; mPos++) {
            ScoreDoc sdoc = mainScoreDocs[mPos];
            if (!seen.contains(sdoc.doc)) reRankScoreDocs[pos++] = sdoc;
        }

        return new TopDocs(mainDocs.totalHits, reRankScoreDocs);
    }
}