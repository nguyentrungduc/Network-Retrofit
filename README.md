# Network-Retrofit
## Restful API
RESTful API là một tiêu chuẩn dùng trong việc thết kế các thiết kế API cho các ứng dụng web để quản lý các resource. RESTful là một trong những kiểu thiết kế API được sử dụng phổ biến nhất ngày nay.
Trọng tâm của REST quy định cách sử dụng các HTTP method (như GET, POST, PUT, DELETE...) và cách định dạng các URL cho ứng dụng web để quản các resource

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

#### Interceptors
- Interceptor là một cơ chế mạnh mẽ để giám sát, viết lại và thử các request, đây là công cụ đơn giản ghi lại request gửi đi và response đến

                    class LoggingInterceptor implements Interceptor {
                      @Override public Response intercept(Interceptor.Chain chain) throws IOException {
                        Request request = chain.request();

                        long t1 = System.nanoTime();
                        logger.info(String.format("Sending request %s on %s%n%s",
                            request.url(), chain.connection(), request.headers()));

                        Response response = chain.proceed(request);

                        long t2 = System.nanoTime();
                        logger.info(String.format("Received response for %s in %.1fms%n%s",
                            response.request().url(), (t2 - t1) / 1e6d, response.headers()));

                        return response;
                      }
                    }
                    
- Một cuộc gọi đến chain.proceed(request) là một phần quan trọng trong mỗi lần thực hiện interceptor. Phương thức tìm kiếm đơn giản này là nơi tất cả các công việc HTTP sảy ra, tạo response để đáp ứng request
- Interceptor can be chained. Giả sử ta có cả compressing interceptor và checksumming interceptor, ta sẽ cần phải quyết định xem dữ liệu có đc nén và sau đó kiểm tra lại hay kiểm tra sau đó đc nén. OKHttp sử dụng danh sách để theo dõi các thiết bị chặn và các thiết bị chặn đc gọi theo thứ tự


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
                    
- Khi gọi request: 
                    
                Request request = new Request.Builder()
              .url("http://www.publicobject.com/helloworld.txt")
              .header("User-Agent", "OkHttp Example")
              .build();

          Response response = client.newCall(request).execute();
          response.body().close();    

- url http://www.publicobject.com/helloworld.txt sẽ đc direct sang https://publicobject.com/helloworld.txt, và Interceptor sẽ đc gọi và response trả về từ chain.proceed() có response chuyển hướng :

          INFO: Sending request http://www.publicobject.com/helloworld.txt on null
          User-Agent: OkHttp Example

          INFO: Received response for https://publicobject.com/helloworld.txt in 1179.7ms
          Server: nginx/1.4.6 (Ubuntu)
          Content-Type: text/plain
          Content-Length: 1759
          Connection: keep-alive 
          
- Interceptors có thể add, remove, replace request header. Ta cũng có thể transform body của request. Ví dụ, bạn có thể sử dụng 1 application interceptor  để add request body      
                    
                    /** This interceptor compresses the HTTP request body. Many webservers can't handle this! */
                    final class GzipRequestInterceptor implements Interceptor {
                      @Override public Response intercept(Interceptor.Chain chain) throws IOException {
                        Request originalRequest = chain.request();
                        if (originalRequest.body() == null || originalRequest.header("Content-Encoding") != null) {
                          return chain.proceed(originalRequest);
                        }

                        Request compressedRequest = originalRequest.newBuilder()
                            .header("Content-Encoding", "gzip")
                            .method(originalRequest.method(), gzip(originalRequest.body()))
                            .build();
                        return chain.proceed(compressedRequest);
                      }

                      private RequestBody gzip(final RequestBody body) {
                        return new RequestBody() {
                          @Override public MediaType contentType() {
                            return body.contentType();
                          }

                          @Override public long contentLength() {
                            return -1; // We don't know the compressed length in advance!
                          }

                          @Override public void writeTo(BufferedSink sink) throws IOException {
                            BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
                            body.writeTo(gzipSink);
                            gzipSink.close();
                          }
                        };
                      }
                    }
          
                    
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
                      
  ### SSL Configuration
  - Theo 
  
