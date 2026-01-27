package com.finalProject.ali.user.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class BizApiService {

    // 공공데이터포털에서 발급받은 Decoding 인증키
    private final String serviceKey = "2a8d2cd12e7fad9b60fdb599dc8983fe8588f388b9dfee0494611ea3440fca60";

    public boolean verifyBusinessNumber(String b_no) {
        try {
            // 1. API URL 설정
            String apiUrl = "https://api.odcloud.kr/api/nts-businessman/v1/status?serviceKey=" + serviceKey;
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // 2. HTTP 설정
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            conn.setConnectTimeout(3000); // 3초 안에 연결 안 되면 끊기
            conn.setReadTimeout(3000);    // 3초 안에 응답 없으면 끊기

            // 3. 요청 데이터 구성 (JSON)
            JSONObject requestBody = new JSONObject();
            JSONArray bNoArray = new JSONArray();
            bNoArray.put(b_no.replace("-", "")); // 하이픈 제거
            requestBody.put("b_no", bNoArray);

            // 4. 데이터 전송
            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.toString().getBytes("utf-8"));
            }

            // 5. 응답 결과 읽기
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }

            // 6. 결과 분석 (b_stt_cd 가 "01"이면 계속 사업 중인 상태)
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray dataArray = jsonResponse.getJSONArray("data");
            if (dataArray.length() > 0) {
                String status = dataArray.getJSONObject(0).getString("b_stt_cd");
                return "01".equals(status); // 01: 영업중, 02: 휴업, 03: 폐업
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}