package org.altmc;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.logging.Logger;

public class Main {

    public static final Logger LOGGER = Logger.getLogger("AltMC-Authlib");
    private static boolean booted = false;

    public static void premain(String args, Instrumentation instrumentation) {
        if (booted) return;
        if (instrumentation == null) {
            LOGGER.severe("No instrumentation provided.");
            return;
        }
        CodeSource selfSource = Transformer.class.getProtectionDomain().getCodeSource();
        URL[] targetURL = new URL[1];
        targetURL[0] = selfSource.getLocation();
        ClassLoader targetLoader = new URLClassLoader(targetURL, null);
        try {
            Class<?> transformerCore = targetLoader.loadClass("org.altmc.Transformer");
            Method startMethod = transformerCore.getDeclaredMethod("start", Instrumentation.class);
            startMethod.invoke(null, instrumentation);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        booted = true;
    }

    public static void agentmain(String args, Instrumentation instrumentation) {
        premain(args, instrumentation);
    }
}
