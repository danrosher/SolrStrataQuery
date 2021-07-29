//Copyright (c) 2021, Dan Rosher
//    All rights reserved.
//
//    This source code is licensed under the BSD-style license found in the
//    LICENSE file in the root directory of this source tree.


package com.github.danrosher.solr.search.strata;

import org.apache.solr.SolrTestCaseJ4;
import org.junit.BeforeClass;
import org.junit.Test;

public class StrataQParserPluginTest extends SolrTestCaseJ4 {

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.setProperty("enable.update.log", "false"); // schema12 doesn't support _version_
        initCore("solrconfig.xml", "schema12.xml");
    }

    @Test
    public void testStrataQParser() throws Exception {
        //3,2,4,1
        assertU(adoc("id", "0", "bid", "7","salary","10"));//0---
        assertU(adoc("id", "1", "bid", "6","salary","20"));//1
        assertU(adoc("id", "2", "bid", "5","salary","30"));//2...

        assertU(adoc("id", "3", "bid", "0","salary","60"));//3
        assertU(adoc("id", "4", "bid", "0","salary","50"));//4

        assertU(adoc("id", "5", "bid", "4","salary","50"));//5---
        assertU(adoc("id", "6", "bid", "4","salary","40"));//6
        assertU(adoc("id", "7", "bid", "2","salary","20"));//7
        assertU(adoc("id", "8", "bid", "1","salary","20"));//8...

        assertU(adoc("id", "9", "bid", "0","salary","40"));//9
        assertU(commit());

//        //normal
//        assertJQ(req(
//            "defType", "lucene",
//            "q","{!edismax}*:*",
//            "df","id",
//            "rows","10",
//            "sort","salary desc,id desc",
//            "fl", "*"),
//            "/response/docs/[0]/id=='3'",
//            "/response/docs/[1]/id=='5'",
//            "/response/docs/[2]/id=='4'",
//            "/response/docs/[3]/id=='9'",
//            "/response/docs/[4]/id=='6'",
//            "/response/docs/[5]/id=='2'",
//            "/response/docs/[6]/id=='8'",
//            "/response/docs/[7]/id=='7'",
//            "/response/docs/[8]/id=='1'",
//            "/response/docs/[9]/id=='0'"
//            );

        //by function query
        assertJQ(req(
            "rq", "{!stratify strata='3,2,4,1' strataSort='mul(bid,salary) desc,salary desc,id desc'}",
            "q","*:*",
            "sort","salary desc,id desc",
            "fl", "*,s_score:mul(bid,salary)"),

            //strata
            "/response/docs/[0]/id=='5'",
            "/response/docs/[0]/bid==4.0",
            "/response/docs/[0]/s_score==200.0",

            "/response/docs/[1]/id=='6'",
            "/response/docs/[1]/bid==4.0",
            "/response/docs/[1]/s_score==160.0",


            "/response/docs/[2]/id=='2'",
            "/response/docs/[2]/bid==5.0",
            "/response/docs/[2]/s_score==150.0",


            //main
            "/response/docs/[3]/id=='3'",
            "/response/docs/[3]/salary==60",

            "/response/docs/[4]/id=='4'",
            "/response/docs/[4]/salary==50",

            //strata
            "/response/docs/[5]/id=='1'",
            "/response/docs/[5]/bid==6.0",
            "/response/docs/[5]/s_score==120.0",


            "/response/docs/[6]/id=='0'",
            "/response/docs/[6]/bid==7.0",
            "/response/docs/[6]/s_score==70.0",


            "/response/docs/[7]/id=='7'",
            "/response/docs/[7]/bid==2.0",
            "/response/docs/[7]/s_score==40.0",


            "/response/docs/[8]/id=='8'",
            "/response/docs/[8]/bid==1.0",
            "/response/docs/[8]/s_score==20.0",

            //main
            "/response/docs/[9]/id=='9'",
            "/response/docs/[9]/salary==40"
        );


//        assertJQ(req(
//            "rq", "{!stratify strata='3,2,4,1' strataSort='bid desc,salary desc,id desc'}",
//            "q","*:*",
//            "sort","salary desc,id desc",
//            "fl", "*"),
//
//            //strata
//            "/response/docs/[0]/id=='0'",
//            "/response/docs/[0]/bid==7.0",
//
//            "/response/docs/[1]/id=='1'",
//            "/response/docs/[1]/bid==6.0",
//
//            "/response/docs/[2]/id=='2'",
//            "/response/docs/[2]/bid==5.0",
//
//            //main
//            "/response/docs/[3]/id=='3'",
//            "/response/docs/[3]/salary==60",
//
//            "/response/docs/[4]/id=='5'",
//            "/response/docs/[4]/salary==50",
//
//            //strata
//            "/response/docs/[5]/id=='6'",
//            "/response/docs/[5]/bid==4.0",
//
//            "/response/docs/[6]/id=='7'",
//            "/response/docs/[6]/bid==2.0",
//
//            "/response/docs/[7]/id=='8'",
//            "/response/docs/[7]/bid==1.0",
//
//            "/response/docs/[8]/id=='4'",
//            "/response/docs/[8]/bid==0.0",
//
//            //main
//            "/response/docs/[9]/id=='9'",
//            "/response/docs/[9]/salary==40"
//        );
//
//        assertJQ(req(
//            "defType", "lucene",
//            "rq", "{!stratify strata='3,2,4,1' strataSort='bid desc,salary desc,id desc'}",
//            "q","{!edismax}*:*",
//            "df","id",
//            "rows","10",
//            "sort","salary desc,id desc",
//            "fl", "*"),
//
//            //strata
//            "/response/docs/[0]/id=='0'",
//            "/response/docs/[0]/bid==7.0",
//
//            "/response/docs/[1]/id=='1'",
//            "/response/docs/[1]/bid==6.0",
//
//            "/response/docs/[2]/id=='2'",
//            "/response/docs/[2]/bid==5.0",
//
//            //main
//            "/response/docs/[3]/id=='3'",
//            "/response/docs/[3]/salary==60",
//
//            "/response/docs/[4]/id=='5'",
//            "/response/docs/[4]/salary==50",
//
//            //strata
//            "/response/docs/[5]/id=='6'",
//            "/response/docs/[5]/bid==4.0",
//
//            "/response/docs/[6]/id=='7'",
//            "/response/docs/[6]/bid==2.0",
//
//            "/response/docs/[7]/id=='8'",
//            "/response/docs/[7]/bid==1.0",
//
//            "/response/docs/[8]/id=='4'",
//            "/response/docs/[8]/bid==0.0",
//
//            //main
//            "/response/docs/[9]/id=='9'",
//            "/response/docs/[9]/salary==40"
//        );
//
//        assertJQ(req(
//            "defType", "lucene",
//            "rq", "{!stratify strata='3,2,4,1' strataSort='bid desc,salary desc'}",
//            "q","{!edismax}*:*",
//            "df","id",
//            "rows","10",
//            "sort","salary desc",
//            "fl", "*"),
//            "/response/docs/[0]/id=='0'",
//            "/response/docs/[0]/bid==7.0",
//
//            "/response/docs/[1]/id=='1'",
//            "/response/docs/[1]/bid==6.0",
//
//            "/response/docs/[2]/id=='2'",
//            "/response/docs/[2]/bid==5.0",
//
//            "/response/docs/[3]/id=='3'",
//            "/response/docs/[3]/salary==60",
//
//            "/response/docs/[4]/id=='4'",
//            "/response/docs/[4]/salary==50",
//
//            "/response/docs/[5]/id=='5'",
//            "/response/docs/[5]/bid==4.0",
//            "/response/docs/[5]/salary==50",
//
//            "/response/docs/[6]/id=='6'",
//            "/response/docs/[6]/bid==4.0",
//            "/response/docs/[6]/salary==40",
//
//            "/response/docs/[7]/id=='7'",
//            "/response/docs/[7]/bid==2.0",
//
//            "/response/docs/[8]/id=='8'",
//            "/response/docs/[8]/bid==1.0",
//
//            "/response/docs/[9]/id=='9'",
//            "/response/docs/[9]/salary==40"
//        );
//
//        assertJQ(req(
//            "defType", "lucene",
//            "rq", "{!stratify strata='1' strataSort='bid desc,salary desc'}",
//            "q","{!edismax}*:*",
//            "df","id",
//            "rows","10",
//            "sort","salary desc",
//            "fl", "*"),
//            "/response/docs/[0]/id=='0'",
//            "/response/docs/[0]/bid==7.0",
//
//            "/response/docs/[1]/id=='3'",
//            "/response/docs/[1]/salary==60",
//
//            "/response/docs/[2]/id=='4'",
//            "/response/docs/[2]/salary==50",
//
//            "/response/docs/[3]/id=='5'",
//            "/response/docs/[3]/salary==50",
//
//            "/response/docs/[4]/id=='6'",
//            "/response/docs/[4]/salary==40",
//
//            "/response/docs/[5]/id=='9'",
//            "/response/docs/[5]/salary==40",
//
//            "/response/docs/[6]/id=='2'",
//            "/response/docs/[6]/salary==30",
//
//            "/response/docs/[7]/id=='1'",
//            "/response/docs/[7]/salary==20",
//
//            "/response/docs/[8]/id=='7'",
//            "/response/docs/[8]/salary==20",
//
//            "/response/docs/[9]/id=='8'",
//            "/response/docs/[9]/salary==20"
//        );
//
//        assertQEx("zero strata",req(
//            "defType", "lucene",
//            "rq", "{!stratify strata='0,0' strataSort='bid desc,salary desc'}",
//            "q","{!edismax}*:*",
//            "df","id",
//            "rows","10",
//            "sort","salary desc",
//            "fl", "*"), SolrException.ErrorCode.BAD_REQUEST
//        );
//
//        assertQEx("null strata",req(
//            "defType", "lucene",
//            "rq", "{!stratify strata='' strataSort='bid desc,salary desc'}",
//            "q","{!edismax}*:*",
//            "df","id",
//            "rows","10",
//            "sort","salary desc",
//            "fl", "*"), SolrException.ErrorCode.BAD_REQUEST
//        );
//
//
//        //asking for more for strata
//        assertJQ(req(
//            "defType", "lucene",
//            "rq", "{!stratify strata='11' strataSort='bid desc,salary desc'}",
//            "q","{!edismax}*:*",
//            "df","id",
//            "rows","10",
//            "sort","salary desc",
//            "fl", "*"), "/response/docs/[0]/id=='0'",
//            "/response/docs/[1]/id=='1'",
//            "/response/docs/[2]/id=='2'",
//            "/response/docs/[3]/id=='5'",
//            "/response/docs/[4]/id=='6'",
//            "/response/docs/[5]/id=='7'",
//            "/response/docs/[6]/id=='8'",
//            "/response/docs/[7]/id=='3'",
//            "/response/docs/[8]/id=='4'",
//            "/response/docs/[9]/id=='9'"
//        );
//
//        //asking for more for both
//        assertJQ(req(
//            "defType", "lucene",
//            "rq", "{!stratify strata='11,11' strataSort='bid desc,salary desc'}",
//            "q","{!edismax}*:*",
//            "df","id",
//            "rows","10",
//            "sort","salary desc",
//            "fl", "*"), "/response/docs/[0]/id=='0'",
//            "/response/docs/[1]/id=='1'",
//            "/response/docs/[2]/id=='2'",
//            "/response/docs/[3]/id=='5'",
//            "/response/docs/[4]/id=='6'",
//            "/response/docs/[5]/id=='7'",
//            "/response/docs/[6]/id=='8'",
//            "/response/docs/[7]/id=='3'",
//            "/response/docs/[8]/id=='4'",
//            "/response/docs/[9]/id=='9'"
//        );
//
//        //alternate strataSort
//        assertJQ(req(
//            "defType", "lucene",
//            "rq", "{!stratify strata='11,11' strataSort='bid desc'}",
//            "q","{!edismax}*:*",
//            "df","id",
//            "rows","10",
//            "sort","salary desc",
//            "fl", "*"), "/response/docs/[0]/id=='0'",
//            "/response/docs/[1]/id=='1'",
//            "/response/docs/[2]/id=='2'",
//            "/response/docs/[3]/id=='5'",
//            "/response/docs/[4]/id=='6'",
//            "/response/docs/[5]/id=='7'",
//            "/response/docs/[6]/id=='8'",
//            "/response/docs/[7]/id=='3'",
//            "/response/docs/[8]/id=='4'",
//            "/response/docs/[9]/id=='9'"
//        );
    }
}
