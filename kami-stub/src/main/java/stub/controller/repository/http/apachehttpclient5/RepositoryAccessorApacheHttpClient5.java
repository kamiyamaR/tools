package stub.controller.repository.http.apachehttpclient5;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Component;

import stub.controller.repository.http.RepositoryAccessor;
import stub.controller.repository.http.RequestInfo;
import stub.controller.repository.http.ResponseInfo;

@Component
public class RepositoryAccessorApacheHttpClient5 implements RepositoryAccessor {

    @Override
    public ResponseInfo execute(RequestInfo input) throws Exception {

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            RequestConfig requestConfig = RequestConfig.custom() // 
                    .setConnectionRequestTimeout(1000, TimeUnit.MILLISECONDS) // 
                    .setResponseTimeout(3000, TimeUnit.MILLISECONDS) // 
                    .build();

            HttpUriRequestBase httpRequest = null;
            switch (input.getMethod().toUpperCase()) {
            case HttpGet.METHOD_NAME:
                httpRequest = new HttpGet(input.getUrl());
                break;
            case HttpHead.METHOD_NAME:
                httpRequest = new HttpHead(input.getUrl());
                break;
            case HttpPost.METHOD_NAME:
                httpRequest = new HttpPost(input.getUrl());
                break;
            case HttpPut.METHOD_NAME:
                httpRequest = new HttpPut(input.getUrl());
                break;
            default:
                throw new IllegalArgumentException(
                        "[" + input.getMethod() + "]はサポート外のリクエストメソッドです。 input=[" + input + "]");
            }

            httpRequest.setConfig(requestConfig);
            if (Objects.nonNull(input.getHeaders())) {
                for (Entry<String, List<String>> entry : input.getHeaders().entrySet()) {
                    if (HttpHeaders.HOST.toLowerCase().equals(entry.getKey().toLowerCase())) {
                        // hostヘッダは設定しない
                        continue;
                    }
                    for (String value : entry.getValue()) {
                        httpRequest.addHeader(entry.getKey(), value);
                    }
                }
            }

            if (Objects.nonNull(input.getBodyByteArray())) {
                StringEntity entity = new StringEntity(new String(input.getBodyByteArray(), StandardCharsets.UTF_8),
                        StandardCharsets.UTF_8);
                httpRequest.setEntity(entity);
            }

            ResponseInfo output = httpClient.execute(httpRequest, response -> {

                Map<String, List<String>> headers = new HashMap<String, List<String>>();
                Iterator<Header> it = response.headerIterator();
                while (it.hasNext()) {
                    Header header = it.next();
                    List<String> val = headers.get(header.getName());
                    if (Objects.isNull(val)) {
                        val = new ArrayList<String>();
                        headers.put(header.getName(), val);
                    }
                    val.add(header.getValue());
                }

                HttpEntity entity = response.getEntity();

                ResponseInfo responseInfo = new ResponseInfo();
                responseInfo.setStatusCode(response.getCode());
                responseInfo.setHeaders(headers);
                if (Objects.nonNull(entity)) {
                    byte[] bodyByteArray = EntityUtils.toByteArray(entity);
                    responseInfo.setBodyByteArray(bodyByteArray);
                }

                return responseInfo;
            });

            return output;

        } catch (Exception e) {
            throw e;
        }
    }

}
