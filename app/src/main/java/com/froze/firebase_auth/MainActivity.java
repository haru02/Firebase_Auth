package com.froze.firebase_auth;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Button btnSignUp, btnSignIn, btnSignOut;
    EditText etEmail, etPw;
    TextView userStatus;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String TAG = "Firebase_Auth";
    FirebaseDatabase database;
    DatabaseReference rootRef, userRef;

    ListView listView;
    ArrayList<Map<String,User>> datas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSignIn = (Button)findViewById(R.id.buttonSignIn);
        btnSignUp = (Button)findViewById(R.id.buttonSignUp);
        btnSignOut = (Button)findViewById(R.id.buttonSignOut);
        etEmail = (EditText)findViewById(R.id.editTextEmail);
        etPw = (EditText)findViewById(R.id.editTextPw);
        userStatus = (TextView)findViewById(R.id.textViewUserStatus);
        // 1. 인증객체 가져오기
        mAuth = FirebaseAuth.getInstance();
        // 2, 리스너 설정
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    userStatus.setText("Sign In");

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    userStatus.setText("Sign Out");
                }
            }
        };

        //4. 신규계정 생성
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String pw = etPw.getText().toString().trim();
                if(!"".equals(email) && !"".equals(pw)){
                    addUser(email,pw);
                }else{
                    Toast.makeText(MainActivity.this, "Email과 Password를 입력하셔야 합니다.", Toast.LENGTH_LONG).show();
                }
            }
        });

        // 5. 들어가기
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String pw = etPw.getText().toString().trim();
                if(!"".equals(email) && !"".equals(pw)){
                    signIn(email,pw);
                }else{
                    Toast.makeText(MainActivity.this, "Email과 Password를 입력하셔야 합니다.", Toast.LENGTH_LONG).show();
                }
            }
        });

        listView = (ListView)findViewById(R.id.listView);
        database = FirebaseDatabase.getInstance();
        rootRef = database.getReference();
        userRef = database.getReference("users");
        CustomAdapter adapter = new CustomAdapter(this);
        listView.setAdapter(adapter);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot users) {
                datas = new ArrayList<Map<String, User>>();
                for (DataSnapshot userData : users.getChildren()) {
                    try {
                        Map<String, User> data = new HashMap<String, User>();
                        String userId = userData.getKey();
                        User user = userData.getValue(User.class);
                        data.put(userId, user);
                        datas.add(data);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                listView.deferNotifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Toast.makeText(MainActivity.this, "Sign Out에 성공했습니다.", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void writeNewUser(String userId, String name, String email) {
        User user = new User(name, email);

        rootRef.setValue("hello");
        rootRef.child("users").child(userId).setValue(user);

        /*
            root - users - michael - name : 누구
                                   - email : 어디
                         - hyojung - name : 누구2
                                   - email
         */
    }

    public void signIn(String email, String pw){
        mAuth.signInWithEmailAndPassword(email, pw)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(MainActivity.this, "Sign In에 실패하였습니다.", Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(MainActivity.this, "Sign In에 성공하였습니다.", Toast.LENGTH_LONG).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Sign In에 실패하였습니다.", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void addUser(String email, String pw){
        mAuth.createUserWithEmailAndPassword(email, pw)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(MainActivity.this, "사용자 등록에 실패하였습니다.", Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(MainActivity.this, "사용자 등록에 성공하였습니다.", Toast.LENGTH_LONG).show();
                        }
                    }
                }).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Toast.makeText(MainActivity.this, "사용자 등록에 성공하였습니다.", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "사용자 등록에 실패하였습니다.", Toast.LENGTH_LONG).show();
            }
        });
    }


    // 3. 리스너 해제 및 재등록
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    class CustomAdapter extends BaseAdapter {

        LayoutInflater inflater;

        public CustomAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public Object getItem(int position) {
            return datas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.layout_item, null);
                TextView tvName = (TextView) convertView.findViewById(R.id.textView2);
                TextView tvEmail = (TextView) convertView.findViewById(R.id.textView3);

                Map<String, User> data = datas.get(position);
                String uid = data.keySet().iterator().next();
                User user = data.get(uid);
                tvName.setText(user.username);
                tvEmail.setText(user.email);

            }
            return convertView;
        }
    }
}
