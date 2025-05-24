package com.example.newsapp.Helper;

import android.content.Context;
import android.util.Log;

import com.example.newsapp.Model.Articles;
import com.example.newsapp.XMLDOMParser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;


import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RSSUtils {

    public static void readRSS(Context context, String RSS_URL) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference articlesCollection = firestore.collection("articles");
        Executor executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .build();

                Request request = new Request.Builder()
                        .url(RSS_URL)
                        .header("Cache-Control", "no-cache")
                        .build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    Log.e("RSSUtils", "Phản hồi HTTP không thành công: " + response.code());
                    return;
                }

                String xmlContent = response.body().string();

                if (!RSS_URL.startsWith("https://")) {
                    Log.e("RSSUtils", "URL không an toàn (không phải HTTPS)");
                    return;
                }

                XMLDOMParser parser = new XMLDOMParser();
                Document document = parser.getDocument(xmlContent);
                if (document == null) {
                    Log.e("RSSUtils", "Không thể phân tích XML.");
                    return;
                }

                document.getDocumentElement().normalize();
                NodeList nodeList = document.getElementsByTagName("item");
                NodeList nodeListdescription = document.getElementsByTagName("description");

                List<Articles> articlesList = new ArrayList<>();

                for (int i = 0; i < 5 && i < nodeList.getLength(); i++) {
                    Element element = (Element) nodeList.item(i);
                    String title = parser.getValue(element, "title");
                    String pubDate = parser.getValue(element, "pubDate");
                    String link = parser.getValue(element, "link");
                    String cdata = nodeListdescription.item(i + 1).getTextContent();
                    String summary = cdata.replaceAll("<[^>]*>", "").trim();
                    String hinhanh = "";
                    String content = fetchContent(link);
                    Matcher m = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>").matcher(cdata);
                    if (m.find()) {
                        hinhanh = m.group(1);
                    }
                        Articles articles = new Articles(link,summary,title,content,null,pubDate,hinhanh,"NuinzIHcKepFEbOLRloo",null,0,0,null,true,"published");

                      articlesCollection.add(articles);
                        articlesList.add(articles);
                }



                Log.d("RSSUtils", "Đã thêm " + articlesList.size() + " bài viết vào database.");

            } catch (Exception e) {
                Log.e("RSSUtils", "Lỗi tải RSS: " + e.getMessage());
            }
        });
    }

    private static String fetchContent(String link) {
        StringBuilder content = new StringBuilder();

        try {
            org.jsoup.nodes.Document document = Jsoup.connect(link)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            Elements contentElements = document.select("article.fck_detail p.Normal");
            for (org.jsoup.nodes.Element element : contentElements) {
                content.append(element.text()).append("\n");
            }

            if (content.toString().trim().isEmpty()) {
                return "Không tìm thấy nội dung bài viết.";
            }

        } catch (IOException e) {
            content.append("Không thể tải nội dung: ").append(e.getMessage());
        }

        return content.toString().trim();
    }
}
