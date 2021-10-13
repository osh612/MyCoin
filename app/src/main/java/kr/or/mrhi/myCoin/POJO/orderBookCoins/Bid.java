package kr.or.mrhi.myCoin.POJO.orderBookCoins;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Bid {
    @SerializedName("price")
    @Expose
    private String price;
    @SerializedName("quantity")
    @Expose
    private String quantity;

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}
