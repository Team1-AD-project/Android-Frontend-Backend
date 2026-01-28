package com.example.EcoGo.service.churn.impl;

import com.example.EcoGo.service.churn.ChurnFeatureVector;
import com.example.EcoGo.service.churn.FeatureExtractor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class MongoFeatureExtractor implements FeatureExtractor {

    private static final String USERS_COL = "users";

    private final MongoTemplate mongoTemplate;

    public MongoFeatureExtractor(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public ChurnFeatureVector extract(String userId) {
        System.out.println(">>> MongoFeatureExtractor CALLED, userId=" + userId);

        Document user = findUserDoc(userId);
        if (user == null) {
            System.out.println(">>> user NOT FOUND for userId=" + userId);
            return new ChurnFeatureVector(new float[0]);
        }

        Document stats = getDoc(user, "stats");
        Document vip = getDoc(user, "vip");

        // stats 兼容：totalTrips / total_trips、activeDays / active_days、completedTasks / completed_tasks
        float totalTrips = num(stats, "totalTrips", "total_trips");
        float activeDays = num(stats, "activeDays", "active_days");
        float completedTasks = num(stats, "completedTasks", "completed_tasks");

        // user 顶层：totalCarbon / total_carbon, totalPoints / total_points, currentPoints / current_points
        float totalCarbon = num(user, "totalCarbon", "total_carbon");
        float totalPoints = num(user, "totalPoints", "total_points");
        float currentPoints = num(user, "currentPoints", "current_points");

        // vip 兼容：level / isActive / is_active
        float vipValue = 0f;
        if (vip != null) {
            vipValue = num(vip, "level"); // 优先 level
            if (vipValue == 0f) {
                boolean isActive = bool(vip, "isActive", "is_active");
                vipValue = isActive ? 1f : 0f;
            }
        }

        float[] features = new float[]{ totalTrips, activeDays, completedTasks, totalCarbon, totalPoints, currentPoints, vipValue };

        System.out.println(">>> extracted features = " + Arrays.toString(features));
        return new ChurnFeatureVector(features);
    }

    private Document findUserDoc(String userId) {
        Document d = mongoTemplate.findOne(
                Query.query(Criteria.where("userid").is(userId)),
                Document.class,
                USERS_COL
        );
        if (d != null) return d;


        d = mongoTemplate.findOne(
                Query.query(Criteria.where("_id").is(userId)),
                Document.class,
                USERS_COL
        );
        if (d != null) return d;


        try {
            ObjectId oid = new ObjectId(userId);
            return mongoTemplate.findOne(
                    Query.query(Criteria.where("_id").is(oid)),
                    Document.class,
                    USERS_COL
            );
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static Document getDoc(Document doc, String key) {
        if (doc == null) return null;
        Object v = doc.get(key);
        return (v instanceof Document) ? (Document) v : null;
    }

    private static float num(Document doc, String... keys) {
        if (doc == null) return 0f;
        for (String k : keys) {
            Object v = doc.get(k);
            if (v instanceof Number) return ((Number) v).floatValue();
        }
        return 0f;
    }

    private static boolean bool(Document doc, String... keys) {
        if (doc == null) return false;
        for (String k : keys) {
            Object v = doc.get(k);
            if (v instanceof Boolean) return (Boolean) v;
        }
        return false;
    }
}
