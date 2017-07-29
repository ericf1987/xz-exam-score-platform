package com.xz.scorep.executor.aggregate;

/**
 * 主客观题得分,科目得分,项目总得分   得分来源类型
 * (先提供,后续改造统计)
 *
 * @author luckylo
 * @createTime 2017-07-29.
 */
public enum ScoreType {

    LOST("lost"),           //得分来源为丢卷(一定为0分)

    CHEAT("cheat"),         //得分来源为作弊(一定为0分)

    ABSENT("absent"),       //得分来源为缺考(一定为0分)

    ZERO("zero"),           //得分来源为卷面得分(且卷面得分为0分)

    PAPER("paper");         //得分来源为卷面得分(且卷面得分不为0分)

    private String name;

    ScoreType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "ScoreType{" +
                "name='" + name + '\'' +
                '}';
    }
}
