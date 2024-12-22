package com.example.workshop;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class reg extends Fragment {
    private EditText Username, Password , Email;
    private DatabaseHelper db;
    private Button btnRegister;

    public reg() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_reg, container, false);
        Username = view.findViewById(R.id.etUsername);
        Password = view.findViewById(R.id.etPassword);
        Email = view.findViewById(R.id.email);
        btnRegister = view.findViewById(R.id.btnRegister);
        db = new DatabaseHelper(requireContext());
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = Username.getText().toString().trim();
                String password = Password.getText().toString().trim();
                String email = Email.getText().toString().trim();

                if (!username.isEmpty() && !password.isEmpty() && !email.isEmpty()) {
                    //SQLiteDatabase db = DatabaseHelper.getWritableDatabase();
                    boolean check=db.checkUserUnique(username,email);
                    if(check){ //ako e unikaten odnosno ne e pronajden vo bazata
                        db.insertUser(username,password,email);
                    }
                    else{
                        Toast.makeText(getContext(),"Веќе постои таков корисник! Изберете друго корисничко име", Toast.LENGTH_LONG).show();
                    }
                    db.close();
                    Toast.makeText(getContext(), "Успешна регистрација вратете се на првито екран!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "Пополнете ги сите полиња", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Intent intent = requireActivity().getIntent();
    }
}