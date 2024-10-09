package redRock.Tech.com;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @GET("students/{id}")
    Call<Student> getStudentInfo(@Header("Authorization") String token, @Path("id") String studentId);
}