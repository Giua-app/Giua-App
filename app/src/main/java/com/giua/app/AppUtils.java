/*
 * Giua App
 * Android app to view data from the giua@school workbook
 * Copyright (C) 2021 - 2022 Hiem, Franck1421 and contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see https://www.gnu.org/licenses/.
 */

package com.giua.app;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;

public class AppUtils {

    public static int convertDpToPx(float dp, Context context) {
        //https://stackoverflow.com/questions/4605527/converting-pixels-to-dp
         
 ​        ​DisplayMetrics realMetrics​ = ​new​ ​DisplayMetrics​(); 
 ​        ((​Activity​) ​context​).​getWindowManager​().​getDefaultDisplay​().​getRealMetrics​(​realMetrics​);
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, realMetrics);
    }

    public static void hideKeyboard(Context context, View view) {
        InputMethodManager imm = context.getSystemService(InputMethodManager.class);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void clearWebViewCookies() {
        CookieManager.getInstance().removeAllCookies(null);
    }
}
