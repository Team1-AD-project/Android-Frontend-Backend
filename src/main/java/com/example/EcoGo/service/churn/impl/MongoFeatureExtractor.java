package com.example.EcoGo.service.churn.impl;

import com.example.EcoGo.service.churn.ChurnFeatureVector;
import com.example.EcoGo.service.churn.FeatureExtractor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class MongoFeatureExtractor implements FeatureExtractor {

    private final MongoTemplate mongoTemplate;


    private final List<String> candidateCollections = List.of("users", "user", "Users", "User");

    public MongoFeatureExtractor(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public ChurnFeatureVector extract(String userId) {
        Document doc = findUserDoc(userId);
        if (doc == null) {
            return new ChurnFeatureVector(new float[]{0, 0, 0, 0, 0, 0, 0});
        }


        float f0 = getFloat(doc, "stats.totalTrips");
        float f1 = getFloat(doc, "stats.activeDays");
        float f2 = getFloat(doc, "stats.completedTasks");
        float f3 = getFloat(doc, "totalCarbon");
        float f4 = getFloat(doc, "totalPoints");
        float f5 = getFloat(doc, "currentPoints");
        float f6 = getVipSignal(doc);

        return new ChurnFeatureVector(new float[]{f0, f1, f2, f3, f4, f5, f6});
    }

    private float getVipSignal(Document doc) {
        // 兼容两套 vip 结构：
        // 1) vip.isActive: boolean
        // 2) vip.level: int
        Float isActive = tryParseFloat(getByPath(doc, "vip.isActive"));
        if (isActive != null) {
            return (isActive != 0.0f) ? 1.0f : 0.0f;
        }

        Float level = tryParseFloat(getByPath(doc, "vip.level"));
        if (level != null) {
            // level 0/1/2... 直接作为数值信号
            return level;
        }

        return 0.0f;
    }

    private Document findUserDoc(String userId) {
        for (String col : candidateCollections) {
            Document doc = findByIdOrUserid(col, userId);
            if (doc != null) return doc;
        }
        return null;
    }

    private Document findByIdOrUserid(String collection, String userId) {
        // 1) _id = userId（字符串）
        Query q1 = new Query(Criteria.where("_id").is(userId));
        Document d1 = mongoTemplate.findOne(q1, Document.class, collection);
        if (d1 != null) return d1;

        // 2) _id = ObjectId(userId)
        try {
            ObjectId oid = new ObjectId(userId);
            Query q2 = new Query(Criteria.where("_id").is(oid));
            Document d2 = mongoTemplate.findOne(q2, Document.class, collection);
            if (d2 != null) return d2;
        } catch (IllegalArgumentException ignored) { }

        // 3) userid（注意：你库里字段名是小写 userid）
        Query q3 = new Query(Criteria.where("userid").is(userId));
        Document d3 = mongoTemplate.findOne(q3, Document.class, collection);
        if (d3 != null) return d3;

        // 4) 兼容旧写法 userId
        Query q4 = new Query(Criteria.where("userId").is(userId));
        Document d4 = mongoTemplate.findOne(q4, Document.class, collection);
        if (d4 != null) return d4;

        return null;
    }

    private float getFloat(Document doc, String path) {
        Object v = getByPath(doc, path);
        Float parsed = tryParseFloat(v);
        return parsed != null ? parsed : 0.0f;
    }

    /**
     * 支持 "stats.totalTrips" 这种点路径读取
     */
    private Object getByPath(Document doc, String path) {
        if (doc == null || path == null || path.isEmpty()) return null;

        String[] parts = path.split("\\.");
        Object curr = doc;
        for (String p : parts) {
            if (!(curr instanceof Document d)) return null;
            curr = d.get(p);
            if (curr == null) return null;
        }
        return curr;
    }

    private Float tryParseFloat(Object v) {
        if (v == null) return null;
        if (v instanceof Boolean b) return b ? 1.0f : 0.0f;
        if (v instanceof Number n) return n.floatValue();
        try {
            String s = String.valueOf(v).trim();
            if (s.isEmpty()) return null;
            return Float.parseFloat(s);
        } catch (Exception e) {
            return null;
        }
    }
}
