package net.minecraft.server.v1_8_R3;

import com.google.common.collect.Maps;
import java.util.Map;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.event.Cancellable;

public class StatisticManager {

    protected final Map<Statistic, StatisticWrapper> a = Maps.newConcurrentMap();

    public StatisticManager() {}

    public boolean hasAchievement(Achievement achievement) {
        return this.getStatisticValue(achievement) > 0;
    }

    public boolean b(Achievement achievement) {
        return achievement.c == null || this.hasAchievement(achievement.c);
    }

    public void b(EntityHuman entityhuman, Statistic statistic, int i) {
        if (!statistic.d() || this.b((Achievement) statistic)) {
            Cancellable cancellable = CraftEventFactory.handleStatisticsIncrease(entityhuman, statistic, this.getStatisticValue(statistic), i);

            if (cancellable != null && cancellable.isCancelled()) {
                return;
            }

            this.setStatistic(entityhuman, statistic, this.getStatisticValue(statistic) + i);
        }

    }

    public void setStatistic(EntityHuman entityhuman, Statistic statistic, int i) {
        StatisticWrapper statisticwrapper = (StatisticWrapper) this.a.get(statistic);

        if (statisticwrapper == null) {
            statisticwrapper = new StatisticWrapper();
            this.a.put(statistic, statisticwrapper);
        }

        statisticwrapper.a(i);
    }

    public int getStatisticValue(Statistic statistic) {
        StatisticWrapper statisticwrapper = (StatisticWrapper) this.a.get(statistic);

        return statisticwrapper == null ? 0 : statisticwrapper.a();
    }

    public <T extends IJsonStatistic> T b(Statistic statistic) {
        StatisticWrapper statisticwrapper = (StatisticWrapper) this.a.get(statistic);

        return statisticwrapper != null ? statisticwrapper.b() : null;
    }

    public <T extends IJsonStatistic> T a(Statistic statistic, T t0) {
        StatisticWrapper statisticwrapper = (StatisticWrapper) this.a.get(statistic);

        if (statisticwrapper == null) {
            statisticwrapper = new StatisticWrapper();
            this.a.put(statistic, statisticwrapper);
        }

        statisticwrapper.a(t0);
        return t0;
    }
}
