package com.example.daidaijie.syllabusapplication.other.update;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.daidaijie.syllabusapplication.App;
import com.example.daidaijie.syllabusapplication.R;
import com.example.daidaijie.syllabusapplication.base.BaseActivity;
import com.example.daidaijie.syllabusapplication.bean.UpdateInfoBean;
import com.example.daidaijie.syllabusapplication.util.ClipboardUtil;
import com.example.daidaijie.syllabusapplication.util.LoggerUtil;
import com.example.daidaijie.syllabusapplication.util.SnackbarUtil;
import com.example.daidaijie.syllabusapplication.util.UpdateAsync;

import java.io.File;

import javax.inject.Inject;

import butterknife.BindView;
import info.hoang8f.widget.FButton;

public class UpdateActivity extends BaseActivity implements UpdateContract.view, SwipeRefreshLayout.OnRefreshListener, IDownloadView, UpdateInstaller {

    private final String TAG = this.getClass().getSimpleName();

    @BindView(R.id.titleTextView)
    TextView mTitleTextView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.nowVersionTextView)
    TextView mNowVersionTextView;
    @BindView(R.id.pusherTextView)
    TextView mPusherTextView;
    @BindView(R.id.updateInfoTextView)
    TextView mUpdateInfoTextView;
    @BindView(R.id.copyUpdateButton)
    FButton mCopyUpdateButton;
    @BindView(R.id.updateButton)
    FButton mUpdateButton;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Inject
    UpdatePresenter mUpdatePresenter;
    @BindView(R.id.newVersionTextView)
    TextView mNewVersionTextView;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupTitleBar(mToolbar);
        setupSwipeRefreshLayout(mSwipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mNowVersionTextView.setText(App.versionName);


        mPusherTextView.setText("Jarvis Yuen");

        mCopyUpdateButton.setVisibility(View.GONE);
        mUpdateButton.setVisibility(View.GONE);

        DaggerUpdateComponent.builder()
                .appComponent(mAppComponent)
                .updateModule(new UpdateModule(this))
                .build().inject(this);

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mUpdatePresenter.start();
            }
        });
    }


    @Override
    protected int getContentView() {
        return R.layout.activity_update;
    }

    @Override
    public void showFailMessage(String msg) {
        SnackbarUtil.ShortSnackbar(
                mTitleTextView, msg, SnackbarUtil.Alert
        ).show();
    }

    @Override
    public void showInfoMessage(String msg) {
        SnackbarUtil.ShortSnackbar(
                mTitleTextView, msg, SnackbarUtil.Info
        ).show();
    }

    @Override
    public void showLoading(boolean isShow) {
        mSwipeRefreshLayout.setRefreshing(isShow);
    }

    @Override
    public void showInfo(final UpdateInfoBean updateInfoBean) {
        int newVersionCode = Integer.parseInt(updateInfoBean.getVersionCode());
        // 更新按钮
        Log.d(TAG, "showInfo: " + newVersionCode + " " + App.versionCode);
        if (newVersionCode > App.versionCode) {
            mUpdateButton.setVisibility(View.VISIBLE);
            mUpdateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent= new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri content_url = Uri.parse("https://fir.im/syllabus");
                    intent.setData(content_url);
                    startActivity(intent);
//                    UpdateAsync updateAsync = new UpdateAsync(UpdateActivity.this, UpdateActivity.this, updateInfoBean.getDownload_address(), updateInfoBean.getApk_file_name());
//                    updateAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
        }

        mCopyUpdateButton.setVisibility(View.VISIBLE);
        mCopyUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardUtil.copyToClipboard(updateInfoBean.getDownload_address());
                showInfoMessage("已经复制到粘贴板");
            }
        });
        Log.d(TAG, "showInfo: " + updateInfoBean.getVersionDescription().trim());
        mUpdateInfoTextView.setText(updateInfoBean.getVersionDescription().trim());
        mNewVersionTextView.setText(updateInfoBean.getVersionName());
    }

    @Override
    public void showProgress(int done, int total) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("下载进度");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setProgress(0);
            progressDialog.setMax(total);
            // 暂时不考虑这个功能了
//            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    showInfoMessage("取消下载");
//                }
//            });
            progressDialog.setCancelable(false);
//            progressDialog.show();
        }

        progressDialog.setMax(total);
        progressDialog.setProgress(done);
        progressDialog.show();

    }

    @Override
    public void onRefresh() {
        mUpdatePresenter.start();
    }

    @Override
    public void installUpdate(File apk) {
        progressDialog.dismiss();
        Uri uri;
        if (apk != null && apk.exists()) {
            showInfoMessage("文件下载成功");
            // 隐式的 intent
            Intent install_apk = new Intent(Intent.ACTION_VIEW);
            install_apk.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //如果android版本大于7.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //即是在清单文件中配置的authorities
                uri = FileProvider.getUriForFile(this, "com.example.daidaijie.syllabusapplication.fileprovider", apk);
                // 给目标应用一个临时授权
                install_apk.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                uri = Uri.fromFile(apk);
            }
            // 安装apk文件
            install_apk.setDataAndType(uri, "application/vnd.android.package-archive");
            startActivity(install_apk);
        } else {
            showInfoMessage("文件下载失败");
        }
    }
}
