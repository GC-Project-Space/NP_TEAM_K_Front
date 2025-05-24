package com.example.np_team_k;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.common.KakaoSdk;
import com.kakao.sdk.user.UserApiClient;

import java.security.MessageDigest;
import java.util.UUID; //추가: guest ID 생성을 위한 UUID

public class LoginActivity extends AppCompatActivity {

    private static final String KAKAO_NATIVE_APP_KEY = "9ff2b589f0a0c62b3b8b633d6c167074";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        printKeyHash();

        KakaoSdk.init(this, KAKAO_NATIVE_APP_KEY);

        ImageButton kakaoLoginButton = findViewById(R.id.kakaoLoginButton);
        kakaoLoginButton.setOnClickListener(view -> {
            UserApiClient.getInstance().loginWithKakaoAccount(this, (OAuthToken token, Throwable error) -> {
                if (error != null) {
                    Log.e("KakaoLogin", "로그인 실패", error);
                } else if (token != null) {
                    Log.i("KakaoLogin", "로그인 성공: " + token.getAccessToken());

                    // 사용자 id 요청
                    UserApiClient.getInstance().me((user, meError) -> {
                        if (meError != null) {
                            Log.e("KakaoLogin", "사용자 정보 요청 실패", meError);
                        } else {
                            String kakaoId = String.valueOf(user.getId()); //Long → String
                            Log.d("KakaoLogin", "사용자 ID: " + kakaoId);

                            //다음 화면으로 ID 전달
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("kakaoId", kakaoId);
                            startActivity(intent);
                            finish();
                        }
                        return null;
                    });
                    // goToNextScreen();
                }
                return null;
            });
        });

        TextView guestLoginText = findViewById(R.id.guestLoginText);
        guestLoginText.setOnClickListener(view -> {
            Log.i("GuestLogin", "비회원 로그인 시도");
            //임의의 guest ID 생성 (UUID 일부)
            String guestId = "guest_" + UUID.randomUUID().toString().substring(0, 8);
            Log.d("GuestLogin", "임시 ID: " + guestId);

            //다음 화면으로 ID 전달
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("kakaoId", guestId);
            startActivity(intent);
            finish();
        });
    }

    private void goToNextScreen() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class); // MainActivity로 이동
        startActivity(intent);
        finish();
    }

    private void printKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(),
                    PackageManager.GET_SIGNING_CERTIFICATES);

            Signature[] signatures;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) { // API 28 이상
                signatures = info.signingInfo.getApkContentsSigners();
            } else {
                // 하위 버전 (API 24~27) 대응
                info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
                signatures = info.signatures;
            }

            for (Signature signature : signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String keyHash = Base64.encodeToString(md.digest(), Base64.NO_WRAP);
                Log.d("KeyHash", "keyHash: " + keyHash);
            }
        } catch (Exception e) {
            Log.e("KeyHash", "키 해시 추출 실패", e);
        }
    }


}
