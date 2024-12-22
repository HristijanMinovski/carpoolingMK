package com.example.workshop;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class log extends Fragment {
    private EditText username, password;
    private Spinner role_spinner;
    private Button loginButton;
    private DatabaseHelper db;


    public log() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_log, container, false);
        username = view.findViewById(R.id.username);
        password = view.findViewById(R.id.password);
        role_spinner = view.findViewById(R.id.role_spinner);
        loginButton = view.findViewById(R.id.login_button);
        db=new DatabaseHelper(requireContext());
        loginButton.setOnClickListener(v -> {
            String user = username.getText().toString();
            String pass = password.getText().toString();
            String spinner = role_spinner.getSelectedItem().toString();
            if (!user.isEmpty() && !pass.isEmpty() && !spinner.isEmpty()) {
                int check=db.checkUser(user,pass);
                if(check==1){ //ako e unikaten odnosno ne e pronajden vo bazata
                    if(spinner.contains("Клиент")){
                        Intent intent = new Intent(requireActivity(),UserClient.class);
                        intent.putExtra("username",user);
                        startActivity(intent);
                        //Toast.makeText(LoginActivity.this, "Se logiravte kako klient", Toast.LENGTH_SHORT).show();
                    }
                    else if(spinner.contains("Возач")){
                        Intent intent= new Intent(requireActivity(),UserDriver.class);
                        intent.putExtra("username",user);
                        startActivity(intent);
                        //Toast.makeText(LoginActivity.this, "Se logiravte kako vozac", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(getContext(),"Корисникот не постои во дата базата", Toast.LENGTH_LONG).show();
                }
                db.close();
            } else {
                Toast.makeText(getContext(), "Пополнете ги сите полиња", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // If you need to handle logic based on the activity's intent, do it here
        Intent intent = requireActivity().getIntent();
        // You can extract extra data from the intent if necessary
    }
}


