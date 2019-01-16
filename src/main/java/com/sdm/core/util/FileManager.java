/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.core.util;

import java.io.*;

/**
 * @author Htoonlin
 */
public class FileManager {

    public static final String[] SIZE_CODES = new String[]{"", "K", "M", "G", "T", "P", "E", "Z", "Y"};

    public static String byteSize(long size) {
        if (size <= 1024) {
            return "1 KB";
        }
        float resultSize = size;
        String result = resultSize + " T";
        for (String code : SIZE_CODES) {
            if (resultSize < 1024) {
                result = (Math.round(resultSize * 100.0) / 100.0) + " " + code;
                break;
            }
            resultSize /= 1024;
        }
        return result + "B";
    }

    public static String[] fileNameSplitter(String fileName) {
        String[] fileInfo = fileName.split("\\.(?=[^\\.]+$)");
        if (fileInfo.length < 2) {
            return new String[]{fileName};
        }
        return fileInfo;
    }


    public static void copyFileOrDirectory(File src, File dest)
        throws IOException {

        if (src.isDirectory()) {

            //if directory not exists, create it
            if (!dest.exists()) {
                dest.mkdir();
            }

            //list all the directory contents
            String files[] = src.list();

            for (String file : files) {
                //construct the src and dest file structure
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                //recursive copy
                copyFileOrDirectory(srcFile, destFile);
            }

        } else {
            //if file, then copy it
            //Use bytes stream to support all file types
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];

            int length;
            //copy the file content in bytes
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            in.close();
            out.close();
        }
    }
}
