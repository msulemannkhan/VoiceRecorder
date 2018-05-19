package com.paykio.khubaibraza.voicrecorder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Khubaib raza on 18/03/2018.
 */
public class EditNameDialogFragment extends android.support.v4.app.DialogFragment {

    private EditText mEditText;

    public EditNameDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static EditNameDialogFragment newInstance(String title) {
        EditNameDialogFragment frag = new EditNameDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        EditText editText;
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View recordView = inflater.inflate(R.layout.fragment_custom_dialog, container, false);
        mEditText =recordView.findViewById(R.id.txt_your_name);
        return recordView;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final EditText input = new EditText(getActivity());
        SharedPreferences sharedPreferences= android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(getActivity());
        input.setText(sharedPreferences.getString("valueInput","-1"));

        String title = getArguments().getString("title");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        alertDialogBuilder.setView(input).setTitle(title);
//        alertDialogBuilder.setView(mEditText);
        alertDialogBuilder.setMessage("Are you sure?");
        alertDialogBuilder.setPositiveButton("OK",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // on success
                String value = input.getText().toString();
                if (input.getText().toString().trim().length() != 0) {
                   // Toast.makeText(getActivity(), value, Toast.LENGTH_SHORT).show();
//                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
//                    SharedPreferences.Editor editor = sharedPref.edit();
//                    editor.putString("valueInput", value);
//                    editor.commit();
                    FileViewerAdapter fileViewerAdapter=new FileViewerAdapter(getContext());
                    fileViewerAdapter.renameFileOutside(value);
                } else {
                    Toast.makeText(getActivity(),"yh chal rha ha", Toast.LENGTH_SHORT).show();
                }
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }

        });
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        return alertDialogBuilder.create();
    }

}