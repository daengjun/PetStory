package com.demo.petstory.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;

import com.demo.petstory.R;
import com.google.android.material.snackbar.Snackbar;

import java.security.MessageDigest;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class util {

    public static String nickName;

    /**
     * 다이어 로그 생성
     **/
    public static Dialog createDialog(int layoutId, Context context) {

        final Dialog customDia = new Dialog(context);
        customDia.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams params = customDia.getWindow().getAttributes();
        customDia.setContentView(layoutId);
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        customDia.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        return customDia;
    }


    /**
     * 이메일 정규식
     **/
    public static boolean isValidEmail(String email) {
        boolean err = false;
        String regex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);
        if (m.matches()) {
            err = true;
        }
        return err;
    }

    /**
     * 비밀 번호 정규식
     **/
    public static boolean isValidPassword(String password) {
        boolean err = false;
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*\\W)(?=\\S+$).{8,20}$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(password);
        if (m.matches()) {
            err = true;
        }
        return err;
    }

    /**
     * 닉네임 정규식
     **/
    public static boolean isValidNickName(final String nickName) {
        boolean err = false;
        String regex = "^[a-zA-Z0-9]*$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(nickName);
        if (m.matches()) {
            if (nickName.length() > 1 && nickName.length() < 9) {
                err = true;
            }
        }
        return err;
    }

    /**
     * 해쉬 키 값 조회
     **/
    private static void getAppKeyHash(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                Log.e("Hash key", something);
            }
        } catch (Exception e) {
            Log.e("name not found", e.toString());
        }
    }

    /**
     * Custom SnackBar
     **/
    public static void snackbar(Context context, String msg, int duration) {
        View rootView = ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
        if (rootView != null) {
            // 개행 처리를 위해 \n을 <br>로 교체
            String formattedMsg = msg.replace("\n", "<br>");
            String str = String.format(Locale.KOREA, "<font color=\"#ffffff\"><b>%s</b></font>", formattedMsg);

            // FROM_HTML_MODE_LEGACY 사용
            Snackbar snackbar = Snackbar.make(rootView, Html.fromHtml(str, HtmlCompat.FROM_HTML_MODE_LEGACY), duration);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundResource(R.drawable.drawable_rounded_box_normal);

            TextView tv = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
            tv.setMaxLines(10);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, rootView.getResources().getDimension(R.dimen.toast_text));

            snackbar.show();
        }
    }

    /**
     * Custom SnackBar object return
     **/
    public static Snackbar getSnackbar(Context context, String msg, int duration) {
        View rootView = ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);

        if (rootView != null) {
            // 개행 처리를 위해 \n을 <br>로 교체
            String formattedMsg = msg.replace("\n", "<br>");
            String str = String.format(Locale.KOREA, "<font color=\"#ffffff\"><b>%s</b></font>", formattedMsg);

            // FROM_HTML_MODE_LEGACY 사용
            Snackbar snackbar = Snackbar.make(rootView, Html.fromHtml(str, HtmlCompat.FROM_HTML_MODE_LEGACY), duration);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundResource(R.drawable.drawable_rounded_box_normal);

            TextView tv = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
            tv.setMaxLines(10);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, rootView.getResources().getDimension(R.dimen.toast_text));

            return snackbar;

        }

        return null;
    }

}
