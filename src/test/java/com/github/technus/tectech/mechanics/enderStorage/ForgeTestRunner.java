package com.github.technus.tectech.mechanics.enderStorage;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraft.launchwrapper.LaunchClassLoader;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/** Forge 1.7.10 requires its own class loader even when only initializing vanilla registries. */
public class ForgeTestRunner extends BlockJUnit4ClassRunner {
    public ForgeTestRunner(Class<?> testClass) throws InitializationError {
        super(loadWithForge(testClass));
    }

    private static Class<?> loadWithForge(Class<?> testClass) throws InitializationError {
        try {
            Set<URL> urls = new LinkedHashSet<>();
            for (String path : System.getProperty("java.class.path").split(File.pathSeparator)) {
                urls.add(new File(path).toURI().toURL());
            }
            for (ClassLoader parent = testClass.getClassLoader(); parent != null; parent = parent.getParent()) {
                if (parent instanceof URLClassLoader) urls.addAll(Arrays.asList(((URLClassLoader) parent).getURLs()));
            }
            LaunchClassLoader loader = new LaunchClassLoader(urls.toArray(new URL[0]));
            loader.addClassLoaderExclusion("org.junit.");
            loader.addClassLoaderExclusion("org.hamcrest.");
            loader.addClassLoaderExclusion(ForgeTestRunner.class.getName());
            loader.loadClass("cpw.mods.fml.common.Loader").getMethod("injectData", Object[].class).invoke(null,
                    (Object) new Object[] { "7", "10", "99", "99", "1.7.10", "9.05", new File("."), new ArrayList<String>() });
            return loader.loadClass(testClass.getName());
        } catch (Exception error) {
            throw new InitializationError(error);
        }
    }
}
