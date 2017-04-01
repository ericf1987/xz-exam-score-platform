package com.xz.scorep.executor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "aggr")
public class AggregateConfig {

    private int optionPoolSize;

    private int rankPoolSize;

    private int segmentsPoolSize;

    private int objectivePoolSize;

    private int subjectPoolSize;

    public int getOptionPoolSize() {
        return optionPoolSize;
    }

    public void setOptionPoolSize(int optionPoolSize) {
        this.optionPoolSize = optionPoolSize;
    }

    public int getRankPoolSize() {
        return rankPoolSize;
    }

    public void setRankPoolSize(int rankPoolSize) {
        this.rankPoolSize = rankPoolSize;
    }

    public int getSegmentsPoolSize() {
        return segmentsPoolSize;
    }

    public void setSegmentsPoolSize(int segmentsPoolSize) {
        this.segmentsPoolSize = segmentsPoolSize;
    }

    public int getObjectivePoolSize() {
        return objectivePoolSize;
    }

    public void setObjectivePoolSize(int objectivePoolSize) {
        this.objectivePoolSize = objectivePoolSize;
    }

    public int getSubjectPoolSize() {
        return subjectPoolSize;
    }

    public void setSubjectPoolSize(int subjectPoolSize) {
        this.subjectPoolSize = subjectPoolSize;
    }
}
