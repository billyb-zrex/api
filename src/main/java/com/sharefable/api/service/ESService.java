package com.sharefable.api.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.sharefable.api.annotations.ESQueryable;
import com.sharefable.api.common.ESIndices;
import com.sharefable.api.entity.AssetContent;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ESService {
    private final ElasticsearchClient client;

    @Autowired
    public ESService(ElasticsearchClient client) {
        this.client = client;
    }

    public AssetContent insertDocument(AssetContent data) {
        IndexResponse resp;
        try {
            resp = client.index(i -> i
                .index(ESIndices.AssetContent)
                .document(data)
            );
            data.setId(resp.id());
            return data;
        } catch (IOException | ElasticsearchException e) {
            log.error("Error while index document {} with error message {}", data, e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    void deleteAllDocumentsFromIndex() throws IOException {
        client.deleteByQuery(d -> d.index(ESIndices.AssetContent)
            .query(q -> q.matchAll(m -> m)));
    }

    private Query getQueryFromField(Field field, AssetContent object, ESQueryable.SearchType type, boolean shouldAddKeyword) {
        if (type == ESQueryable.SearchType.Term) {
            return TermQuery.of(m -> {
                String fieldName = field.getName() + (shouldAddKeyword ? ".keyword" : "");
                try {
                    Object val = field.get(object);
                    if (field.getType() == Long.class) {
                        return m.field(fieldName).value((Long) val);
                    } else if (field.getType() == Integer.class) {
                        return m.field(fieldName).value((Integer) val);
                    } else {
                        // cast to string type
                        return m.field(fieldName).value((String) val);
                    }
                } catch (IllegalAccessException e) {
                    log.error("Error while getting creating term query for field {} with value with message {}",
                        fieldName, e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            })._toQuery();

        } else {
            return MatchQuery.of(m -> {
                String fieldName = field.getName();
                try {
                    Object val = field.get(object);
                    if (field.getType() == Long.class) {
                        return m.field(fieldName).query((Long) val);
                    } else if (field.getType() == Integer.class) {
                        return m.field(fieldName).query((Integer) val);
                    } else {
                        // cast to string type
                        return m.field(fieldName).query((String) val);
                    }
                } catch (IllegalAccessException e) {
                    log.error("Error while getting creating term query for field {} with value with message {}",
                        fieldName, e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            })._toQuery();
        }
    }

    public AssetContent getExactDocument(AssetContent data) {
        List<Pair<Double, AssetContent>> match = getMatchedDocuments(data, true);
        if (match == null || match.size() == 0) {
            return null;
        }
        return match.get(0).getValue1();
    }

    public List<AssetContent> getMatchedDocuments(AssetContent data) {
        List<Pair<Double, AssetContent>> match = getMatchedDocuments(data, false);
        if (match == null || match.size() == 0) {
            return null;
        }
        return match.stream().map(Pair::getValue1).collect(Collectors.toList());
    }

    public AssetContent updateDocument(AssetContent partialDocument) {
        try {
            UpdateResponse<AssetContent> resp = client.update(u ->
                    u.index(ESIndices.AssetContent).id(partialDocument.getId()).doc(partialDocument)
                        .source(s -> s.fetch(true)),
                AssetContent.class);
            if (resp.get() != null) {
                return resp.get().source();
            }
            return null;
        } catch (IOException | ElasticsearchException e) {
            log.error("Error while updating document {} with error message {}", partialDocument, e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private List<Pair<Double, AssetContent>> getMatchedDocuments(AssetContent data, boolean isExactMatch) {
        Field[] fields = data.getClass().getDeclaredFields();
        List<Query> filterQueries = new ArrayList<>();
        List<Query> matchQueries = new ArrayList<>();
        for (Field field : fields) {
            field.setAccessible(true);
            boolean shouldAddFieldInQuery = true;
            ESQueryable.SearchType type = isExactMatch ? ESQueryable.SearchType.Term : ESQueryable.SearchType.Match;
            ESQueryable queryAnnot = field.getAnnotation(ESQueryable.class);
            if (field.isAnnotationPresent(ESQueryable.class)) {
                if (queryAnnot.exclude()) {
                    shouldAddFieldInQuery = false;
                } else if (!isExactMatch) {
                    type = queryAnnot.type();
                }
            }

            if (shouldAddFieldInQuery) {
                try {
                    if (field.get(data) == null) {
                        continue;
                    }
                } catch (IllegalAccessException e) {
                    log.error("Can't access field value of {} with error {}", field.getName(), e.getMessage());
                    e.printStackTrace();
                    continue;
                }
                Query query = getQueryFromField(field, data, type, queryAnnot.isAlsoKeyword());
                List<Query> queries = type == ESQueryable.SearchType.Term ? filterQueries : matchQueries;
                queries.add(query);
                log.info("Query to ES: {}", query.toString());
            }
        }

        try {
            SearchResponse<AssetContent> resp = client.search(t -> t.index(ESIndices.AssetContent)
                    .query(q -> q.bool(b -> b.filter(filterQueries).must(matchQueries))),
                AssetContent.class);
            List<Hit<AssetContent>> hits = resp.hits().hits();
            List<Pair<Double, AssetContent>> pairs = new ArrayList<>();
            for (Hit<AssetContent> hit : hits) {
                AssetContent source = hit.source();
                if (source != null) {
                    source.setId(hit.id());
                    Double score = hit.score();
                    pairs.add(Pair.with(score, source));
                }
            }
            return pairs;
        } catch (IOException | ElasticsearchException e) {
            log.error("Error while querying matches {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
