package redRock.Tech.com;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Button buttonFetchStudent;
    private TextView textViewStudentInfo;

    private ApiService apiService;
    private String authToken; // Store the JWT token

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonFetchStudent = findViewById(R.id.button_fetch_student);
        textViewStudentInfo = findViewById(R.id.textview_student_info);

        // Set up Retrofit
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.56.1:3000/") // Replace with your VM's IP address
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        // Perform login to get the token
        performLogin();

        // Set button click listener
        buttonFetchStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (authToken != null) {
                    fetchStudentInfo("S12345"); // Replace with actual StudentID
                } else {
                    Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void performLogin() {
        LoginRequest loginRequest = new LoginRequest("redrockadmin", "RedR0cks!");

        Call<LoginResponse> call = apiService.login(loginRequest);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    LoginResponse loginResponse = response.body();
                    authToken = loginResponse.getToken();
                    Log.d(TAG, "Login successful. Token: " + authToken);
                } else {
                    Log.e(TAG, "Login failed: " + response.code());
                    Toast.makeText(MainActivity.this, "Login failed.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e(TAG, "Login error: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Login error.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchStudentInfo(String studentId) {
        String bearerToken = "Bearer " + authToken;
        Call<Student> call = apiService.getStudentInfo(bearerToken, studentId);
        call.enqueue(new Callback<Student>() {
            @Override
            public void onResponse(Call<Student> call, Response<Student> response) {
                if (response.isSuccessful()) {
                    Student student = response.body();
                    displayStudentInfo(student);
                } else {
                    Log.e(TAG, "Failed to fetch student info: " + response.code());
                    Toast.makeText(MainActivity.this, "Failed to fetch student info.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Student> call, Throwable t) {
                Log.e(TAG, "Error fetching student info: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Error fetching student info.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayStudentInfo(Student student) {
        String info = "ID: " + student.getStudentID() + "\n"
                + "Name: " + student.getFirstName() + " " + student.getLastName() + "\n"
                + "Email: " + student.getEmail() + "\n"
                + "Class: " + student.getClassName();

        textViewStudentInfo.setText(info);
    }
}
