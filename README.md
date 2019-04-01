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
          
- Để nhận response trong bộ nhớ cache, ta chỉ cần gọi cacheResponse() trong Response :

                    Call call = client.newCall(request);
                    call.enqueue(new Callback() {
                      @Override
                      public void onFailure(Call call, IOException e) {

                      }

                      @Override
                      public void onResponse(Call call, final Response response) throws IOException
                      {
                         final Response text = response.cacheResponse();
                         // if no cached object, result will be null
                         if (text != null) {
                            Log.d("here", text.toString());
                         }
                      }
                    });       
                    
### Troubleshooting
- OkHttp có thể khó khắc phục sự cố khi cố gắng bước qua các lớp trừu tượng khác nhau trong các thư viện. Bạn có thể thêm HTTPLogInterceptor có thể được thêm khi sử dụng thư viện OkHttp3, nơi sẽ hiện các request/response HTTP thông qua Log. Ta cũng có thể tận dụng Stetho của Facebook để sử dụng Chrome để kiểm tra tất cả lưu lượng truy cập mạng.

### HttpLogInterceptor
- Để sử dụng HttpLogInterceptor, add dependency

                    implementation 'com.squareup.okhttp3:logging-interceptor:3.6.0'
                    
- Sẽ cần thêm network interceptor cho HttpLogInterceptor.

                    OkHttpClient.Builder builder = new OkHttpClient.Builder();
                    HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();

                    // Can be Level.BASIC, Level.HEADERS, or Level.BODY
                    // See http://square.github.io/okhttp/3.x/logging-interceptor/ to see the options.
                    httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                    builder.networkInterceptors().add(httpLoggingInterceptor);
                    builder.build();
                    
### Stetho
 
 - Sử dụng plugin Stetho của Facebook để theo dõi các request vs chrome 
 - Add dependecy
                    dependencies { 
                        implementation 'com.facebook.stetho:stetho-okhttp3:1.3.0' 
                    } 
                    
                    OkHttpClient client = new OkHttpClient.Builder()
                        .addNetworkInterceptor(new StethoInterceptor())
                        .build();
                        
- Init in Application 
                   
                   public class MyApplication extends Application {
                      public void onCreate() {
                        super.onCreate();
                        Stetho.initializeWithDefaults(this);
                      }
                    }
                    
### Using with Websockets
- Với Okhttp v3.5 bao gồm hỗ trợ cho web socket 2 chiều. Url nên được sử dụng phải đc thêm tiền tố vào ws:// hoặc wss:// cho phiên bản bảo mật. Mặc dù các cổng kết nối giống như HTTP (cổng 80 và cổng 443), máy chủ vẫn đc cấu hình để hỗ trợ WebSockets vì chúng là một giao thức hoàn toàn khác nhau                                                
                               
                    Request request = new Request.Builder().url(url).build();
                    WebSocket webSocket = client.newWebSocket(request,  = new WebSocketListener() {

                      @Override
                      public void onOpen(WebSocket webSocket, Response response) {
                         // connection succeeded
                      }

                      @Override
                      public void onMessage(WebSocket webSocket, String text) {
                         // text message received
                      }

                      @Override
                      public void onMessage(WebSocket webSocket, ByteString bytes) {
                         // binary message received
                      }

                      @Override
                      public void onClosed(WebSocket webSocket, int code, String reason) {
                         // no more messages and the connection should be released
                      }

                      @Override
                      public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                         // unexpected error 
                      });
                      
- Send message 

                    websocket.send("hello");
                    
- OkHttp xử lý tất cả công việc trên worker thread, vì vậy bạn không phải lo lắng về việc thực hiện các cuộc gọi Websocket trên main thread 

-Nếu bạn cần đóng kết nối đúng cách, hãy đảm bảo sử dụng status code =  1000. 

### Enabling TLS V1.2 on older devices
- Nếu thấy SSL handshake terminated và sử dụng các thiết bị Android 4.0, ta cần bật TLS v1.2 một cách rõ ràng. Android đã hỗ trợ TLS 1.2 kể từ API 16 (Android 4.1). phải đảm bảo rằng bạn đang sử dụng OpenSSL mới nhất bằng cách sử dụng CarrierInstaller

                    public class MyApplication extends Application {
                        @Override
                        public void onCreate() {
                            super.onCreate();
                            try {
                              // Google Play will install latest OpenSSL 
                              ProviderInstaller.installIfNeeded(getApplicationContext());
                              SSLContext sslContext;
                              sslContext = SSLContext.getInstance("TLSv1.2");
                              sslContext.init(null, null, null);
                              sslContext.createSSLEngine();
                            } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException
                                | NoSuchAlgorithmException | KeyManagementException e) {
                                e.printStackTrace();
                            }
                        }
                    }

### Timeouts 
- Sử dụng timeout để ko thực hiện request khi ko thể truy cập đc, phân vung mạng có thể là do sự cố kết nối với cline hay server hay cái gì đó...

                    private final OkHttpClient client;

                      public ConfigureTimeouts() throws Exception {
                        client = new OkHttpClient.Builder()
                            .connectTimeout(10, TimeUnit.SECONDS)
                            .writeTimeout(10, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .build();
                      }

                      public void run() throws Exception {
                        Request request = new Request.Builder()
                            .url("http://httpbin.org/delay/2") // This URL is served with a 2 second delay.
                            .build();

                        try (Response response = client.newCall(request).execute()) {
                          System.out.println("Response completed: " + response);
                        }
                      }
                      
  ### Handling authentication
  - OKHttp có thể tự dộng thử lại các request ko đc xác thực. Khi một response là 401 Not Authorized, 1 Authenticator đc yêu cầu cung cấp thông tin đăng nhập, việc triển khai nên đc xây dựng một request mới bao gồm các thông tin còn thiếu. Nếu không có thông tin xác thực return null để thử lại 
  - Sử dụng Response.challenge để nhận các schemes và realms của bất kỳ authentication challenges. 
  
                     private final OkHttpClient client;

                      public Authenticate() {
                        client = new OkHttpClient.Builder()
                            .authenticator(new Authenticator() {
                              @Override public Request authenticate(Route route, Response response) throws IOException {
                                if (response.request().header("Authorization") != null) {
                                  return null; // Give up, we've already attempted to authenticate.
                                }

                                System.out.println("Authenticating for response: " + response);
                                System.out.println("Challenges: " + response.challenges());
                                String credential = Credentials.basic("jesse", "password1");
                                return response.request().newBuilder()
                                    .header("Authorization", credential)
                                    .build();
                              }
                            })
                            .build();
                      }

                      public void run() throws Exception {
                        Request request = new Request.Builder()
                            .url("http://publicobject.com/secrets/hellosecret.txt")
                            .build();

                        try (Response response = client.newCall(request).execute()) {
                          if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                          System.out.println(response.body().string());
                        }
                      }
