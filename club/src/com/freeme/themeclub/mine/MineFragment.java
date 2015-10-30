package com.freeme.themeclub.mine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.droi.accout.sdk.DroiAccount;
import com.freeme.themeclub.R;
import com.freeme.themeclub.theme.onlinetheme.OnlineThemesDetailActivity;
import com.freeme.themeclub.theme.onlinetheme.util.OnlineThemesUtils;

public class MineFragment extends Fragment{
    private static final int DIALOG_PROGRESS = 1;

    private ImageView mLoginBtn;
    private ImageView mAvatar;
    private TextView mName;
    private DroiAccount mDroidAccount;
    private ProgressDialog mProgressDialog = null;
    private Button mDeleteBtn;
    private static final int REQUEST_CODE_USER_INFO = 2;
    private static final int BIND_PHONE = 3;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_mine, null);
        setupViews(contentView);
        return contentView;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDroidAccount = DroiAccount.getInstance(getActivity());

    }

    @Override
    public void onResume() {
        super.onResume();
        initViews();
    }

    private void recordIfLogin(boolean hasLogin){
        SharedPreferences sp = getActivity().getSharedPreferences(
                "account",Activity.MODE_PRIVATE); 
        Editor editor = sp.edit();
        editor.putBoolean("has_login", hasLogin);
        editor.commit();
    }

    private boolean getIfLogin(){
        SharedPreferences sp = getActivity().getSharedPreferences(
                "account",Activity.MODE_PRIVATE); 
        return sp.getBoolean("has_login", false);
    }

    @Deprecated
    public Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        if(id == DIALOG_PROGRESS){
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Processing...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(true);
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){

                @Override
                public void onCancel(DialogInterface dialog) {

                }

            });
            mProgressDialog = progressDialog;
            dialog = progressDialog;
        }

        return dialog;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_USER_INFO){
            if(resultCode == getActivity().RESULT_OK){
                boolean exitAccount = 
                        data.getBooleanExtra("ExitAccount", false);
                if(exitAccount){
                    recordIfLogin(false);
                }else{
                    recordIfLogin(true);
                }
                boolean deleteAccount = data.getBooleanExtra("DeleteAccount", false);
                if(deleteAccount){
                    Toast.makeText(getActivity(), "Logout All Apps", Toast.LENGTH_SHORT).show();
                    mLoginBtn.setVisibility(View.VISIBLE);
                }
            }

        }else if(requestCode == BIND_PHONE){
            if(resultCode == getActivity().RESULT_OK){

            }
        }
    }

    public String getVersion() {
        try {
            PackageManager manager = getActivity().getPackageManager();
            PackageInfo info = manager.getPackageInfo(getActivity().getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return getResources().getString(R.string.unknow_version);
        }
    }

    private void setupViews(View contentView){
        mLoginBtn = (ImageView)contentView.findViewById(R.id.login_btn);
        mLoginBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(mDroidAccount.checkAccount()){
                    whetherChangeAccount();
                }else{
                    mDroidAccount.login();
                    recordIfLogin(true);
                    initViews();
                }
            }
        });

        mAvatar = (ImageView)contentView.findViewById(R.id.photo);
        mAvatar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(mDroidAccount.checkAccount() && getIfLogin()){
                    Intent intent = mDroidAccount.getSettingsIntent(getString(R.string.app_name));
                    startActivityForResult(intent, REQUEST_CODE_USER_INFO);
                }
            }
        });
        mName = (TextView)contentView.findViewById(R.id.name);

        TextView version = (TextView)contentView.findViewById(R.id.version_code);
        version.setText(getVersion());

        final TextView cacheSize = (TextView)contentView.findViewById(R.id.cache_size);
        cacheSize.setText(FileUtil.getAutoFileOrFilesSize(
                OnlineThemesUtils.getSDPath()+OnlineThemesUtils.mWallpaper_download_path));

        RelativeLayout cacheArea = (RelativeLayout)contentView.findViewById(R.id.account_item_one);
        cacheArea.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                clearCache();
                cacheSize.setText("0B");
            }
        });
    }

    private void clearCache(){
        File wallpapersFile = new File(OnlineThemesUtils.getSDPath()+
                OnlineThemesUtils.mWallpaper_download_path);
        File themesFile = new File(OnlineThemesUtils.getSDPath()+
                OnlineThemesUtils.mDownload_path);

        FileUtil.delete(wallpapersFile);
        FileUtil.delete(themesFile);
    }

    private void whetherChangeAccount(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.hint))
        .setMessage(getResources().getString(
                R.string.login_message_left)+
                mDroidAccount.getUserName()+
                getResources().getString(
                        R.string.login_message_right))
                        .setNegativeButton(getResources().getString(R.string.change_account),
                                new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                mDroidAccount.changeAccount();
                            }
                        })
                        .setPositiveButton(getResources().getString(R.string.login_now), 
                                new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                recordIfLogin(true);
                                initViews();
                            }
                        }).create();
        builder.show();
    }

    private void showProgress(){
        getActivity().showDialog(DIALOG_PROGRESS);
    }

    private void hideProgress(){
        if (mProgressDialog != null){
            mProgressDialog.dismiss();
        }
    }

    private void initViews(){
        if(mDroidAccount.checkAccount()){
            if(getIfLogin()){
                updateAvatar();
                mName.setText(mDroidAccount.getNickName());
                mName.setVisibility(View.VISIBLE);
                mLoginBtn.setVisibility(View.INVISIBLE);
                return ;
            }
        }
        mAvatar.setImageResource(R.drawable.photo);
        mName.setVisibility(View.GONE);
        mLoginBtn.setVisibility(View.VISIBLE);

    }

    private void updateAvatar(){
        String avatarPath = mDroidAccount.getAvatarUrl();
        if(!TextUtils.isEmpty(avatarPath)){
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(avatarPath);
                Bitmap bitmap = BitmapFactory.decodeStream(fis);
                mAvatar.setImageBitmap(bitmap);
                fis.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }catch (IOException e){

            }
        }else{
            mAvatar.setImageResource(R.drawable.photo);
        }
    }
}
