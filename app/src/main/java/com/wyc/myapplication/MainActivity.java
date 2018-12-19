package com.wyc.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.wyc.custom_annotation.BindView;
import com.wyc.custom_annotation.OnClick;
import com.wyc.custom_annotation_lib.CustomAnnotation;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.tv_hello)
    TextView mHelloTextView;
    @BindView(R.id.button)
    Button mButton;
    @BindView(R.id.button2)
    Button mButton2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CustomAnnotation.bind(this);
    }

    @OnClick(R.id.button)
    public void doAction(View v) {
        mHelloTextView.setText("<<success>>");
    }

    @OnClick(R.id.button2)
    public void doAction2(View v) {
        mHelloTextView.setText("++++++++++++");
    }
}
