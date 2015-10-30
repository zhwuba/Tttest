package com.freeme.themeclub;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.Uri;

public class ShareUtil {
    public static void shareText(Context context,String content){
        shareFreemeOS(context, content, null, true);
        /*
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(
                R.string.share));
        shareIntent.putExtra(Intent.EXTRA_TEXT, content);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(Intent.createChooser(shareIntent, context.getResources().getString(
                R.string.share)));
        */
    }

    public static void shareImage(Context context,String content,String imgPath){
        shareFreemeOS(context, content, imgPath, false);
        /*
        Intent intent=new Intent(Intent.ACTION_SEND);   
        intent.setType("image/*");   
        File f = new File(imgPath);    
        Uri u = Uri.fromFile(f);    
        intent.putExtra(Intent.EXTRA_STREAM, u); 
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(
                R.string.share));   
        intent.putExtra(Intent.EXTRA_TEXT, content);    
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   
        context.startActivity(Intent.createChooser(intent, context.getResources().getString(
                R.string.share)));
        */
    }
    
    private static void shareImageInner(Context context,String content,String imgPath){

        Intent intent=new Intent(Intent.ACTION_SEND);   
        intent.setType("image/*");   
        File f = new File(imgPath);    
        Uri u = Uri.fromFile(f);    
        intent.putExtra(Intent.EXTRA_STREAM, u); 
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(
                R.string.share));   
        intent.putExtra(Intent.EXTRA_TEXT, content);    
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   
        context.startActivity(Intent.createChooser(intent, context.getResources().getString(
                R.string.share)));
    }

    private  static void shareTextInner(Context context,String content){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(
                R.string.share));
        shareIntent.putExtra(Intent.EXTRA_TEXT, content);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(Intent.createChooser(shareIntent, context.getResources().getString(
                R.string.share)));
    }

    public static void shareFreemeOS(Context context, String content, String imgPath, boolean shareText) {
        Resources res = context.getResources();
        String title = res.getString(R.string.share_freeme_extra_title);
        String summary = res.getString(R.string.share_freeme_extra_summary);
        String imageUrl = res.getString(R.string.share_freeme_extra_image_url);
        String sharedUrl = res.getString(R.string.share_freeme_extra_url);

        if(false == checkInstalled(context)) {
            if(shareText) {
                shareTextInner(context, content);
            } else {
                shareImageInner(context, content, imgPath);
            }
            /*
            String plain = title + "\n"
                            + summary + "\n"
                            + sharedUrl + " \n(From:FreemeOS)";
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, title);
            intent.putExtra(Intent.EXTRA_TEXT, plain);
            context.startActivity(Intent.createChooser(intent, title));
            */
            return;
        } else {
            Intent intent = new Intent("com.freeme.sharecenter.SHAREAPP");
            intent.setPackage("com.freeme.sharedcenter");
            intent.putExtra("title", title);
            intent.putExtra("summary", summary);
            intent.putExtra("sharedUrl", sharedUrl);
            intent.putExtra("imageUrl", imageUrl);
            intent.putExtra("package", context.getPackageName());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            try {
                context.startActivity(intent);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public static boolean checkInstalled(Context context) {
        PackageInfo info = null;
        
        try {
            info = context.getPackageManager().getPackageInfo("com.freeme.sharedcenter", 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        
        if(null == info) {
            return false;
        } else {
            return true;
        }
    }
}
