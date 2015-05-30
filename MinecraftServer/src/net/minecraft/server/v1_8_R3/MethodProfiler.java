package net.minecraft.server.v1_8_R3;

import java.util.List;

public class MethodProfiler {

    public boolean a;

    public MethodProfiler() {}

    public void a() {}

    public void a(String s) {}

    public void b() {}

    public List<MethodProfiler.ProfilerInfo> b(String s) {
        return null;
    }

    public void c(String s) {}

    public String c() {
        return "";
    }

    public static final class ProfilerInfo implements Comparable<MethodProfiler.ProfilerInfo> {

        public double a;
        public double b;
        public String c;

        public ProfilerInfo(String s, double d0, double d1) {
            this.c = s;
            this.a = d0;
            this.b = d1;
        }

        public int a(MethodProfiler.ProfilerInfo methodprofiler_profilerinfo) {
            return methodprofiler_profilerinfo.a < this.a ? -1 : (methodprofiler_profilerinfo.a > this.a ? 1 : methodprofiler_profilerinfo.c.compareTo(this.c));
        }

        public int compareTo(MethodProfiler.ProfilerInfo methodprofiler_profilerinfo) {
            return this.a(methodprofiler_profilerinfo);
        }

        public int compareTo(Object object) {
            return this.compareTo((MethodProfiler.ProfilerInfo) object);
        }
    }
}
