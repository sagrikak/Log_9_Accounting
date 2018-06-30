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

public class DisplayCashActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private FirebaseDatabase mDatabase;
    private DatabaseReference dbRef, offsetRef, mReimbursementRef, mEmployeeRef;

    private DatePicker datePicker;
    private Calendar c;
    private String curYear = "0", curMonth, dateCash, radioCash, refIdCash, date, curSession, head;
    private EditText descriptionCash, amountCash, reimburseCash;
    private Spinner spinner;
    private int addCounterCount, getCounterCount, generateRefIdCount, uploadImageCount, addDataCount;

    private StorageReference mStorageRef, imageRef;
    private static final int SELECT_FILE = 100, REQUEST_CAMERA = 101, MY_PERMISSIONS_REQUEST_CAMERA = 200;
    private Uri selectedImage, imageUrl, image;
    private File file;
    private ProgressDialog imageProgress;
    private UploadTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_cash);
        radioCash = "Debit";

        addCounterCount = 1;
        getCounterCount = 1;
        generateRefIdCount = 1;
        uploadImageCount = 1;
        addDataCount = 1;

        mDatabase = FirebaseDatabase.getInstance();
        dbRef = mDatabase.getReference("Cash");
        mReimbursementRef = FirebaseDatabase.getInstance().getReference("Reimbursement");
        mEmployeeRef = FirebaseDatabase.getInstance().getReference("Employee");
        offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");

        c = Calendar.getInstance();
        datePicker = findViewById(R.id.date_cash); // initiate a date picker
        datePicker.setMaxDate(c.getTimeInMillis());

        descriptionCash = findViewById(R.id.et_description);
        amountCash = findViewById(R.id.et_amount);
        reimburseCash = findViewById(R.id.et_reimbursement);
        spinner = findViewById(R.id.spinner_head);

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

    public void setDate() {
        offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                double offset = snapshot.getValue(Double.class);
                double estimatedServerTimeMs = System.currentTimeMillis() + offset;
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

    public void onItemSelected(AdapterView parent, View view, int position, long id) {
        // On selecting a spinner item
        head = parent.getItemAtPosition(position).toString();
    }

    public void onNothingSelected(AdapterView arg0) {
        // TODO Auto-generated method stub
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

    public static String getDate(long milliSeconds, String dateFormat)
    {
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

    public void onImageButtonClicked(View view) {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(DisplayCashActivity.this);
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
        int permissionCheck = ContextCompat.checkSelfPermission(DisplayCashActivity.this,
                android.Manifest.permission.CAMERA);
        if(permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            file = new File(this.getExternalCacheDir(),
                    String.valueOf(System.currentTimeMillis()) + ".jpg");
            selectedImage = Uri.fromFile(file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImage);
            this.startActivityForResult(intent, REQUEST_CAMERA);
        }
        else  {
            ActivityCompat.requestPermissions(DisplayCashActivity.this,
                    new String[]{android.Manifest.permission.CAMERA},
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

                    Toast.makeText(DisplayCashActivity.this, "Camera Permission Granted", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    file = new File(this.getExternalCacheDir(),
                            String.valueOf(System.currentTimeMillis()) + ".jpg");
                    selectedImage = Uri.fromFile(file);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImage);
                    this.startActivityForResult(intent, REQUEST_CAMERA);
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Toast.makeText(DisplayCashActivity.this, "Camera Permission Denied. You can't use camera for this task.", Toast.LENGTH_SHORT).show();

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
                    Toast.makeText(DisplayCashActivity.this, "Error Occured", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(DisplayCashActivity.this, "Image selected", Toast.LENGTH_SHORT).show();
                break;
            case REQUEST_CAMERA:
                if (resultCode == RESULT_OK) {
                    Log.d("Image", "clicked");
                    CropImage.activity(selectedImage).setGuidelines(CropImageView.Guidelines.ON).start(this);
                }
        }
    }

    public void uploadImage(String imageName, final int monthlyCounter) {
        //create reference to images folder and assigning a name to the file that will be uploaded
        imageRef = mStorageRef.child("Cash/" + curSession + "/" + imageName);

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
                Log.d("uploadingerror", String.valueOf(exception.getMessage()));
                Toast.makeText(DisplayCashActivity.this, "Error in uploading!", Toast.LENGTH_SHORT).show();
                imageProgress.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                imageUrl = taskSnapshot.getDownloadUrl();
                Toast.makeText(DisplayCashActivity.this, "Image successfully uploaded", Toast.LENGTH_SHORT).show();
                if(addDataCount == 1) {
                    addData(monthlyCounter);
                    addDataCount = 0;
                }
            }
        });
    }

    public void onSubmit(View view) {
        dateCash = String.valueOf(datePicker.getDayOfMonth()) + "/" + String.valueOf(datePicker.getMonth() + 1) + "/" + String.valueOf(datePicker.getYear());

        if (TextUtils.isEmpty(descriptionCash.getText().toString()) || TextUtils.isEmpty(amountCash.getText().toString()) || selectedImage == null || head == null) {
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
        //starting upload

        operationsStart();
    }

    public void operationsStart() {
        setDate();
    }

    public void addCounter() {
        dbRef.child(curSession).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    Log.d("Session", "exists");
                    dbRef.child(curSession).child(curMonth).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() == null) {
                                Log.d("Month", "created");
                                dbRef.child(curSession).child(curMonth).child("Monthly Counter").setValue("1");
                            }
                            if(getCounterCount == 1) {
                                getCounter();
                                getCounterCount = 0;
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    Log.d("Session", "does not exist");
                    dbRef.child(curSession).child(curMonth).child("Monthly Counter").setValue("1");
                    if(getCounterCount == 1) {
                        getCounter();
                        getCounterCount = 0;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getCounter() {
        dbRef.child(curSession).child(curMonth).child("Monthly Counter").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                if(generateRefIdCount == 1) {
                    generateRefId(Integer.valueOf(value));
                    generateRefIdCount = 0;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void generateRefId(int monthlyCounter) {
        refIdCash = finalCurMonth(curMonth) + finalMonthlyCounter(monthlyCounter);
        if(uploadImageCount == 1) {
            uploadImage(refIdCash, monthlyCounter);
            uploadImageCount = 0;
        }
    }

    public void addData(int monthlyCounter) {
        dbRef.child(curSession).child(curMonth).child(refIdCash).child("Transaction Date").setValue(dateCash);
        dbRef.child(curSession).child(curMonth).child(refIdCash).child("ID").setValue(refIdCash);
        dbRef.child(curSession).child(curMonth).child(refIdCash).child("Description").setValue(descriptionCash.getText().toString());
        dbRef.child(curSession).child(curMonth).child(refIdCash).child("Amount").setValue(amountCash.getText().toString());
        dbRef.child(curSession).child(curMonth).child(refIdCash).child("Type").setValue(radioCash);
        dbRef.child(curSession).child(curMonth).child(refIdCash).child("Image").setValue(String.valueOf(imageUrl));
        dbRef.child(curSession).child(curMonth).child(refIdCash).child("Transaction Head").setValue(head);
        if (!TextUtils.isEmpty(reimburseCash.getText().toString())) {
            dbRef.child(curSession).child(curMonth).child(refIdCash).child("Reimbursement ID").setValue(reimburseCash.getText().toString());

            String reimburse = "";
            for (int i=3; i<reimburseCash.getText().toString().length(); i++){
                reimburse += reimburseCash.getText().toString().charAt(i);
            }
            final String re;
            re = reimburse;

            String reimburseId = reimburseCash.getText().toString();

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
                                        Integer.valueOf(amountCash.getText().toString())));
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

        monthlyCounter += 1;
        dbRef.child(curSession).child(curMonth).child("Monthly Counter").setValue(String.valueOf(monthlyCounter));

        imageProgress.dismiss();

        Toast toast = Toast.makeText(getApplicationContext(), "Details Uploaded", Toast.LENGTH_SHORT);
        toast.show();

        Intent intent = new Intent(this, CashSummaryActivity.class);
        startActivity(intent);
    }

    public String finalCurMonth(String x) {
        if(x.equals("1"))
            return "J";
        else if(x.equals("2"))
            return "K";
        else if(x.equals("3"))
            return "L";
        else if(x.equals("4"))
            return "A";
        else if(x.equals("5"))
            return "B";
        else if(x.equals("6"))
            return "C";
        else if(x.equals("7"))
            return "D";
        else if(x.equals("8"))
            return "E";
        else if(x.equals("9"))
            return "F";
        else if(x.equals("10"))
            return "G";
        else if(x.equals("11"))
            return "H";
        else
            return "I";
    }

    public String finalMonthlyCounter(int x) {
        if (x < 10)
            return "00" + String.valueOf(x);
        else if (x < 100)
            return "0" + String.valueOf(x);
        else return String.valueOf(x);
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.radio_debit:
                if (checked)
                    radioCash = "Debit";
            case R.id.radio_credit:
                if (checked)
                    radioCash = "Credit";
        }
    }
}