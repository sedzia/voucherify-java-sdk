Voucherify Java SDK
===================

###Version: 2.6.0
[Voucherify](http://voucherify.io?utm_source=inbound&utm_medium=github&utm_campaign=voucherify-java-sdk) has a new platform that will help your team automate voucher campaigns. It does this by providing composable API and the marketer-friendly interface that increases teams' productivity:

- **roll-out thousands** of vouchers **in minutes** instead of weeks,
- **check status** or disable **every single** promo code in real time, 
- **track redemption** history and build reports on the fly.

Here you can find a library that makes it easier to integrate your Java application with Voucherify.

You can browse the [javadoc](http://rspective.github.io/voucherify-java-sdk/apidocs/index.html) for more information or 
go to the documentation located at [voucherify.readme.io](https://voucherify.readme.io).

Setup
=====

Grab via Maven:
```xml
<dependency>
  <groupId>pl.rspective.voucherify.client</groupId>
  <artifactId>voucherify-java-sdk</artifactId>
  <version>2.6.0</version>
</dependency>
```
or via Gradle:
```groovy
compile 'pl.rspective.voucherify.client:voucherify-java-sdk:2.6.0'
```

NOTE:
The SDK requires at least Java 6.


### Default Client

The Voucherify SDK uses [Retrofit](http://square.github.io/retrofit/) under the hood as a REST client, which detects [OkHttp](http://square.github.io/okhttp/) in your classpath and uses it if it's available, otherwise falls back to the default `HttpURLConnection`.
If you want you can also specify a custom client to be used (see javadoc).

Authentication
==============

[Log-in](http://app.voucherify.io/#/login) to Voucherify web interface and obtain your Application Keys from [Configuration](https://app.voucherify.io/#/app/configuration):

![](https://www.filepicker.io/api/file/WKYkl2bSAWKHccEN9tEG)

Usage
=====

The `VoucherifyClient` manages all your interaction with the Voucherify API.

```java
VoucherifyClient client = new VoucherifyClient.Builder()
                                .setAppId("c70a6f00-cf91-4756-9df5-47628850002b")
                                .setAppToken("3266b9f8-e246-4f79-bdf0-833929b1380c")
                                .build();
```

Current list of features:
- fetch one voucher based on his code
- consume the voucher based on his code with consumer trackingId (optional, pass null if you want to disable trackingId)
- fetch voucher usage details based on his code

Every method has a corresponding asynchronous extension which can be accessed through the `async()` or 'rx()' method of the vouchers module.

Create a voucher
===

Use `Voucher.Builder` to define a voucher:

```java
DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        
Voucher voucher = new Voucher.Builder()
        .setPercentOff(10.0)
        .setStartDate(df.parse("2016-01-01"))
        .setExpirationDate(df.parse("2016-12-31"))
        .setCategory("API Test")
        .build();
```

Then send it to Voucherify:

```java        
try {
    Voucher createdVoucher = client.vouchers().createVoucher(voucher);
    System.out.println("Voucher created: " + createdVoucher.getCode());
} catch (RetrofitError e) {
    // handle errors
}
```


Fetch voucher details
===

```java
try {
    Voucher voucher = client.vouchers().fetchVoucher("Testing7fjWdr");
} catch (RetrofitError e) {
    // error
}
```

or asynchronously:

```java
client.vouchers().async().fetchVoucher("Testing7fjWdr", new VoucherifyCallback<Voucher>() {
    @Override
    public void onSuccess(Voucher result) {
    }

    @Override
    public void onFailure(RetrofitError error) {
    // error
  }
});
```

or using RxJava:

```java
client.vouchers()
        .rx()
        .fetchVoucher("Testing7fjWdr")
        .subscribeOn(Schedulers.io())
        .subscribe(new Action1<Voucher>() {
            @Override
            public void call(Voucher vouchers) {

            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
            }
        });
```

Disable a voucher
===

```java        
try {
    Voucher createdVoucher = client.vouchers().disableVoucher("JxiJaV");
} catch (RetrofitError e) {
    // handle errors
}
```

Enable a voucher
===

```java        
try {
    Voucher createdVoucher = client.vouchers().enableVoucher("JxiJaV");
} catch (RetrofitError e) {
    // handle errors
}
```

Fetch voucher redemption details
===
```java
try {
    VoucherRedemption voucherRedemption = client.vouchers().redemption("Testing7fjWdr");
} catch (RetrofitError e) {
    // error
}

client.vouchers().async().redemption("Testing7fjWdr", new VoucherifyCallback<VoucherRedemption>() {
    @Override
    public void onSuccess(VoucherRedemption voucherRedemption) {
    }
});


client.vouchers().rx().redemption("Testing7fjWdr")
    .subscribeOn(Schedulers.io())
    .subscribe(new Subscriber<VoucherRedemption>() {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable throwable) {
        }

        @Override
        public void onNext(VoucherRedemption voucherRedemption) {
        }
});

```

Redeem a voucher
===

Synchronously:

```java
try {
    VoucherRedemptionResult result = client.vouchers().redeem("Testing7fjWdr");
} catch (RetrofitError e) {
    // handle errors
}
```
or asynchronously

```java
client.vouchers().async().redeem("Testing7fjWdr", new VoucherifyCallback<Voucher>() {
    @Override
    public void onSuccess(VoucherRedemptionResult result) {
    }

    @Override
    public void onFailure(RetrofitError retrofitError) {
        super.onFailure(retrofitError);
    }
});
```

or using RxJava:

```java
client.vouchers().rx().redeem("Testing7fjWdr")
    .subscribeOn(Schedulers.io())
    .subscribe(new Subscriber<VoucherRedemptionResult>() {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable throwable) {
        }

        @Override
        public void onNext(VoucherRedemptionResult result) {
        }
    });
```

Customer tracking
===

Voucherify gives you an option to track customers actions. They can be used for analytics.

You can pass tracking id or a detailed customer profile in a second parameter to the `redeem` method.

Just tracking id:

```java
VoucherRedemptionResult result = client.vouchers().redeem("Testing7fjWdr", "alice.morgan");
```

Customer profile:
```java
  VoucherRedemptionResult result = client.vouchers().redeem("Testing7fjWdr", new VoucherRedemptionContext(
          new Customer.Builder()
                .setSourceId("alice.morgan")
                .setName("Alice Morgan")
                .setEmail("alice@morgan.com")
                .setDescription("")
                .addMetadata("locale", "en-GB")
                .addMetadata("shoeSize", 5)
                .addMetadata("favouriteBrands", new String[]{"Armani", "L'Autre Chose", "Vicini"})
                .build()));
```

Rollback a redemption
===

Your business logic may include a case when you need to undo a redemption. You can do it by calling `rollbackRedemption(String redemption_id, String tracking_id, String reason)` method.
This operation will create a rollback entry in voucher's `redemption.redemption_entries` and give 1 redemption back to the pool (decrease `redeemed_quantity` by 1).

Example:

```java
	VoucherRedemptionResult result = client.vouchers().rollbackRedemption("r_irOQWUTAjthQwnkn5JQM1V6N", "alice.morgan", "invalid_credit_card");
```


Fetch redemptions across all vouchers
===

Sometimes you may need to list redemptions of all your vouchers at once. You can do this with `listRedemptions` method.

It takes an optional [`RedemptionFilter`](http://rspective.github.io/voucherify-java-sdk/apidocs/pl/rspective/voucherify/client/model/RedemptionsFilter.html) as a parameter that enables you to narrow down the list of redemptions according to specified limits.
It the filter is not provided a default filter will be used - a 100 redemptions from current month. 
 
Example:

```java
    Date startOfToday = Date.from(ZonedDateTime.now().with(LocalTime.MIN).toInstant());
    RedemptionsFilter filterTodayFailed = RedemptionsFilter.filter()
                                .withStartDate(startOfToday)
                                .withResult(
                                    RedemptionResult.FAILURE_INACTIVE, 
                                    RedemptionResult.FAILURE_NOT_EXIST);
                
   List<RedemptionDetails> todaysredemptions = createLocalClient().vouchers().listRedemptions(filterTodayFailed);
```


List vouchers that match given filter
===

You can list vouchers that meet specific criteria - for example belong to a specific category.
Define the criteria using [`VouchersFilter`](http://rspective.github.io/voucherify-java-sdk/apidocs/pl/rspective/voucherify/client/model/VouchersFilter.html).
A default filter will be used even if you pass `null` which means that the result will be limited to 10 vouchers.

 
Example:

```java
    VouchersFilter filterTestCategory = VouchersFilter.filter().withCategory("Test");
                
    List<Voucher> testVouchers = createLocalClient().vouchers().listVouchers(filterTestCategory);
```

Utils
===

You can use `VoucherifyUtils` to calculate actual discount and price after discount.

- `BigDecimal calculatePrice(BigDecimal basePrice, Voucher voucher, BigDecimal unitPrice)`
- `BigDecimal calculateDiscount(BigDecimal basePrice, Voucher voucher, BigDecimal unitPrice)`

For example you can redeem a 10% off voucher and calculate the final price.

```java
BigDecimal initialPrice = new BigDecimal("25.00");
try {
    VoucherRedemptionResult result = client.vouchers().redeem("Testing7fjWdr", TRACKING_ID);
    BigDecimal finalPrice = VoucherifyUtils.calculatePrice(initialPrice, result.getVoucher());
    // finalPrice == 22.50
} catch (RetrofitError e) {
    // handle errors
}
```

Changelog
=========
- **2016-06-02** - `3.0.0` - New customer model.
- **2016-05-30** - `2.6.0` - New publish model.
- **2016-04-27** - `2.5.0` - Redemption rollback.

See more in [Changelog](CHANGELOG.md)
