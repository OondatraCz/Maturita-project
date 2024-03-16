package project.taskmaster.choremaster;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.UUID;

import project.taskmaster.choremaster.databinding.FragmentSettingsBinding;


public class SettingsFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FragmentSettingsBinding binding;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private SharedPreferences sharedPreferences;
    private List<String> groupIds;
    private List<String> groupNames;
    private static final int CREATE_GROUP_REQUEST_CODE = 1;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_GROUP_REQUEST_CODE && resultCode == RESULT_OK) {
            updateGroupSpinner(sharedPreferences.getString("activeGroupId", null));
        }
        Toast.makeText(getActivity(), "funguje to", Toast.LENGTH_SHORT).show();
    }

    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        groupIds = loadGroupIds();
        groupNames = loadGroupNames();
        populateGroupSpinner();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        sharedPreferences = getActivity().getSharedPreferences("ChoreMaster", Context.MODE_PRIVATE);
        groupIds = loadGroupIds();
        groupNames = loadGroupNames();
    }

    private void updateGroupSpinner(String newGroupId) {

        populateGroupSpinner();

        /*FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("groups").document(newGroupId).get()
                .addOnSuccessListener(groupSnapshot -> {
                    if (groupSnapshot.exists()) {

                        String newGroupName = groupSnapshot.getString("name");
                        groupIds.add(newGroupId);

                        ArrayAdapter<String> adapter = (ArrayAdapter<String>) binding.groupSelectionSpinner.getAdapter();
                        adapter.add(newGroupName);
                        adapter.notifyDataSetChanged();
                        binding.groupSelectionSpinner.setSelection(groupIds.indexOf(newGroupId));
                        groupNames.add(newGroupName);
                        saveGroupIds();
                        saveGroupNames();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (user == null) {
            Intent intent = new Intent(getActivity(), Login.class);
            startActivity(intent);
        } else{
            //binding.txt.setText(user.getEmail());
        }

        populateGroupSpinner();

        binding.groupSelectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sharedPreferences.edit().putString("activeGroupId", groupIds.get(position)).commit();
                //Toast.makeText(getActivity(), groupIds.get(position), Toast.LENGTH_SHORT).show();
                //binding.txt.setText(groupIds.get(position)/*sharedPreferences.getString("activeGroupId", null)*/);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.btnManageGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupId = sharedPreferences.getString("activeGroupId", null);
                if (groupId != null) {
                    checkUserRoleAndStartActivity(groupId);
                } else {
                    Toast.makeText(getActivity(), "No group selected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.btnLeaveGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = user.getUid();
                String groupId = sharedPreferences.getString("activeGroupId", null);

                if (groupId == null) {
                    Toast.makeText(getActivity(), "No group selected", Toast.LENGTH_SHORT).show();
                    return;
                }


                db.collection("groups").document(groupId)
                        .update("members." + userId, FieldValue.delete())
                        .addOnSuccessListener(aVoid -> {
                            db.collection("users").document(userId)
                                    .update("groups", FieldValue.arrayRemove(groupId))
                                    .addOnSuccessListener(bVoid -> {
                                        Toast.makeText(getActivity(), "Group left successfully", Toast.LENGTH_SHORT).show();
                                        groupNames.remove(groupIds.indexOf(groupId));
                                        groupIds.remove(groupId);

                                        saveGroupIds();
                                        saveGroupNames();

                                        if(!groupIds.isEmpty()){
                                            sharedPreferences.edit().putString("activeGroupId", groupIds.get(0)).commit();
                                        } else {
                                            sharedPreferences.edit().remove("activeGroupId").commit();
                                        }
                                        populateGroupSpinner();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        })
                        .addOnFailureListener(e -> Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        binding.btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences.edit().clear().commit();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), Login.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        binding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle("Are you sure?");

                final EditText passwordInput = new EditText(getActivity());
                passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                dialog.setView(passwordInput);
                dialog.setMessage("Deleting this account will result in completely removing your account from the system and you will not be able to access the app.\nProvide your password to delete your account:");
                dialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String userPassword = passwordInput.getText().toString();
                                if (userPassword.isEmpty()) return;
                                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), userPassword);
                                user.reauthenticate(credential)
                                        .addOnSuccessListener(bVoid -> {
                                            String userId = user.getUid();

                                            for (String groupId : groupIds) {
                                                db.collection("groups").document(groupId)
                                                        .update("members." + userId, FieldValue.delete())
                                                        .addOnSuccessListener(aVoid -> {
                                                            db.collection("groups").document(groupId).get()
                                                                    .addOnSuccessListener(groupSnapshot -> {
                                                                        if (groupSnapshot.exists()) {
                                                                            Map<String, Object> groupData = groupSnapshot.getData();
                                                                            if (groupData != null) {
                                                                                Map<String, Object> members = (Map<String, Object>) groupData.get("members");
                                                                                if (members == null || members.isEmpty()) {
                                                                                    // Group is empty, delete it
                                                                                    db.collection("groups").document(groupId).delete();
                                                                                }
                                                                            }
                                                                        }
                                                                    });
                                                        });
                                                // TODO - vyresit tasky smazaneho uzivatele! vyresit predani role moda! vyresit samzani skupiny kdyz je prazdna - staci checknout jestli to userId je tam jenom jedno predtim nez se maze.
                                            }


                                            db.collection("users").document(userId).delete();

                                            user.delete()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            Toast.makeText(getActivity(), "Account deleted", Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(getActivity(), Register.class);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(intent);
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        });
                            }
                        });
                dialog.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();

            }
        });

        binding.btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getContext(), CreateGroupActivity.class), CREATE_GROUP_REQUEST_CODE);
            }
        });

        binding.btnInviteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String activeGroupId = sharedPreferences.getString("activeGroupId", null);
                db.collection("groups").document(activeGroupId).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()){
                                String role = documentSnapshot.getString("members." + user.getUid() + ".role");
                                if(role.equals("admin")){
                                    //String uuid = UUID.randomUUID().toString(); - jenom pokud bych chtěl řešit vypršení odkazu po určité době
                                    String link = "https://chore-master-project.web.app/?groupId=" + sharedPreferences.getString("activeGroupId", null)/* + "&uuid=" + uuid*/;

                                    Intent sendIntent = new Intent();
                                    sendIntent.setAction(Intent.ACTION_SEND);
                                    sendIntent.putExtra(Intent.EXTRA_TEXT, "Join our group in the app: " + link);
                                    sendIntent.setType("text/plain");

                                    Intent shareIntent = Intent.createChooser(sendIntent, null);
                                    startActivity(shareIntent);
                                }
                                else{
                                    Toast.makeText(getActivity(), "You are not an admin of this group", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

        return view;
    }

    private void populateGroupSpinner(){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, groupNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.groupSelectionSpinner.setAdapter(adapter);
        binding.groupSelectionSpinner.setSelection(groupIds.indexOf(sharedPreferences.getString("activeGroupId", null)));
    }
    private void saveGroupIds() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String joined = TextUtils.join(",", groupIds);

        editor.putString("allGroupIds", joined);
        editor.apply();
    }
    private List<String> loadGroupIds() {
        String storedString = sharedPreferences.getString("allGroupIds", "");

        if (!storedString.equals("")) {
            return new ArrayList<>(Arrays.asList(storedString.split(",")));
        } else {
            return new ArrayList<>();
        }
    }
    private void saveGroupNames() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String joined = TextUtils.join(",", groupNames);

        editor.putString("allGroupNames", joined);
        editor.apply();
    }
    private List<String> loadGroupNames() {
        String storedString = sharedPreferences.getString("allGroupNames", "");

        if (!storedString.equals("")) {
            return new ArrayList<>(Arrays.asList(storedString.split(",")));
        } else {
            return new ArrayList<>();
        }
    }

    private void checkUserRoleAndStartActivity(String groupId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("groups").document(groupId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> members = (Map<String, Object>) documentSnapshot.get("members");
                        if (members != null && members.containsKey(userId)) {
                            Map<String, Object> userDetails = (Map<String, Object>) members.get(userId);
                            String role = (String) userDetails.get("role");
                            if ("admin".equals(role)) {
                                startActivity(new Intent(getActivity(), ManageGroupActivity.class));
                            } else {
                                Toast.makeText(getActivity(), "You are not an admin of this group", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(getActivity(), "Group not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Error checking user role", Toast.LENGTH_SHORT).show());
    }
}