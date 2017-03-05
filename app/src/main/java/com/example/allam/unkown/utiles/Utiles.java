package com.example.allam.unkown.utiles;

import android.content.Context;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;

import com.example.allam.unkown.R;

/**
 * Created by Allam on 2/16/2017.
 */

public class Utiles {

    public static boolean isEditTextNull(Context context, EditText editText){
        if(TextUtils.isEmpty(editText.getText())){
            setEditTextError(editText, context.getString(R.string.required_field));
            return true;
        }
        return false;
    }

    public static void setEditTextError(EditText editText, String errorMessage){
            editText.setError(errorMessage);
            editText.requestFocus();
    }

    public static boolean isEmailValid(Context context, EditText email) {
        if (!Utiles.isEditTextNull(context, email)) {
            if (Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
                return true;
            }else {
                Utiles.setEditTextError(email, context.getString(R.string.invalid_email_address));
            }
        }

        return false;
    }

    public static  boolean isUserNameValid(Context context,EditText userName) {
        boolean isNull = Utiles.isEditTextNull(context, userName);
        boolean isLengthValid = userName.getText().toString().length() >= 3;
        if(!isLengthValid){
            Utiles.setEditTextError(userName, context.getString(R.string.username_length_error));
        }

        return isLengthValid && !isNull;
    }

    public static  boolean isPasswordValid(Context context,EditText password) {
        boolean isNull = Utiles.isEditTextNull(context, password);
        boolean isLengthValid = password.getText().toString().length() >= 6;
        if(!isLengthValid){
            Utiles.setEditTextError(password, context.getString(R.string.password_length_error));
        }

        return isLengthValid && !isNull;
    }


}
