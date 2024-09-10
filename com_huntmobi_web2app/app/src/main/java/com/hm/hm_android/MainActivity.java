package com.hm.hm_android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.huntmobi.web2app.hm;

import org.json.JSONArray;
import org.json.JSONException;

public class MainActivity extends AppCompatActivity {
    private static final String HM_SharedPreferences_Info = "HM_SharedPreferences_Info";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // S2S时需要注册这个block，非S2S可以不加
        hm.UpdateW2aDataEvent((adv_data, HM_W2a_Data) -> {
            // 接收到W2A并发送到后台
            Log.d("", "onCreate: " + HM_W2a_Data);
        });
        // 关联设备或者用户的唯一ID，可用于投放后与BI数据对齐。（当游客账号与正式账号无绑定关系时，不要传游客ID，避免造成后期数据对不齐的情况）
        hm.SetDeviceID(getApplication(), "IDFV||UserID||GuestID");

        // 判断App是首次安装并且是首次启动时，isNewUser=true; App版本更新和正常启动isNewUser=false。注意App从不包含HMSDK的版本升级到HMSDK的版本时，isNewUser=false
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences(HM_SharedPreferences_Info, Context.MODE_PRIVATE);
        String isFirstInsert = sharedPreferences.getString("initHMB", "1");
        boolean isNewUser = !"0".equals(isFirstInsert);
        if (isNewUser) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("initHMB", "0");
            editor.apply();
        }

        // 安装事件，回调为数组，数组内的数据为与落地页协定的deeplink数据。
        // SDK网关地址"https://wa.bi4sight.com"。完成注册事件名为BI_CompleteRegistration。AppName需要传小写的。
        hm.Init(getApplication(),
                "https://cdn.bi4sight.com",
                "BI_CompleteRegistration",
                isNewUser,
                "test",
                new hm.InitCallback() {
                    @Override
                    public void onSuccess(JSONArray list) {
                        // 在初始化完成后执行的操作
                        if (list != null && list.length() > 0) {
                            // 跳转ID对应详情页 String idStr = list[1]
                            Log.d("InitCallback", "onSuccess: " + list.toString());
//                            try {
//                                String a = list.getString(1);
//                            } catch (JSONException e) {
//                                throw new RuntimeException(e);
//                            }
                        } else {
                            Log.e("InitCallback", "onSuccess: JSONArray list is null");
                        }

                    }
                });

        sendEvent();
    }

    public void sendEvent() {
        /*
            当用户看完落地页对应的剧或小说时，上报这个事件

            eventID : 事件ID，需要确保唯一性，web2app的后台会做幂等性处理；可传空字符串，SDK默认会生成GUID上报。
            event_name : 固定事件名BI_AddToWishlist
            currency : 货币单位，使用国际标准货币代码，如：USD，代表美元；INR 代表印度卢比
            value : 货币价值，使用浮点小数；如果传入非数字将强制默认为0
            typeStr : 单集单章单个商品可传“product”，多个可传"product_group"
            idsSt : 单集单章单商品可传对应ID，多个可用英文逗号分隔传入一个完整字符串);
        */
            hm.EventPost("",
                    "BI_AddToWishlist",
                    "",
                    "",
                    "product_group",
                    "vid000001,vid000002,vid000003");


        /*
        当用户点击付款按钮时触发

        eventID : 事件ID，需要确保唯一性，web2app的后台会做幂等性处理；可传空字符串，SDK默认会生成GUID上报。
        event_name : 固定事件名BI_InitiateCheckout
        currency : 货币单位，使用国际标准货币代码，如：USD，代表美元；INR 代表印度卢比
        value : 货币价值，使用浮点小数；如果传入非数字将强制默认为0
        typeStr : 单集单章单个商品可传“product”，多个可传"product_group"
        idsSt : 单集单章单商品可传对应ID，多个可用英文逗号分隔传入一个完整字符串);
        */
            hm.EventPost("",
                    "BI_InitiateCheckout",
                    "USD",
                    "9.99",
                    "product_group",
                    "p111111,p222222,p333333");


        /*
        用户支付完成后上报这个事件

        event_name : 固定事件名BI_Purchase
        currency : 货币单位，使用国际标准货币代码，如：USD，代表美元；INR 代表印度卢比
        value : 货币价值，使用浮点小数；如果传入非数字将强制默认为0
        typeStr : 单集单章单个商品可传“product”，多个可传"product_group"
        idsSt : 单集单章单商品可传对应ID，多个可用英文逗号分隔传入一个完整字符串);
        po_id : 三方支付返回的付费订单Id, 服务器根据该ID标记唯一信息，过期时间48小时；48小时内同样的ID 不再处理；强约束不考虑其他，如果为空或空字符串时候，忽略去重
        */
            hm.Purchase("BI_Purchase",
                    "USD",
                    "9.99",
                    "product_group",
                    "p111111,p222222,p333333",
                    "poid");


        /*
        当用户进入商品详情页时，上报这个事件

        event_name : 固定事件名BI_ProductView
        currency : 货币单位，使用国际标准货币代码，如：USD，代表美元；INR 代表印度卢比,可传空字符串
        value : 货币价值，使用浮点小数；如果传入非数字将强制默认为0,可传空字符串
        typeStr : 可传“product”
        idsSt : 商品对应ID
        */
            hm.EventPost("",
                    "BI_ProductView",
                    "USD",
                    "999.99",
                    "product",
                    "pid000001");

        /*
        当用户订阅某套餐或项目时，上报这个事件

        event_name : 固定事件名BI_Subscribe
        currency : 货币单位，使用国际标准货币代码，如：USD，代表美元；INR 代表印度卢比
        value : 货币价值，使用浮点小数；如果传入非数字将强制默认为0
        typeStr : 可传“product”
        idsSt : 可传对应ID
        */
            hm.EventPost("",
                    "BI_Subscribe",
                    "USD",
                    "999.99",
                    "product",
                    "pid000001");



            // 当用户数据发生变化时调用上报用户信息接口，供FB广告投放学习
            // 最优方案是通过FBSDK获取到以下相应的数据
            // 没有可传空字符串
        /*
        修改用户信息
        邮编：使用小写字母，且不可包含空格和破折号。美国邮编只限使用前 5 位数。英国邮编请使用邮域 + 邮区 + 邮政部门格式。
        城市： 小写字母（移除所有空格） 推荐使用罗马字母字符 a 至 z。仅限小写字母，且不可包含标点符号、特殊字符和空格。若使用特殊字符，则须按 UTF-8 格式对文本进行编码。
        州或省：以两个小写字母表示的州或省代码 使用 2 个字符的 ANSI 缩写代码 必须为小写字母。请使用小写字母对美国境外的州/省/自治区/直辖市名称作标准化处理，且不可包含标点符号、特殊字符和空格。
        性别： f 表示女性 m 表示男性
        名字： 不包含姓氏 推荐使用罗马字母字符 a 至 z。仅限小写字母，且不可包含标点符号。若使用特殊字符，则须按 UTF-8 格式对文本进行编码。
        姓氏 ：不包含名字 推荐使用罗马字母字符 a 至 z。仅限小写字母，且不可包含标点符号。若使用特殊字符，则须按 UTF-8 格式对文本进行编码。
        出生年月： 输入：2/16/1997 标准化格式：19970216 格式规则 YYYYMMDD
        国家： 请按照 ISO 3166-1 二位字母代码表示方式使用小写二位字母国家/地区代码。 输入：United States 准化格式：us
        */
//            hm.UserDataUpdateEvent("获取到用户的邮箱",
//                    "用户使用登录Facebook时拿到的userid(没有可传空字符串)",
//                    "获取到用户的电话",
//                    "获取到用户的邮编",
//                    "获取到用户的城市",
//                    "获取到用户的省",
//                    "获取到用户的性别",
//                    "获取到用户的first name",
//                    "获取到用户的last name",
//                    "获取到用户的生日",
//                    "获取到用户的国家",
//                    new hm.UserDataUpdateCallback() {
//                        @Override
//                        public void onSuccess() {
//                            // 更新用户信息之后，在UserDataUpdateEvent的回调中，固定执行添加支付信息事件,事件名为BI_AddPaymentInfo，其余参数可传空
//                            hm.EventPost("", "BI_AddPaymentInfo", "", "", "", "");
//                        }
//                    });
        hm.UserDataUpdateEvent("123@321.com", "", "", "", "", "", "", "", "", "", "", new hm.UserDataUpdateCallback() {
            @Override
            public void onSuccess() {
                hm.EventPost("", "BI_AddPaymentInfo", "", "", "", "");
            }
        });


    }

}