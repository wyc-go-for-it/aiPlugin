package com.wyc.ai;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.dialog.aiScale
 * @ClassName: AiGoods
 * @Description: Ai称商品
 * @Author: wyc
 * @CreateDate: 2023-06-30 13:50
 * @UpdateUser: 更新者：
 * @UpdateDate: 2023-06-30 13:50
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public final class AiGoods implements Parcelable {
    private String id;
    private String name;
    private String barcode;
    private double num;
    private double price;


    public AiGoods(){

    }

    public AiGoods(@NonNull String id , String name){
        this.id = id;
        this.name = name;
    }

    protected AiGoods(Parcel in) {
        id = in.readString();
        name = in.readString();
        barcode = in.readString();
        num = in.readDouble();
        price = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(barcode);
        dest.writeDouble(num);
        dest.writeDouble(price);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AiGoods> CREATOR = new Creator<AiGoods>() {
        @Override
        public AiGoods createFromParcel(Parcel in) {
            return new AiGoods(in);
        }

        @Override
        public AiGoods[] newArray(int size) {
            return new AiGoods[size];
        }
    };

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public double getNum() {
        return num;
    }

    public void setNum(double num) {
        this.num = num;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AiGoods aiGoods = (AiGoods) o;
        return Objects.equals(id, aiGoods.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AiGoods{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", barcode='" + barcode + '\'' +
                ", num=" + num +
                ", price=" + price +
                '}';
    }
}
