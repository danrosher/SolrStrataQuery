# Strata QParser

Strata QParser merges the main query sort and a strata sort into an alternate resultset. This is particular useful if 
for example you want to have 'sponsored' or 'elevated' docs interleaved with the 'standard' sorted results.

The documents to be returned from the two sorts are interleaved according a 'strata' parameter provided for the search.

Strata QParser extends RankQuery and only works with the 'rq' parameter. For example:
 
    rq={!stratify strata='3,2,4,1' strataSort='bid desc,salary desc'}"
    
Here the parser is identified with 'stratify'. The interleaving of sorts into the results sets are with the 'strata'
parameter. The sorting for the alternate 'stratify' parser is with 'strataSort'.

So for strata='3,2,4,1', we use 'strataSort' for all even indicies i.e. [3,4] and main 'sort' for odd indicies [2,1]

Search results are returned in strataSort then sort interleaved. All other results are then from the main sort.

As an example, imagine we have the following docs:

|id  |bid |salary|
|----|----|----|
|0   |7   |10|
|1   |6   |20|
|2   |5   |30|
|3   |0   |60|
|4   |0   |50|
|5   |4   |50|
|6   |4   |40|
|7   |2   |20|
|8   |1   |20|
|9   |0   |40|

if we do a search like 

    q=*:*&sort=salary+desc,id+desc we have the following order

|id  |bid |salary
|----|----|---|
|3   |0   |60
|4   |0   |50
|5   |4   |50
|9   |0   |40
|6   |4   |40
|2   |5   |30
|8   |1   |20
|7   |2   |20
|1   |6   |20
|0   |7   |10

Now if we do the following search with stratify 

    q=*:*&sort=salary+desc,id+desc&rq={!stratify strata='3,2,4,1' strataSort='bid desc,salary desc'}

we get:

|id  |bid |salary|comments|
|----|----|------|-----|
|0   |7   |10|     strata ...3
|1   |6   |20|     strata
|2   |5   |30|     strata
|3   |0   |60|     main  ....2
|5   |4   |50|     main
|6   |4   |40|     strata  ..4
|7   |2   |20|     strata
|8   |1   |20|     strata
|4   |0   |50|     strata
|9   |0   |40|     main  ....1

See the alternate order?

 
 ## Installation
 
 ./gradlew clean jar
 
 ### Add to solrconfig.xml
 
 e.g 
 
    <config>
    ... 
    <lib dir="${solr.install.dir:../../../..}/dist/" regex="SolrStrataQuery-1.0-SNAPSHOT.jar" />
    ....
    <queryParser name="stratify" class="com.github.danrosher.solr.search.strata.StrataQParserPlugin"/>
    
    # "stratify" can be any string

    
 ## Usage 

 ### Stratyify sort results sort by bid,salary,id and then salary,id
    q=*:*&sort=salary+desc,id+desc&rq={!stratify strata='3,2,4,1' strataSort='bid desc,salary+desc,id+desc'}
        
 ### Stratyify sort results sort by bid only then and then salary only
    q=*:*&sort=salary+desc&rq={!stratify strata='3,2,4,1' strataSort='bid desc'}
    
 ### Stratyify just the top 3 bids, then all others from main sort
    q=*:*&sort=salary+desc&rq={!stratify strata='3' strataSort='bid desc'}   

   