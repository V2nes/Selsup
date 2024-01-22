package org.example;



import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CrptApi {
    private final TimeUnit timeUnit;
    private final int requestLimit;
    private final Object lock = new Object();
    private AtomicInteger requestCount = new AtomicInteger(0);

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
    }

    public void createDocument(String document, String signature) throws IOException {
        if (!tryAcquire()){
            return;
        }
        try(CloseableHttpClient httpClient = HttpClients.createDefault()){
            HttpPost httpPost = new HttpPost("https://ismp.crpt.ru/api/v3/lk/documents/create");
            httpPost.addHeader("Content-Type","application/json");
            httpPost.setEntity(new StringEntity(document));

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity,"UTF-8");

        }finally {
            release();
        }
    }

    public boolean tryAcquire(){
        synchronized (lock){
            int count = requestCount.incrementAndGet();
            if (count > requestLimit){
                requestCount.decrementAndGet();
                return false;
            }
            return true;
        }
    }
    public void release(){
        synchronized (lock){
            requestCount.decrementAndGet();
        }
    }
}
