package net.minecraft.server.v1_8_R3;

public class ScoreboardStatisticCriteria extends ScoreboardBaseCriteria {

    private final Statistic j;

    public ScoreboardStatisticCriteria(Statistic statistic) {
        super(statistic.name);
        this.j = statistic;
    }
}
