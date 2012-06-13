package org.gradle.api.tasks.scala.incremental;

import sbt.compiler.IC;
import sbt.inc.Analysis;
import xsbti.Maybe;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

public class SbtAnalysis {

    public static final Analysis EMPTY_ANALYSIS = sbt.inc.Analysis$.MODULE$.Empty();

    public static final Maybe<Analysis> JUST_EMPTY_ANALYSIS = Maybe.just(EMPTY_ANALYSIS);

    private static HashMap<File, SoftReference<Analysis>> cached = new HashMap<File, SoftReference<Analysis>>();

    public static Maybe<Analysis> getAnalysis(File file, File classesDirectory, Map<File, File> cacheMap) {
        if (file.getName().endsWith("jar")) {
            return JUST_EMPTY_ANALYSIS;
        } else if (file.equals(classesDirectory)) {
            return JUST_EMPTY_ANALYSIS;
        } else if (file.exists() && file.isDirectory()) {
            File cacheFile = cacheMap.get(file);
            if (cacheFile == null) cacheFile = fallbackCacheLocation(file);
            return Maybe.just(get(cacheFile));
        } else {
            return JUST_EMPTY_ANALYSIS;
        }
    }

    public static synchronized Analysis get(File cacheFile) {
        Analysis analysis = null;
        SoftReference<Analysis> ref = cached.get(cacheFile);
        if (ref != null) {
            analysis = ref.get();
        }
        if (analysis == null) {
            if (cacheFile.exists()) {
                analysis = IC.readAnalysis(cacheFile);
            } else {
                analysis = EMPTY_ANALYSIS;
            }
            cached.put(cacheFile, new SoftReference(analysis));
        }
        return analysis;
    }

    public static synchronized void put(File cacheFile, Analysis analysis) {
        cached.put(cacheFile, new SoftReference(analysis));
    }

    public static File fallbackCacheLocation(File file) {
        return new File(new File(file.getParent(), "analysis"), file.getName());
    }
}