package com.ogray.glbot.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Slf4j
public class Utils {
    static Locale locale = new Locale("en");
    static ResourceBundle bundle = ResourceBundle.getBundle("bot", locale);

    public static String getString(String key) {
        return bundle.getString(key);
    }


    public static void mkdir(String name) {
        new File(name).mkdirs();
    }
    public static boolean isFileFolderExists(String name) {
        return new File(name).exists();
    }
    public static String copyFile(String src, String dstFolder) throws IOException {
        File sourceFile = new File(src);
        String sourceFileName = sourceFile.getName();

        Path copied = Paths.get(dstFolder + "/" + sourceFileName);
        Path originalPath = sourceFile.toPath();

        log.info("copy ["+originalPath+"] to ["+copied+"]");
        Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);
        return copied.toString();
    }

    // replace extension
    public static String makeWcsFile(String name) {
        int ch = name.lastIndexOf('.');
        if(ch<0) return name + ".wcs";

        return name.substring(0,ch) + ".wcs";
    }
}
