package io.voucherify.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.voucherify.client.api.VoucherifyApi;
import io.voucherify.client.error.ErrorInterceptor;
import io.voucherify.client.json.deserializer.CampaignsResponseDeserializer;
import io.voucherify.client.json.deserializer.DateDeserializer;
import io.voucherify.client.json.deserializer.VouchersResponseDeserializer;
import io.voucherify.client.json.serializer.DateSerializer;
import io.voucherify.client.model.campaign.response.CampaignsResponse;
import io.voucherify.client.model.voucher.response.VouchersResponse;
import io.voucherify.client.module.CampaignsModule;
import io.voucherify.client.module.CustomersModule;
import io.voucherify.client.module.DistributionsModule;
import io.voucherify.client.module.ProductsModule;
import io.voucherify.client.module.RedemptionsModule;
import io.voucherify.client.module.SegmentsModule;
import io.voucherify.client.module.ValidationRulesModule;
import io.voucherify.client.module.ValidationsModule;
import io.voucherify.client.module.VoucherModule;
import io.voucherify.client.utils.Platform;
import io.voucherify.client.utils.adapter.sync.SyncCallAdapterFactory;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.Date;
import java.util.concurrent.Executor;

public class VoucherifyClient {

  private final String httpScheme;

  private VoucherModule voucherModule;

  private ValidationsModule validationsModule;

  private CustomersModule customersModule;

  private CampaignsModule campaignsModule;

  private RedemptionsModule redemptionsModule;

  private DistributionsModule distributionsModule;

  private ProductsModule productsModule;

  private SegmentsModule segmentsModule;

  private ValidationRulesModule validationRulesModule;

  private VoucherifyApi voucherifyApi;

  private Executor executor;

  private VoucherifyClient(Builder builder) {
    if (builder.clientSecretKey == null) {
      throw new IllegalArgumentException("App token must be defined.");
    }

    if (builder.appId == null) {
      throw new IllegalArgumentException("App ID must be defined.");
    }

    this.httpScheme = createHttpScheme(builder);
    this.executor = createCallbackExecutor();

    this.voucherifyApi = createRetrofitService(builder);

    this.voucherModule = new VoucherModule(voucherifyApi, executor);
    this.validationsModule = new ValidationsModule(voucherifyApi, executor);
    this.customersModule = new CustomersModule(voucherifyApi, executor);
    this.campaignsModule = new CampaignsModule(voucherifyApi, executor);
    this.redemptionsModule = new RedemptionsModule(voucherifyApi, executor);
    this.distributionsModule = new DistributionsModule(voucherifyApi, executor);
    this.productsModule = new ProductsModule(voucherifyApi, executor);
    this.segmentsModule = new SegmentsModule(voucherifyApi, executor);
    this.validationRulesModule = new ValidationRulesModule(voucherifyApi, executor);
  }

  public VoucherModule vouchers() {
    return voucherModule;
  }

  public ValidationsModule validations() {
    return validationsModule;
  }

  public CustomersModule customers() {
    return customersModule;
  }

  public CampaignsModule campaigns() {
    return campaignsModule;
  }

  public RedemptionsModule redemptions() {
    return redemptionsModule;
  }

  public DistributionsModule distributions() {
    return distributionsModule;
  }

  public ProductsModule products() {
    return productsModule;
  }

  public SegmentsModule segments() {
    return segmentsModule;
  }

  public ValidationRulesModule validationRules() {
    return validationRulesModule;
  }

  private Executor createCallbackExecutor() {
    return Platform.get().callbackExecutor();
  }

  private String createHttpScheme(Builder builder) {
    if (builder.secure) {
      return Constants.SCHEME_HTTPS;
    } else {
      return Constants.SCHEME_HTTP;
    }
  }

  private VoucherifyApi createRetrofitService(Builder builder) {
    Retrofit.Builder restBuilder = new Retrofit.Builder()
        .addCallAdapterFactory(new SyncCallAdapterFactory())
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .addConverterFactory(JacksonConverterFactory.create(createObjectMapper(builder)));

    setEndPoint(builder, restBuilder);
    setClient(builder, restBuilder);

    return restBuilder.build().create(VoucherifyApi.class);
  }

  private void setClient(Builder builder, Retrofit.Builder retrofitBuilder) {
    if (builder.client != null) {
      retrofitBuilder.client(builder.client);
    } else {
      retrofitBuilder.client(createOkHttpClient(builder));
    }
  }

