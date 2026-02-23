package com.prm392_sp26.prm392_kitchen_mobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.prm392_sp26.prm392_kitchen_mobile.MainActivity;
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.model.request.LoginRequest;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.LoginResponse;
import com.prm392_sp26.prm392_kitchen_mobile.network.ApiClient;
import com.prm392_sp26.prm392_kitchen_mobile.shared.BaseResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * AuthActivity - Màn hình đăng nhập
 * Xử lý Google Sign-In với Firebase và gửi idToken đến backend
 */
public class AuthActivity extends AppCompatActivity {

    private static final String TAG = "AuthActivity";

    private FirebaseAuth firebaseAuth;
    private CredentialManager credentialManager;
    private PrefsManager prefsManager;

    private Button btnGoogleSignIn;
    private ProgressBar progressAuth;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.authMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo
        firebaseAuth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(this);
        prefsManager = PrefsManager.getInstance(this);

        // Bind views
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        progressAuth = findViewById(R.id.progressAuth);
        tvStatus = findViewById(R.id.tvStatus);

        // Set click listener
        btnGoogleSignIn.setOnClickListener(v -> startGoogleSignIn());
    }

    /**
     * Bắt đầu quá trình Google Sign-In
     */
    private void startGoogleSignIn() {
        setLoading(true);
        setStatus("Đang mở Google Account Chooser...");

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                this,
                request,
                null,
                getMainExecutor(),
                googleCredentialCallback
        );
    }

    /**
     * Callback xử lý kết quả từ Credential Manager
     */
    private final CredentialManagerCallback<GetCredentialResponse, GetCredentialException> googleCredentialCallback =
            new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                @Override
                public void onResult(GetCredentialResponse result) {
                    try {
                        Credential credential = result.getCredential();
                        GoogleIdTokenCredential googleCredential = GoogleIdTokenCredential.createFrom(credential.getData());

                        setStatus("Đã chọn tài khoản: " + googleCredential.getId());
                        firebaseAuthWithGoogle(googleCredential.getIdToken());
                    } catch (Exception e) {
                        setLoading(false);
                        setStatus("Đăng nhập Google thất bại: " + e.getMessage());
                        Log.e(TAG, "Google sign-in failed", e);
                    }
                }

                @Override
                public void onError(GetCredentialException e) {
                    setLoading(false);
                    setStatus("Đăng nhập bị hủy hoặc thất bại");
                    Log.e(TAG, "Google sign-in cancelled/failed", e);
                }
            };

    /**
     * Đăng nhập Firebase với Google credential
     */
    private void firebaseAuthWithGoogle(String googleIdToken) {
        if (googleIdToken == null || googleIdToken.trim().isEmpty()) {
            setLoading(false);
            setStatus("Thiếu Google ID token");
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(googleIdToken, null);
        setStatus("Đang đăng nhập Firebase...");

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, signInTask -> {
                    if (!signInTask.isSuccessful()) {
                        setLoading(false);
                        Exception e = signInTask.getException();
                        setStatus("Đăng nhập Firebase thất bại: " + (e == null ? "unknown" : e.getMessage()));
                        Log.e(TAG, "Firebase signInWithCredential failed", e);
                        return;
                    }

                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user == null) {
                        setLoading(false);
                        setStatus("Đăng nhập thành công nhưng user null");
                        return;
                    }

                    setStatus("Đăng nhập Firebase OK, đang lấy ID Token...");
                    fetchFirebaseIdTokenAndLoginBackend(user);
                });
    }

    /**
     * Lấy Firebase ID Token và gửi đến backend để xác thực
     */
    private void fetchFirebaseIdTokenAndLoginBackend(@NonNull FirebaseUser user) {
        user.getIdToken(true)
                .addOnCompleteListener(this, tokenTask -> {
                    if (!tokenTask.isSuccessful()) {
                        setLoading(false);
                        Exception e = tokenTask.getException();
                        setStatus("Lấy ID Token thất bại: " + (e == null ? "unknown" : e.getMessage()));
                        Log.e(TAG, "getIdToken failed", e);
                        return;
                    }

                    String idToken = tokenTask.getResult().getToken();
                    if (idToken == null || idToken.isEmpty()) {
                        setLoading(false);
                        setStatus("ID Token rỗng");
                        return;
                    }

                    setStatus("Đang gửi đến server...");
                    loginToBackend(idToken);
                });
    }

    /**
     * Gửi Firebase ID Token đến backend API /api/auth/login
     */
    private void loginToBackend(String idToken) {
        LoginRequest request = new LoginRequest(idToken);

        ApiClient.getInstance().getApiService().login(request).enqueue(new Callback<BaseResponse<LoginResponse>>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse<LoginResponse>> call,
                                   @NonNull Response<BaseResponse<LoginResponse>> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<LoginResponse> baseResponse = response.body();

                    if (baseResponse.isSuccess() && baseResponse.getData() != null) {
                        // Lưu thông tin đăng nhập
                        prefsManager.saveLoginResponse(baseResponse.getData());
                        setStatus("Đăng nhập thành công!");
                        Toast.makeText(AuthActivity.this, baseResponse.getMessage(), Toast.LENGTH_SHORT).show();

                        // Chuyển đến MainActivity
                        navigateToMain();
                    } else {
                        setStatus("Lỗi: " + baseResponse.getMessage());
                        Toast.makeText(AuthActivity.this, baseResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    setStatus("Lỗi server: " + response.code());
                    Log.e(TAG, "Login failed with code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse<LoginResponse>> call, @NonNull Throwable t) {
                setLoading(false);
                setStatus("Lỗi kết nối: " + t.getMessage());
                Log.e(TAG, "Login API call failed", t);
                Toast.makeText(AuthActivity.this, "Không thể kết nối đến server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Chuyển đến MainActivity sau khi đăng nhập thành công
     */
    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Hiển thị/ẩn loading
     */
    private void setLoading(boolean loading) {
        progressAuth.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnGoogleSignIn.setEnabled(!loading);
    }

    /**
     * Cập nhật status text
     */
    private void setStatus(String message) {
        tvStatus.setText(message);
        Log.d(TAG, message);
    }
}
