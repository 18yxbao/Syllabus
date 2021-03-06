package com.example.daidaijie.syllabusapplication.schoolDymatic.personal;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.daidaijie.syllabusapplication.R;
import com.example.daidaijie.syllabusapplication.base.BaseActivity;
import com.example.daidaijie.syllabusapplication.bean.UserBaseBean;
import com.example.daidaijie.syllabusapplication.main.MainActivity;
import com.example.daidaijie.syllabusapplication.user.UserComponent;
import com.example.daidaijie.syllabusapplication.util.SnackbarUtil;
import com.example.daidaijie.syllabusapplication.util.ThemeUtil;
import com.example.daidaijie.syllabusapplication.widget.LoadingDialogBuiler;
import com.facebook.drawee.view.SimpleDraweeView;

import javax.inject.Inject;

import butterknife.BindView;
import info.hoang8f.widget.FButton;

public class PersonalActivity extends BaseActivity implements PersonalContract.view {
    private final String TAG = this.getClass().getSimpleName();

    @BindView(R.id.titleTextView)
    TextView mTitleTextView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.headImageDraweeView)
    SimpleDraweeView mHeadImageDraweeView;
    @BindView(R.id.accountEditText)
    EditText mAccountEditText;
    @BindView(R.id.niceNameEditText)
    EditText mNiceNameEditText;
    @BindView(R.id.sayEditText)
    EditText mSayEditText;
    @BindView(R.id.finishButton)
    FButton mFinishButton;

    AlertDialog mLoadingDialog;

    @Inject
    PersonalPresenter mPersonalPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupTitleBar(mToolbar);

        DaggerPersonalComponent.builder()
                .userComponent(UserComponent.buildInstance(mAppComponent))
                .personalModule(new PersonalModule(this))
                .build().inject(this);

        mPersonalPresenter.start();

        mHeadImageDraweeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPersonalPresenter.selectHeadImg();
            }
        });

        mFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPersonalPresenter.pushData(mNiceNameEditText.getText().toString().trim(),
                        mSayEditText.getText().toString().trim());
            }
        });

        mLoadingDialog = LoadingDialogBuiler.getLoadingDialog(this, ThemeUtil.getInstance().colorPrimary);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_personal;
    }

    @Override
    public void showUserBase(UserBaseBean userBaseBean) {
        Log.d(TAG, "showUserBase: " + userBaseBean.getPhoto());
        mHeadImageDraweeView.setImageURI(userBaseBean.getPhoto());
        mNiceNameEditText.setText(userBaseBean.getNickname());
        mAccountEditText.setText(userBaseBean.getAccount());
        mSayEditText.setText(userBaseBean.getProfile());

    }

    @Override
    public void showHead(String uri) {
        mHeadImageDraweeView.setImageURI(uri);
    }

    @Override
    public void showFailMessage(String msg) {
        SnackbarUtil.ShortSnackbar(mHeadImageDraweeView, msg, SnackbarUtil.Alert).show();
    }

    @Override
    public void showSuccessMessage(String msg) {
        //SnackbarUtil.ShortSnackbar(mHeadImageDraweeView, msg, SnackbarUtil.Confirm).show();
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toMain() {
        finish();
    }

    @Override
    public void showLoading(boolean isShow) {
        if (isShow) {
            mLoadingDialog.show();
        } else {
            mLoadingDialog.dismiss();
        }
    }

}