## Retrofit
- Retrofit là một Rest Client cho Android và Java và được tạo ra bởi Square. Retrofit giúp dễ dàng kết nối đến một dịch vụ REST trên web bằng cách chyển đổi API thành Java Interface. 
- Thư viện mạnh mẽ này giúp bạn dễ dàng xử lý dữ liệu JSON hoặc XML sau đó phân tích cú pháp thành Plain Old Java Objects (POJOs). Tất cả các yêu cầu GET, POST, PUT, PATCH, và DELETE đều có thể được thực thi.
- Giống như hầu hết các phần mềm mã nguồn mở khác, Retrofit được xây dựng dựa trên một số thư viện mạnh mẽ và công cụ khác. Đằng sau nó, Retrofit làm cho việc sử dụng OkHttp để xử lý các yêu cầu trên mạng. Ngoài ra, từ Retrofit2 không tích hợp bất kỳ một bộ chuyển đổi JSON nào để phân tích từ JSON thành các đối tượng Java. Thay vào đó nó đi kèm với các thư viện chuyển đổi JSON sau đây để xử lý điều đó:
- Gson: com.squareup.retrofit:converter-gson
- Jackson: com.squareup.retrofit:converter-jackson
- Moshi: com.squareup.retrofit:converter-moshi
## Guide
- Sử dụng Annotations để mô tả yêu cầu HTTP:
= Hỗ trợ tham số URL và tham số truy vấn
- Chuyển đổi đối tượng để yêu cầu nội dung
- Multipart request body và file upload

                    public interface GitHubService {
                      @GET("users/{user}/repos")
                      Call<List<Repo>> listRepos(@Path("user") String user);
                    }
### Request Method
- Mỗi phương thức phải có Annotation HTTP cung cấp request method và URL. Có năm Annotation được tích hợp sẵn: @GET, @POST, @PUT, @DELETE và @HEAD URL tương đối của tài nguyên được chỉ định trong Annotation.

- @GET("users/list")

- Bạn cũng có thể chỉ định tham số truy vấn trong URL.

                    @GET("users/list?sort=desc")

### URL MANIPULATION
- URL request có thể được cập nhật tự động bằng cách sử dụng các khối thay thế và tham số trên phương thức.
- Chúng ta có thể sử dụng URL 1 cách động dựa vào biến truyền vào, bằng cách sử dụng anotation @Path

                    @GET("group/{id}/users")
                    Call<List<User>> groupList(@Path("id") int groupId);

- Chúng ta có thể nối thêm paramater vào sau URL bằng cách sử dụng @Query
                    
                    @GET("group/{id}/users")
                    Call<List<User>> groupList(@Path("id") int groupId, @Query("sort") String sort);
                    
- Đối với các kết hợp tham số truy vấn phức tạp, có thể sử dụng @QueryMap.
                    
                    @GET("group/{id}/users")
                    Call<List<User>> groupList(@Path("id") int groupId, @QueryMap Map<String, String> options);
                    
### Request Body
- Một đối tượng có thể được chỉ định để sử dụng làm phần thân yêu cầu HTTP với Annotation @Body.

                    @POST("users/new")
                    Call<User> createUser(@Body User user);
                    
- Đối tượng cũng sẽ được chuyển đổi bằng cách sử dụng Converter được chỉ định trên instance của Retrofit. Nếu không có Converter nào được thêm vào, chỉ có thể sử dụng RequestBody.

### FORM ENCODED AND MULTIPART 
- Các phương thức cũng có thể được khai báo để gửi dữ liệu được mã hóa và dữ liệu multipart(nhiều phần). Dữ liệu được mã hóa theo form được gửi khi @FormUrlEncoded được chỉ định trên phương thức. Mỗi cặp key-value được chú thích bằng @Field chứa tên và đối tượng cung cấp giá trị.

                    @FormUrlEncoded
                    @POST("user/edit")
                    Call<User> updateUser(@Field("first_name") String first, @Field("last_name") String last);

- Các yêu cầu multipart được sử dụng khi @Multipart xuất hiện trên phương thức. Các phần được khai báo bằng cách sử dụng @Part

                    @Multipart
                    @PUT("user/photo")
                    Call<User> updateUser(@Part("photo") RequestBody photo, @Part("description") RequestBody description);
                    
- Các phần của multiparts sử dụng một trong các bộ chuyển đổi của Retrofit hoặc chúng có thể implement RequestBody để xử lý serialization của riêng chúng.



