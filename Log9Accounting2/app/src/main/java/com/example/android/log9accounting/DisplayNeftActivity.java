package com.example.android.log9accounting;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import android.Manifest;

public class DisplayNeftActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private DatabaseReference dbRef, offsetRef, mReimbursementRef, mEmployeeRef;
    private EditText descriptionNeft, amountNeft, reimburseNeft;
    private Spinner spinner;
    private DatePicker datePicker;
    private Calendar c;
    private String typeNeft, categoryNeft, curYear, dateCard, curMonth, curSession, date, head;

    private StorageReference mStorageRef, imageRef;
    private int addCounterCount, getCounterCount, setDateCount;
    private static final int SELECT_FILE = 100, REQUEST_CAMERA = 101, MY_PERMISSIONS_REQUEST_CAMERA= 200;
    private Uri selectedImage, imageUrl, image;
    private File file;

    private ProgressDialog imageProgress;
    private UploadTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_neft);
        typeNeft = "Admin";
        categoryNeft = "Capital";

        getCounterCount = 1;
        setDateCount = 1;
        addCounterCount = 1;

        dbRef = FirebaseDatabase.getInstance().getReference("NEFT");
        mReimbursementRef = FirebaseDatabase.getInstance().getReference("Reimbursement");
        mEmployeeRef = FirebaseDatabase.getInstance().getReference("Employee");
        offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");

        datePicker = findViewById(R.id.date_neft);
        descriptionNeft = findViewById(R.id.et_description);
        amountNeft = findViewById(R.id.et_amount);
        reimburseNeft = findViewById(R.id.et_reimbursement);
        spinner = findViewById(R.id.spinner_head);

        c = Calendar.getInstance();
        datePicker.setMaxDate(c.getTimeInMillis());
        mStorageRef = FirebaseStorage.getInstance().getReference();

        spinner.setOnItemSelectedListener(this);

        List<String> categories = new ArrayList<>();
        categories.add("Automobile");
        categories.add("Business Services");
        categories.add("Computers");
        categories.add("Education");
        categories.add("Personal");
        categories.add("Travel");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, AdminMainActivity.class);
        startActivity(intent);
        finish();
    }

    public void onItemSelected(AdapterView parent, View view, int position, long id) {
        // On selecting a spinner item
        head = parent.getItemAtPosition(position).toString();
    }

    public void onNothingSelected(AdapterView arg0) {
        // TODO Auto-generated method stub
    }

    public void setDate() {
        offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                double offset = snapshot.getValue(Double.class);
                double estimatedServerTimeMs = System.currentTimeMillis() + offset;
                datePicker.setMaxDate(number(estimatedServerTimeMs));
                date = getDate(number(estimatedServerTimeMs), "dd/MM/yyyy hh:mm:ss");
                curYear = String.valueOf(date.charAt(6)) + String.valueOf(date.charAt(7)) + String.valueOf(date.charAt(8)) + String.valueOf(date.charAt(9));
                curMonth = String.valueOf(date.charAt(3)) + String.valueOf(date.charAt(4));
                Log.d("currentmonth", curMonth);
                Log.d("currentyear", curYear);
                curSession = getSession(curMonth, curYear);
                Log.d("currentsession", curSession);
                if(Integer.valueOf(curYear) > 1974) {
                    if(addCounterCount == 1) {
                        addCounter();
                        addCounterCount = 0;
                    }
                }
                else {
                    onSubmit(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Listener was cancelled");
            }
        });
    }

    public String getSession(String month, String year) {
        if(Integer.valueOf(month) < 4) {
            return String.valueOf(Integer.valueOf(year) - 1) + '-' + year;
        } else {
            return year + '-' + String.valueOf(Integer.valueOf(year) + 1);
        }
    }

    public long number(double time) {
        String strTime = String.valueOf(time);
        String strFinalTime = "";
        Log.d("Current time", strTime);
        for (int i=0; i<strTime.length(); i++) {
            if(strTime.charAt(i) == 'E')
                break;
            if(strTime.charAt(i) != '.')
                strFinalTime+=strTime.charAt(i);
        }
        Log.d("Current time", strFinalTime);
        return Long.valueOf(strFinalTime);
    }

    public static String getDate(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void onRadioTypeButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.radio_admin:
                if (checked)
                    typeNeft = "Admin";
            case R.id.radio_research:
                if (checked)
                    typeNeft = "Research";
        }
    }

    public void onRadioCategoryButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.radio_capital:
                if (checked)
                    categoryNeft = "Capital";
            case R.id.radio_expenses:
                if (checked)
                    categoryNeft = "Expenses";
        }
    }

    public void onImageButtonClicked(View view) {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(DisplayNeftActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    cameraIntent();
                } else if (items[item].equals("Choose from Library")) {
                    galleryIntent();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public void cameraIntent() {
        int permissionCheck = ContextCompat.checkSelfPermission(DisplayNeftActivity.this,
                Manifest.permission.CAMERA);
        if(permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            file = new File(this.getExternalCacheDir(),
                    String.valueOf(System.currentTimeMillis()) + ".jpg");
            selectedImage = Uri.fromFile(file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImage);
            this.startActivityForResult(intent, REQUEST_CAMERA);
        }
        else  {
            ActivityCompat.requestPermissions(DisplayNeftActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(DisplayNeftActivity.this, "Camera Permission Granted", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    file = new File(this.getExternalCacheDir(),
                            String.valueOf(System.currentTimeMillis()) + ".jpg");
                    selectedImage = Uri.fromFile(file);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImage);
                    this.startActivityForResult(intent, REQUEST_CAMERA);
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Toast.makeText(DisplayNeftActivity.this, "Camera Permission Denied. You can't use camera for this task.", Toast.LENGTH_SHORT).show();

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
        }
    }

    public void galleryIntent() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, SELECT_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SELECT_FILE:
                if (resultCode == RESULT_OK) {
                    Log.d("Image", "selected");
                    image = data.getData();
                    CropImage.activity(image).setGuidelines(CropImageView.Guidelines.ON).start(this);
                }
                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if(resultCode == RESULT_OK) {
                    selectedImage = result.getUri();
                } else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Toast.makeText(DisplayNeftActivity.this, "An error occured.", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(DisplayNeftActivity.this, "Image selected", Toast.LENGTH_SHORT).show();
                break;
            case REQUEST_CAMERA:
                if (resultCode == RESULT_OK) {
                    Log.d("Image", "clicked");
                    CropImage.activity(selectedImage).setGuidelines(CropImageView.Guidelines.ON).start(this);
                }
        }
    }

    public void onSubmit(View view) {
        dateCard = String.valueOf(datePicker.getDayOfMonth()) + "/" + String.valueOf(datePicker.getMonth() + 1) + "/" + String.valueOf(datePicker.getYear());

        if (TextUtils.isEmpty(descriptionNeft.getText().toString()) || TextUtils.isEmpty(amountNeft.getText().toString()) || selectedImage == null) {
            Toast toast = Toast.makeText(getApplicationContext(), "Please enter all the details", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        if (!isNetworkAvailable()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        //creating and showing progress dialog
        imageProgress = new ProgressDialog(this);
        imageProgress.setMax(100);
        imageProgress.setMessage("Uploading...");
        imageProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        if(isNetworkAvailable())
            imageProgress.show();
        imageProgress.setCancelable(false);

        operationsStart();
    }

    public void operationsStart() {
        setDate();
    }

    public void addCounter() {
        dbRef.child(curSession).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("addCounter", "true");
                if (dataSnapshot.getValue() == null) {
                    Log.d("Year", "does not exist");
                    dbRef.child(curSession).child("Yearly Counter").setValue("1");
                }
                if (getCounterCount == 1) {
                    getCounter();
                    getCounterCount = 0;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getCounter() {
        dbRef.child(curSession).child("Yearly Counter").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String yearlyCounter = dataSnapshot.getValue(String.class);
                Log.d("Yearly Counter", yearlyCounter);
                uploadImage(yearlyCounter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void uploadImage(final String imageName) {
        //create reference to images folder and assigning a name to the file that will be uploaded
        imageRef = mStorageRef.child("NEFT/" + curSession + "/" + imageName);

        //starting upload
        uploadTask = imageRef.putFile(selectedImage);
        // Observe state change events such as progress, pause, and resume
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                //sets and increments value of progressbar
                imageProgress.incrementProgressBy((int) progress);
            }
        });
        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(DisplayNeftActivity.this, "Error in uploading!", Toast.LENGTH_SHORT).show();
                imageProgress.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                imageUrl = taskSnapshot.getDownloadUrl();
                Toast.makeText(DisplayNeftActivity.this, "Image successfully uploaded", Toast.LENGTH_SHORT).show();
                addData(imageName);
            }
        });
    }

    public void addData(String refIdCard) {

        int yearlyCounter = Integer.valueOf(refIdCard);

        dbRef.child(curSession).child(refIdCard).child("Transaction Date").setValue(dateCard);
        dbRef.child(curSession).child(refIdCard).child("ID").setValue(refIdCard);
        dbRef.child(curSession).child(refIdCard).child("Description").setValue(descriptionNeft.getText().toString());
        dbRef.child(curSession).child(refIdCard).child("Amount").setValue(amountNeft.getText().toString());
        dbRef.child(curSession).child(refIdCard).child("Type").setValue(typeNeft);
        dbRef.child(curSession).child(refIdCard).child("Category").setValue(categoryNeft);
        dbRef.child(curSession).child(refIdCard).child("Image").setValue(String.valueOf(imageUrl));
        dbRef.child(curSession).child(refIdCard).child("Transaction Head").setValue(head);
        if (!TextUtils.isEmpty(reimburseNeft.getText().toString())) {
            dbRef.child(curSession).child(refIdCard).child("Reimbursement ID").setValue(reimburseNeft.getText().toString());

            String reimburse = "";
            for (int i=3; i<reimburseNeft.getText().toString().length(); i++){
                reimburse += reimburseNeft.getText().toString().charAt(i);
            }
            final String re;
            re = reimburse;

            String reimburseId = reimburseNeft.getText().toString();

            final String user_id = String.valueOf(reimburseId.charAt(0)) + String.valueOf(reimburseId.charAt(1)) + String.valueOf(reimburseId.charAt(2));
            Log.v("user", user_id);

            mReimbursementRef.child(user_id).child(reimburse).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue() != null) {
                        mReimbursementRef.child(user_id).child(re).child("0").child("status").setValue("paid");
                        mEmployeeRef.child(user_id).child("amount_paid").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String amount_paid = (String) dataSnapshot.getValue();
                                mEmployeeRef.child(user_id).child("amount_paid").setValue(String.valueOf(Integer.valueOf(amount_paid) +
                                        Integer.valueOf(amountNeft.getText().toString())));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), "The reimbursement id you entered does not exist.", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        yearlyCounter++;
        dbRef.child(curSession).child("Yearly Counter").setValue(String.valueOf(yearlyCounter));

        imageProgress.dismiss();

        Toast toast = Toast.makeText(getApplicationContext(), "Details Uploaded", Toast.LENGTH_SHORT);
        toast.show();

        Intent intent = new Intent(this, NeftSummaryActivity.class);
        startActivity(intent);
    }
}