  private OkHttpClient createOkHttpClient(Builder builder) {
    OkHttpClient.Builder okBuilder = new OkHttpClient.Builder();

    okBuilder.addInterceptor(createHeadersInterceptor(builder));
    okBuilder.addInterceptor(createLoggingInterceptor(builder));
    okBuilder.addInterceptor(new ErrorInterceptor());

    return okBuilder.build();
  }

  private ObjectMapper createObjectMapper(Builder builder) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    SimpleModule jsonParsingModule = new SimpleModule();
    jsonParsingModule.addSerializer(Date.class, new DateSerializer(Constants.ENDPOINT_DATE_FORMAT));
    jsonParsingModule.addDeserializer(Date.class, new DateDeserializer(Constants.ENDPOINT_DATE_FORMAT, Constants.ENDPOINT_SECONDARY_DATE_FORMAT));
    jsonParsingModule.addDeserializer(CampaignsResponse.class, new CampaignsResponseDeserializer(builder.apiVersion));
    jsonParsingModule.addDeserializer(VouchersResponse.class, new VouchersResponseDeserializer(builder.apiVersion));
    mapper.registerModule(jsonParsingModule);
    return mapper;
  }

  private Interceptor createHeadersInterceptor(final Builder builder) {
    return chain -> {
      Request request = chain.request();
      Headers.Builder headersBuilder = request.headers().newBuilder()
          .add(Constants.HTTP_HEADER_VOUCHERIFY_CHANNEL, Constants.VOUCHERIFY_CHANNEL_NAME)
          .add(Constants.HTTP_HEADER_APP_ID, builder.appId)
          .add(Constants.HTTP_HEADER_APP_TOKEN, builder.clientSecretKey);

      if (builder.apiVersion != null) {
        headersBuilder.add(Constants.HTTP_HEADER_VOUCHERIFY_API_VERSION, builder.apiVersion.getValue());
      }

      return chain.proceed(request.newBuilder().headers(headersBuilder.build()).build());
    };
  }

  private HttpLoggingInterceptor createLoggingInterceptor(final Builder builder) {
    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
    if (builder.logLevel != null) {
      logging.setLevel(builder.logLevel.toHttpLogLevel());
    } else {
      logging.setLevel(HttpLoggingInterceptor.Level.NONE);
    }
    return logging;
  }

  private void setEndPoint(Builder builder, Retrofit.Builder retrofitBuilder) {
    String endpoint;

    if (builder.endpoint == null) {
      endpoint = Constants.ENDPOINT_VOUCHERIFY;
    } else {
      endpoint = builder.endpoint;
    }

    retrofitBuilder.baseUrl(String.format("%s://%s", httpScheme, endpoint));
  }

  public static class Builder {

    String clientSecretKey;

    String appId;

    String endpoint;

    boolean secure;

    LogLevel logLevel;

    OkHttpClient client;

    ApiVersion apiVersion;

    public Builder() {
      this.secure = true;
    }

    public Builder setClientSecretKey(String clientSecretKey) {
      if (clientSecretKey == null) {
        throw new IllegalArgumentException("Cannot call setClientSecretKey() with null.");
      }

      this.clientSecretKey = clientSecretKey;
      return this;
    }

    public Builder setAppId(String appId) {
      if (appId == null) {
        throw new IllegalArgumentException("Cannot call setAppId() with null.");
      }

      this.appId = appId;
      return this;
    }

    public Builder setClient(final OkHttpClient client) {
      if (client == null) {
        throw new IllegalArgumentException("Cannot call setClient() with null.");
      }
      this.client = client;
      return this;
    }

    public Builder setEndpoint(String remoteUrl) {
      if (remoteUrl == null) {
        throw new IllegalArgumentException("Cannot call setEndpoint() with null.");
      }

      this.endpoint = remoteUrl;
      System.out.println(remoteUrl);
      return this;
    }

    public Builder setLogLevel(LogLevel logLevel) {
      if (logLevel == null) {
        throw new IllegalArgumentException("Cannot call setLogLevel() with null.");
      }

      this.logLevel = logLevel;
      return this;
    }

    public Builder withSSL() {
      this.secure = true;
      return this;
    }

    public Builder withoutSSL() {
      this.secure = false;
      return this;
    }

    public Builder apiVersion(ApiVersion version) {
      this.apiVersion = version;
      return this;
    }

    public VoucherifyClient build() {
      return new VoucherifyClient(this);
    }

  }
}
