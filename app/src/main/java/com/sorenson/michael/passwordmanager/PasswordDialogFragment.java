package com.sorenson.michael.passwordmanager;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class PasswordDialogFragment extends DialogFragment {
    public static interface OnCompleteListener {
        public abstract void onComplete(String pw);
    }

    private  OnCompleteListener mListener;

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mListener = (OnCompleteListener)activity;
        } catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnCompleteListener");
        }
    }

    public void finishDialog(String pw) {
        this.mListener.onComplete(pw);
        this.dismiss();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstancesState) {
        super.onAttach(getActivity());
        final View view = inflater.inflate(R.layout.fragment_password_dialog, container);
        getDialog().setTitle(R.string.password_dialog);
        Button save_btn = (Button) view.findViewById(R.id.dialog_save);
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText pw = (EditText) view.findViewById(R.id.dialog_pw);
                finishDialog(pw.getText().toString());
            }
        });
        return view;
    }
}
