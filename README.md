# Network-Retrofit
## OkHttp
### Overview
- OkHttp là một third-party library được phát triển bài Square cho mục đích gửi và nhận HTTP-based network requests. Nó được xây dựng dựa trên Okio, nơi cố gắng hiệu quả hơn về đọc và ghi dữ liệu so với các thư viện I/O Java tiêu chuẩn bằng cách tạo một 
shared memory pool. Đây cũng là thư viện cơ bản cho thư viện Retrofit cung cấp type safety cho việc sử dụng API dựa trên REST.
- Thư viện OkHttp thực sự cung cấp một triển khai trên HttpUrlConnection, phiên bản Android 4.4 trở lên hiện đang sử dụng. Do đó, khi sử dụng cách tiếp cận thủ công được mô tả trong phần này của hướng dẫn, class HttpUrlConnection bên dưới có thể sử dụng code từ thư viện OkHttp. Tuy nhiên, có một API riêng được cung cấp bởi OkHttp giúp gửi và nhận các yêu cầu mạng dễ dàng hơn
- Với OkHttp v2.4 cũng cung cấp một cách cập nhật hơn để quản lý URL . Thay vì các class java.net.URL, java.net.URI hoặc android.net.Uri, nó cung cấp một lớp HTTPUrl mới giúp dễ dàng lấy cổng HTTP, phân tích cú pháp URL và chuỗi URL chuẩn hóa.
### Setup
- Cung cấp quyên internet 

          <uses-permission android:name="android.permission.INTERNET"/>
          
- Add dependecy:

          implementation 'com.squareup.okhttp3:okhttp:3.11.0'
          
- Chú ý: Nếu ta đang nâng cấp từ phiên bản cũ hơn của OkHttp, quá trình nhập của bạn cũng sẽ cần được thay đổi từ import com.sapesup.okhttp.XXXX để import okhttp3.XXXX.
- Nếu đang có ý định sử dụng Picasso với OkHttp3, có thể nâng cấp Picasso lên phiên bản snapshot hoặc sử dụng custom dowloader

        dependencies {
          implementation 'com.jakewharton.picasso:picasso2-okhttp3-downloader:1.1.0'
        }
        
          // Use OkHttpClient singleton
          OkHttpClient client = new OkHttpClient();
          Picasso picasso = new Picasso.Builder(context).downloader(new OkHttp3Downloader(client)).build();
          
### Sending and Receiving Network Requests
- Tạo request:

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                                 .url("http://publicobject.com/helloworld.txt")
                                 .build();
                                 
- Nếu có bất kỳ tham số truy vấn nào cần được thêm vào, class HttpUrl do OkHttp cung cấp có thể được sử dụng để xây dựng URL:

          HttpUrl.Builder urlBuilder = HttpUrl.parse("https://ajax.googleapis.com/ajax/services/search/images").newBuilder();
          urlBuilder.addQueryParameter("v", "1.0");
          urlBuilder.addQueryParameter("q", "android");
          urlBuilder.addQueryParameter("rsz", "8");
          String url = urlBuilder.build().toString();

          Request request = new Request.Builder()
                               .url(url)
                               .build();
                               
- Thêm header 
            
            Request request = new Request.Builder()
            .header("Authorization", "token abcd")
            .url("https://api.github.com/users/codepath")
            .build();
   
### Synchronous Network Calls

- Ta có thể tạo 1 object call để gửi request synchronously

          Response response = client.newCall(request).execute();
          
- Vì ko thể call api trên main thread, -> chỉ có thể sử dụng synchronously call trên background thread hay background service, hay sử dụng asynsTask để call network

### Asynchronous Network Calls
- Ta có thể gọi asynchronous bằng các tạo call và sử dụng enqueue() để lắng nghe callback

            client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
            }
        }
        
### Processing Network Responses
- Giả sử request ko bị hủy và ko có vấn đề về kết nối, hàm onRespone() sẽ bị loại bỏ. Nó pass qua 1 object response ta có thể check status code, response body hay bất kỳ header nào trả về

          if (!response.isSuccessful()) {
              throw new IOException("Unexpected code " + response);
          }
          
- Header trả về đc cung cấp bởi 1 list

            Headers responseHeaders = response.headers();
            for (int i = 0; i < responseHeaders.size(); i++) {
              Log.d("DEBUG", responseHeaders.name(i) + ": " + responseHeaders.value(i));
            }

- Cũng có thể lấy trực tiếp bằng response.header()

          String header = response.header("Date");
          
- Ta cũng có thể nhận được dữ liệu response bằng cách gọi answer.body() và sau đó gọi string() để đọc entrie payload. Lưu ý rằng answer.body() chỉ có thể được chạy một lần và nên được thực hiện trên background thread

### Processing JSON data

            Request request = new Request.Builder()
             .url("https://api.github.com/users/codepath")
             .build();
          
- Passing jsonobject
                    
            client.newCall(request).enqueue(new Callback() {
                  @Override
                  public void onResponse(Call call, final Response response) throws IOException {  
                      try {
                          String responseData = response.body().string();
                          JSONObject json = new JSONObject(responseData);
                          final String owner = json.getString("name");
                      } catch (JSONException e) {

                      }
                  }
              });
              
### Processing JSON data with Gson

              static class GitUser {
                  String name;
                  String url;
                  int id;
              }
              
              // Create new gson object
              final Gson gson = new Gson();
              // Get a handler that can be used to post to the main thread
              client.newCall(request).enqueue(new Callback() {
                  // Parse response using gson deserializer
                  @Override
                  public void onResponse(Call call, final Response response) throws IOException {
                      // Process the data on the worker thread
                      GitUser user = gson.fromJson(response.body().charStream(), GitUser.class);
                      // Access deserialized user object here
                  }
              }
              
### Sending Authenticated Requests
- OkHttp có một cơ chế để sửa đổi các request gửi đi bằng cách sử dụng các bộ chặn( interceptors). Một trường hợp sử dụng phổ biến là giao thức OAuth, yêu cầu các requét được ký bằng private key. Thư viện bảng chỉ dẫn OkHttp làm việc với thư viện SignPost để sử dụng một bộ chặn để ký từng request. Bằng cách này, người gọi không cần phải nhớ ký từng request :

            OkHttpOAuthConsumer consumer = new OkHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
            consumer.setTokenWithSecret(token, secret);
            okHttpClient.interceptors().add(new SigningInterceptor(consumer));  

### Caching Network Responses

            int cacheSize = 10 * 1024 * 1024; // 10 MiB
            Cache cache = new Cache(new File(getApplication().getCacheDir(),"cacheFileName"), cacheSize);
            OkHttpClient client = new OkHttpClient.Builder().cache(cache).build();
            
- Có thể kiểm soát xem có truy xuất response được lưu trong bộ nhớ cache hay không bằng cách đặt thuộc tính cacheControl trên request. Ví dụ: nếu ta chỉ muốn truy xuất request nếu dữ liệu được lưu trữ 

            Request request = new Request.Builder()
                .url("http://publicobject.com/helloworld.txt")
                .cacheControl(new CacheControl.Builder().onlyIfCached().build())
                .build();
                
- Ta có thể bắt response bằng noCache() cho request:
      
          .cacheControl(new CacheControl.Builder().noCache().build())        
          
- 
 
                               
                               
