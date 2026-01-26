package com.example.EcoGo.service.churn;

import org.springframework.stereotype.Service;

@Service
public class SupportServiceImpl implements SupportService {

    private final FeatureExtractor featureExtractor;
    private final ModelRunner modelRunner;
    private final Thresholds thresholds;

    public SupportServiceImpl(FeatureExtractor featureExtractor,
                              ModelRunner modelRunner,
                              Thresholds thresholds) {
        this.featureExtractor = featureExtractor;
        this.modelRunner = modelRunner;
        this.thresholds = thresholds;
    }

    @Override
    public ChurnRiskLevel getChurnRiskLevel(String userId) {
        ChurnFeatureVector fv = featureExtractor.extract(userId);

        if (fv == null || fv.isInsufficient()) {
            return ChurnRiskLevel.INSUFFICIENT_DATA;
        }

        double p = modelRunner.predictProbability(fv.toArray());
        return thresholds.toLevel(p);
    }
}
