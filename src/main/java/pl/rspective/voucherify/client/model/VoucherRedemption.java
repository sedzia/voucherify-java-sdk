package pl.rspective.voucherify.client.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Represents voucher redemption
 */
public class VoucherRedemption {

	/**
	 * Value of voucher's quantity
	 */
	private Integer quantity;

	/**
	 * Value of redeemed voucher's quantity
	 */
	@SerializedName("redeemed_quantity")
	private int redeemedQuantity;
	
	/**
	 *
	 */
	@SerializedName("redemption_entries")
	private List<RedemptionEntry> redemptionEntries;

	
	public static VoucherRedemption quantity(int limit) {
	    VoucherRedemption redemption = new VoucherRedemption();
	    redemption.quantity = limit;
	    return redemption;
	}
	
    public static VoucherRedemption unlimited() {
        return new VoucherRedemption();
    }
	
	/**
	 *
	 * @return
	 */
	public Integer getQuantity() {
		return quantity;
	}

	/**
	 *
	 * @return
	 */
	public int getRedeemedQuantity() {
		return redeemedQuantity;
	}

	/**
	 *
	 * @return
	 */
	public List<RedemptionEntry> getRedemptionEntries() {
		return redemptionEntries;
	}

}