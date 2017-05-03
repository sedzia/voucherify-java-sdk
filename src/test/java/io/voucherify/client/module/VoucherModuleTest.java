package io.voucherify.client.module;

import com.squareup.okhttp.mockwebserver.RecordedRequest;
import io.voucherify.client.callback.VoucherifyCallback;
import io.voucherify.client.model.voucher.CreateVoucher;
import io.voucherify.client.model.voucher.Discount;
import io.voucherify.client.model.voucher.Voucher;
import org.junit.Test;
import io.voucherify.client.model.voucher.VoucherUpdate;
import io.voucherify.client.model.voucher.VouchersFilter;
import io.voucherify.client.model.voucher.response.VoucherResponse;
import rx.Observable;

import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class VoucherModuleTest extends AbstractModuleTest {

  @Test
  public void shouldCreateVoucher() throws Exception {
    // given
    Voucher voucher = Voucher.builder().active(true).category("category")
            .campaign("my-campaign").isReferralCode(false)
            .discount(Discount.unitOff(10.0))
            .build();

    CreateVoucher createVoucher = CreateVoucher.builder().voucher(voucher).build();

    enqueueResponse(voucher);

    // when
    VoucherResponse result = client.vouchers().create(createVoucher);

    // then
    assertThat(result).isNotNull();
    RecordedRequest request = getRequest();
    assertThat(request.getPath()).isEqualTo("/vouchers");
    assertThat(request.getMethod()).isEqualTo("POST");
  }

  @Test
  public void shouldGetVoucher() throws Exception {
    // given
    Voucher voucher = Voucher.builder()
            .code("some-code")
            .active(true).category("category")
            .campaign("my-campaign").isReferralCode(false)
            .discount(Discount.unitOff(10.0))
            .build();

    enqueueResponse(voucher);

    // when
    VoucherResponse result = client.vouchers().get("some-code");

    // then
    assertThat(result).isNotNull();
    assertThat(result.getCode()).isEqualTo(voucher.getCode());
    RecordedRequest request = getRequest();
    assertThat(request.getPath()).isEqualTo("/vouchers/some-code");
    assertThat(request.getMethod()).isEqualTo("GET");
  }

  @Test
  public void shouldListVouchers() throws Exception {
    // given
    Voucher voucher = Voucher.builder()
            .code("some-code")
            .active(true).category("category")
            .campaign("my-campaign").isReferralCode(false)
            .discount(Discount.unitOff(10.0))
            .build();

    enqueueResponse("[" + mapper.writeValueAsString(voucher) + "]");

    VouchersFilter filter = VouchersFilter.builder()
            .limit(10)
            .page(5)
            .campaign("some-campaign")
            .category("some-category")
            .build();

    // when
    List<VoucherResponse> list = client.vouchers().list(filter);

    // then
    assertThat(list).isNotEmpty();
    RecordedRequest request = getRequest();
    assertThat(request.getPath()).isEqualTo("/vouchers?limit=10&campaign=some-campaign&page=5&category=some-category");
    assertThat(request.getMethod()).isEqualTo("GET");
  }

  @Test
  public void shouldUpdateVoucher() throws Exception {
    // given
    Voucher voucher = Voucher.builder()
            .code("some-code")
            .active(false).category("some-category")
            .campaign("my-campaign").isReferralCode(false)
            .discount(Discount.unitOff(10.0))
            .build();

    VoucherUpdate update = VoucherUpdate.builder()
            .active(false)
            .category("some-category")
            .build();

    enqueueResponse(voucher);

    // when
    VoucherResponse result = client.vouchers().update("some-code", update);

    // then
    assertThat(result.getCategory()).isEqualTo("some-category");
    assertThat(result.getActive()).isEqualTo(false);
    RecordedRequest request = getRequest();
    assertThat(request.getPath()).isEqualTo("/vouchers/some-code");
    assertThat(request.getMethod()).isEqualTo("PUT");
  }

  @Test
  public void shouldDisableVoucher() throws Exception {
    // given
    Voucher voucher = Voucher.builder()
            .code("some-code")
            .active(false).category("some-category")
            .campaign("my-campaign").isReferralCode(false)
            .discount(Discount.unitOff(10.0))
            .build();

    enqueueResponse(voucher);

    // when
    VoucherResponse result = client.vouchers().disable("some-code");

    // then
    assertThat(result.getActive()).isEqualTo(false);
    RecordedRequest request = getRequest();
    assertThat(request.getPath()).isEqualTo("/vouchers/some-code/disable");
    assertThat(request.getMethod()).isEqualTo("POST");
  }

  @Test
  public void shouldEnableVoucher() throws Exception {
    // given
    Voucher voucher = Voucher.builder()
            .code("some-code")
            .active(true).category("some-category")
            .campaign("my-campaign").isReferralCode(false)
            .discount(Discount.unitOff(10.0))
            .build();

    enqueueResponse(voucher);

    // when
    VoucherResponse result = client.vouchers().enable("some-code");

    // then
    assertThat(result.getActive()).isEqualTo(true);
    RecordedRequest request = getRequest();
    assertThat(request.getPath()).isEqualTo("/vouchers/some-code/enable");
    assertThat(request.getMethod()).isEqualTo("POST");
  }

  @Test
  public void shouldCreateVoucherAsync() throws Exception {
    // given
    Voucher voucher = Voucher.builder().active(true).category("category")
            .campaign("my-campaign").isReferralCode(false)
            .discount(Discount.unitOff(10.0))
            .build();

    CreateVoucher createVoucher = CreateVoucher.builder().voucher(voucher).build();

    enqueueResponse(voucher);

    VoucherifyCallback callback = createCallback();

    // when
    client.vouchers().async().create(createVoucher, callback);

    // then
    await().atMost(5, SECONDS).until(wasCallbackFired());
    RecordedRequest request = getRequest();
    assertThat(request.getPath()).isEqualTo("/vouchers");
    assertThat(request.getMethod()).isEqualTo("POST");
  }

  @Test
  public void shouldGetVoucherAsync() throws Exception {
    // given
    Voucher voucher = Voucher.builder()
            .code("some-code")
            .active(true).category("category")
            .campaign("my-campaign").isReferralCode(false)
            .discount(Discount.unitOff(10.0))
            .build();

    enqueueResponse(voucher);

    VoucherifyCallback callback = createCallback();

    // when
    client.vouchers().async().get("some-code", callback);

    // then
    await().atMost(5, SECONDS).until(wasCallbackFired());
    RecordedRequest request = getRequest();
    assertThat(request.getPath()).isEqualTo("/vouchers/some-code");
    assertThat(request.getMethod()).isEqualTo("GET");
  }

  @Test
  public void shouldListVouchersAsync() throws Exception {
    // given
    Voucher voucher = Voucher.builder()
            .code("some-code")
            .active(true).category("category")
            .campaign("my-campaign").isReferralCode(false)
            .discount(Discount.unitOff(10.0))
            .build();

    enqueueResponse("[" + mapper.writeValueAsString(voucher) + "]");

    VoucherifyCallback callback = createCallback();

    VouchersFilter filter = VouchersFilter.builder()
            .limit(10)
            .page(5)
            .campaign("some-campaign")
            .category("some-category")
            .build();

    // when
    client.vouchers().async().list(filter, callback);

    // then
    await().atMost(5, SECONDS).until(wasCallbackFired());
    RecordedRequest request = getRequest();
    assertThat(request.getPath()).isEqualTo("/vouchers?limit=10&campaign=some-campaign&page=5&category=some-category");
    assertThat(request.getMethod()).isEqualTo("GET");
  }

  @Test
  public void shouldUpdateVoucherAsync() throws Exception {
    // given
    Voucher voucher = Voucher.builder()
            .code("some-code")
            .active(false).category("some-category")
            .campaign("my-campaign").isReferralCode(false)
            .discount(Discount.unitOff(10.0))
            .build();

    VoucherUpdate update = VoucherUpdate.builder()
            .active(false)
            .category("some-category")
            .build();

    VoucherifyCallback callback = createCallback();

    enqueueResponse(voucher);

    // when
    client.vouchers().async().update("some-code", update, callback);

    // then
    await().atMost(5, SECONDS).until(wasCallbackFired());
    RecordedRequest request = getRequest();
    assertThat(request.getPath()).isEqualTo("/vouchers/some-code");
    assertThat(request.getMethod()).isEqualTo("PUT");
  }

  @Test
  public void shouldDisableVoucherAsync() throws Exception {
    // given
    Voucher voucher = Voucher.builder()
            .code("some-code")
            .active(false).category("some-category")
            .campaign("my-campaign").isReferralCode(false)
            .discount(Discount.unitOff(10.0))
            .build();

    VoucherifyCallback callback = createCallback();

    enqueueResponse(voucher);

    // when
    client.vouchers().async().disable("some-code", callback);

    // then
    await().atMost(5, SECONDS).until(wasCallbackFired());
    RecordedRequest request = getRequest();
    assertThat(request.getPath()).isEqualTo("/vouchers/some-code/disable");
    assertThat(request.getMethod()).isEqualTo("POST");
  }

  @Test
  public void shouldEnableVoucherAsync() throws Exception {
    // given
    Voucher voucher = Voucher.builder()
            .code("some-code")
            .active(true).category("some-category")
            .campaign("my-campaign").isReferralCode(false)
            .discount(Discount.unitOff(10.0))
            .build();

    VoucherifyCallback callback = createCallback();

    enqueueResponse(voucher);

    // when
    client.vouchers().async().enable("some-code", callback);

    // then
    await().atMost(5, SECONDS).until(wasCallbackFired());
    RecordedRequest request = getRequest();
    assertThat(request.getPath()).isEqualTo("/vouchers/some-code/enable");
    assertThat(request.getMethod()).isEqualTo("POST");
  }

  @Test
  public void shouldCreateVoucherRxJava() throws Exception {
    // given
    Voucher voucher = Voucher.builder().active(true).category("category")
            .campaign("my-campaign").isReferralCode(false)
            .discount(Discount.unitOff(10.0))
            .build();

    CreateVoucher createVoucher = CreateVoucher.builder().voucher(voucher).build();

    enqueueResponse(voucher);

    // when
    Observable<VoucherResponse> observable = client.vouchers().rx().create(createVoucher);

    // then
    VoucherResponse result = observable.toBlocking().first();
    assertThat(result).isNotNull();
    RecordedRequest request = getRequest();
    assertThat(request.getPath()).isEqualTo("/vouchers");
    assertThat(request.getMethod()).isEqualTo("POST");
  }

  @Test
  public void shouldGetVoucherRxJava() throws Exception {
    // given
    Voucher voucher = Voucher.builder()
            .code("some-code")
            .active(true).category("category")
            .campaign("my-campaign").isReferralCode(false)
            .discount(Discount.unitOff(10.0))
            .build();

    enqueueResponse(voucher);

    // when
    Observable<VoucherResponse> observable = client.vouchers().rx().get("some-code");

    // then
    VoucherResponse result = observable.toBlocking().first();
    assertThat(result).isNotNull();
    assertThat(result.getCode()).isEqualTo(voucher.getCode());
    RecordedRequest request = getRequest();
    assertThat(request.getPath()).isEqualTo("/vouchers/some-code");
    assertThat(request.getMethod()).isEqualTo("GET");
  }

  @Test
  public void shouldListVouchersRxJava() throws Exception {
    // given
    Voucher voucher = Voucher.builder()
            .code("some-code")
            .active(true).category("category")
            .campaign("my-campaign").isReferralCode(false)
            .discount(Discount.unitOff(10.0))
            .build();

    enqueueResponse("[" + mapper.writeValueAsString(voucher) + "]");

    VouchersFilter filter = VouchersFilter.builder()
            .limit(10)
            .page(5)
            .campaign("some-campaign")
            .category("some-category")
            .build();

    // when
    Observable<List<VoucherResponse>> observable = client.vouchers().rx().list(filter);

    // then
    List<VoucherResponse> result = observable.toBlocking().first();
    assertThat(result).isNotEmpty();
    RecordedRequest request = getRequest();
    assertThat(request.getPath()).isEqualTo("/vouchers?limit=10&campaign=some-campaign&page=5&category=some-category");
    assertThat(request.getMethod()).isEqualTo("GET");
  }

  @Test
  public void shouldUpdateVoucherRxJava() throws Exception {
    // given
    Voucher voucher = Voucher.builder()
            .code("some-code")
            .active(false).category("some-category")
            .campaign("my-campaign").isReferralCode(false)
            .discount(Discount.unitOff(10.0))
            .build();

    VoucherUpdate update = VoucherUpdate.builder()
            .active(false)
            .category("some-category")
            .build();

    enqueueResponse(voucher);

    // when
    Observable<VoucherResponse> observable = client.vouchers().rx().update("some-code", update);

    // then
    VoucherResponse result = observable.toBlocking().first();
    assertThat(result.getCategory()).isEqualTo("some-category");
    assertThat(result.getActive()).isEqualTo(false);
    RecordedRequest request = getRequest();
    assertThat(request.getPath()).isEqualTo("/vouchers/some-code");
    assertThat(request.getMethod()).isEqualTo("PUT");
  }

  @Test
  public void shouldDisableVoucherRxJava() throws Exception {
    // given
    Voucher voucher = Voucher.builder()
            .code("some-code")
            .active(false).category("some-category")
            .campaign("my-campaign").isReferralCode(false)
            .discount(Discount.unitOff(10.0))
            .build();

    enqueueResponse(voucher);

    // when
    Observable<VoucherResponse> observable = client.vouchers().rx().disable("some-code");

    // then
    VoucherResponse result = observable.toBlocking().first();
    assertThat(result.getActive()).isEqualTo(false);
    RecordedRequest request = getRequest();
    assertThat(request.getPath()).isEqualTo("/vouchers/some-code/disable");
    assertThat(request.getMethod()).isEqualTo("POST");
  }

  @Test
  public void shouldEnableVoucherRxJava() throws Exception {
    // given
    Voucher voucher = Voucher.builder()
            .code("some-code")
            .active(true).category("some-category")
            .campaign("my-campaign").isReferralCode(false)
            .discount(Discount.unitOff(10.0))
            .build();

    enqueueResponse(voucher);

    // when
    Observable<VoucherResponse> observable = client.vouchers().rx().enable("some-code");

    // then
    VoucherResponse result = observable.toBlocking().first();
    assertThat(result.getActive()).isEqualTo(true);
    RecordedRequest request = getRequest();
    assertThat(request.getPath()).isEqualTo("/vouchers/some-code/enable");
    assertThat(request.getMethod()).isEqualTo("POST");
  }
}