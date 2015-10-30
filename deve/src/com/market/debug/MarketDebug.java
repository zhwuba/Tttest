package com.market.debug;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.market.featureOption.FeatureOption;

import android.content.Context;
import android.os.Environment;

/**
 * for market debug record
 * @author Athlon
 *
 */
public class MarketDebug {

    
    /**
     * for pull the market data in system to sdcard
     * @param context
     */
    public static void pullDebugMarketData(final Context context) {
        new Thread() {
            @Override
            public void run() {
                String outputDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/TydMarketDebug/";
                
                String debugDirPath = context.getFilesDir().getAbsolutePath();
                debugDirPath = debugDirPath.substring(0, debugDirPath.length() - "files".length());
                
                File dir = new File(debugDirPath);
                File[] debugFileList = dir.listFiles();
                File debugFile = null;
                for(int i=0; i < debugFileList.length; i++) {
                    debugFile = debugFileList[i];
                    outputDebugMarketDataFile(debugFile, debugDirPath, outputDirPath);
                }
            }
        }.start();
    }
    
    
    private static void outputDebugMarketDataFile(File inputFile, String debugDirPath, String outputDirPath) {
        if(inputFile.isDirectory()) {
            File[] files = inputFile.listFiles();
            for(int i=0; i < files.length; i++) {
                outputDebugMarketDataFile(files[i], debugDirPath, outputDirPath);
            }
        }else {
            FileInputStream fips = null;
            FileOutputStream fops = null;
            try {
                fips = new FileInputStream(inputFile);
                
                String outputFilePath = outputDirPath + inputFile.getAbsolutePath().substring(debugDirPath.length());
                File outputFile = new File(outputFilePath);
                outputFile.getParentFile().mkdirs();
                fops = new FileOutputStream(outputFilePath, false);
                byte[] buff = new byte[2048];
                int rc = 0;
                while((rc = fips.read(buff)) != -1) {
                    fops.write(buff, 0, rc);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                	if (fops != null)
                		fops.close();
                	if (fips != null)
                		fips.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
        }
    }
    
    
    
    /**
     * for debug report time log
     * @param action
     * @param result
     * @param startTime
     * @param endTime
     * @param uploadCount
     */
    public static void recordReportTimeLog(String action, boolean result, long startTime, long endTime, int uploadCount) {
        if (!FeatureOption.DEBUG_REPORT) {
            return;
        }
        
        String recordString = action + " result:" + Boolean.toString(result) + " | upload count = " + uploadCount + " | report time = " + Long.toString(endTime - startTime) + " millis";
        writeDebugReportFile("reportTimeLog.txt", recordString);
    }
    
    
    /**
     * to record the log in sdcard
     * @param fileName save file in sdcard, parent dir is like:"/sdcard/TydMarketDebug/", you can set the fileName like:"DownloadModule/fileName.txt"
     * @param recordString the record string witch will be write to the debug file you set, record string will write to the end of file and auto line feed
     */
    public static void writeDebugReportFile(String fileName, String recordString) {
        if (!FeatureOption.DEBUG_REPORT) {
            return;
        }
        
        String outputFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/TydMarketDebug/" + fileName;
        File outputFile = new File(outputFilePath);
        outputFile.getParentFile().mkdirs();
        FileOutputStream fops = null;
        recordString += "\n";
        
        try {
            fops = new FileOutputStream(outputFilePath, true);
            fops.write(recordString.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fops.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
