package com.giua.app;

import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.giua.webscraper.GiuaScraper;
import com.google.android.material.textfield.TextInputEditText;
import com.giua.app.GlobalVars;

import org.w3c.dom.Text;

public class FirstFragment extends Fragment {

    AppCompatEditText user;
    AppCompatEditText password;
    ProgressBar progressBar;

    GiuaScraper gS;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        View fragmentFirstLayout = inflater.inflate(R.layout.fragment_first, container, false);

        user = fragmentFirstLayout.findViewById(R.id.textUser);
        password = fragmentFirstLayout.findViewById(R.id.textPassword);
        progressBar = fragmentFirstLayout.findViewById(R.id.progressBar);




        // Inflate the layout for this fragment
        return fragmentFirstLayout;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);

                GiuaScraper gS = new GlobalVars().setgS(user.getText().toString(), password.getText().toString());


                gS.login();

                if(gS.checkLogin() == true){
                    System.out.println("login ok");
                    NavHostFragment.findNavController(FirstFragment.this)
                            .navigate(R.id.action_FirstFragment_to_SecondFragment);
                }
            }
        });
    }
}