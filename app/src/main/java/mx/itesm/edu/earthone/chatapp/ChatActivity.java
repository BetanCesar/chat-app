package mx.itesm.edu.earthone.chatapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;

import mx.itesm.edu.earthone.chatapp.Adapters.ChatAdapter;
import mx.itesm.edu.earthone.chatapp.pojo.ChatPojo;

public class ChatActivity extends Activity {

    private Button button;
    private ImageButton btnLogOut;
    private EditText editMessage;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private ChildEventListener childEventListener;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener stateListener;
    // Storage
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    private final int LOGIN = 123;

    private ListView listView;
    private ChatAdapter adapter;
    private String userName;
    private ImageButton imageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("messages"); // Crea un arbol de JSONs

        firebaseAuth = firebaseAuth.getInstance(); //login

        // Storage
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("pics"); // Crea una carpeta llamada pics

        listView = (ListView) findViewById(R.id.messageList);
        adapter = new ChatAdapter(this, R.layout.chat_layout, new ArrayList<ChatPojo>());
        listView.setAdapter(adapter);

        editMessage = (EditText) findViewById(R.id.editMessage);
        button = (Button) findViewById(R.id.btnSendMessage);
        btnLogOut = (ImageButton) findViewById(R.id.btnLogOut);
        btnLogOut.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                AuthUI.getInstance().signOut(getApplicationContext());
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*SharedPreferences sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
                String string = sharedPreferences.getString("name", "not saved");*/
                String message = editMessage.getText().toString();

                ChatPojo chatPojo = new ChatPojo(userName, null, message);
                databaseReference.push().setValue(chatPojo);
                editMessage.setText("");
            }
        });

        stateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if(firebaseUser != null){
                    Toast.makeText(getApplicationContext(),
                            "Welcome to the jungle",
                            Toast.LENGTH_LONG).show();
                    userName = firebaseUser.getDisplayName();
                    loadChats();
                }else{
                    clean();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(
                                            Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build(), new  AuthUI.IdpConfig.GoogleBuilder().build()
                                            )
                                    ).build(), LOGIN );

                }

            }
        };

        // ImageButton Implementation btnAddImage of activity_chat

        imageButton = (ImageButton) findViewById(R.id.btnAddImage);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Select a PIC!"), 3007);
            }
        });
    }

    private void clean(){ // limpiar si no esta loggeado
        userName = "";
        adapter.clear();
        if(childEventListener != null){
            databaseReference.removeEventListener(childEventListener);
            childEventListener = null;
        }
    }

    private void loadChats(){
        if(childEventListener == null) {
            childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    ChatPojo chatPojo = dataSnapshot.getValue(ChatPojo.class);
                    adapter.add(chatPojo);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            databaseReference.addChildEventListener(childEventListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        firebaseAuth.removeAuthStateListener(stateListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(firebaseAuth != null){ // Verificar que no sea nula
            firebaseAuth.addAuthStateListener(stateListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // El request code esta definido previamente, y el RESULT_OK es que escogió una imágen
        // En el data se sabe que imágen escogió
        if(requestCode == 3007 && resultCode == RESULT_OK){
            Uri pic = data.getData();
            StorageReference str = storageReference.child(pic.getLastPathSegment());
            str.putFile(pic).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri uploaded = taskSnapshot.getDownloadUrl();
                    ChatPojo chatPojo = new ChatPojo(userName, uploaded.toString(), null );
                    databaseReference.push().setValue(chatPojo);
                }
            });
        }

    }
}
