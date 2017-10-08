package io.voucherify.client.module;

import io.reactivex.Observable;
import io.voucherify.client.api.VoucherifyApi;
import io.voucherify.client.callback.VoucherifyCallback;
import io.voucherify.client.model.customer.Customer;
import io.voucherify.client.model.customer.response.CustomerResponse;
import io.voucherify.client.module.CustomersModule.ExtAsync;
import io.voucherify.client.module.CustomersModule.ExtRxJava;
import io.voucherify.client.utils.RxUtils;
import io.voucherify.client.utils.response.EmptyResponse;

import java.util.concurrent.Executor;

public final class CustomersModule extends AbsModule<ExtAsync, ExtRxJava> {

  public CustomersModule(VoucherifyApi api, Executor executor) {
    super(api, executor);
  }

  public CustomerResponse get(String customerId) {
    return api.getCustomerById(customerId);
  }

  public CustomerResponse create(Customer customer) {
    return api.createCustomer(customer);
  }

  public CustomerResponse update(Customer customer) {
    return api.updateCustomer(customer.getId(), customer);
  }

  public void delete(String customerId) {
    api.deleteCustomer(customerId);
  }

  @Override
  ExtAsync createAsyncExtension() {
    return new ExtAsync();
  }

  @Override
  ExtRxJava createRxJavaExtension() {
    return new ExtRxJava();
  }

  @Override
  public ExtAsync async() {
    return extAsync;
  }

  @Override
  public ExtRxJava rx() {
    return extRxJava;
  }

  public class ExtAsync extends AbsModule.Async {

    public void get(String customerId, VoucherifyCallback<CustomerResponse> callback) {
      RxUtils.subscribe(executor, rx().get(customerId), callback);
    }

    public void create(Customer customer, VoucherifyCallback<CustomerResponse> callback) {
      RxUtils.subscribe(executor, rx().create(customer), callback);
    }

    public void update(Customer customer, VoucherifyCallback<CustomerResponse> callback) {
      RxUtils.subscribe(executor, rx().update(customer), callback);
    }

    public void delete(String customerId, VoucherifyCallback<EmptyResponse> callback) {
      RxUtils.subscribe(executor, rx().delete(customerId), callback);
    }
  }

  public class ExtRxJava extends AbsModule.Rx {

    public Observable<CustomerResponse> get(final String customerId) {
      return RxUtils.defer(new RxUtils.DefFunc<CustomerResponse>() {

        @Override
        public CustomerResponse method() {
          return CustomersModule.this.get(customerId);
        }
      });
    }

    public Observable<CustomerResponse> create(final Customer customer) {
      return RxUtils.defer(new RxUtils.DefFunc<CustomerResponse>() {

        @Override
        public CustomerResponse method() {
          return CustomersModule.this.create(customer);
        }
      });
    }

    public Observable<CustomerResponse> update(final Customer customer) {
      return RxUtils.defer(new RxUtils.DefFunc<CustomerResponse>() {

        @Override
        public CustomerResponse method() {
          return CustomersModule.this.update(customer);
        }
      });
    }

    public Observable<EmptyResponse> delete(final String customerId) {
      return RxUtils.defer(new RxUtils.DefFunc<EmptyResponse>() {

        @Override
        public EmptyResponse method() {
          CustomersModule.this.delete(customerId);
          return EmptyResponse.create();
        }
      });
    }
  }
}
