package com.huntmobi.w2a;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.huntmobi.web2app.HM_EventDataModel;
import com.huntmobi.web2app.HM_EventInfoModel;
import com.huntmobi.web2app.HM_UserInfoModel;
import com.huntmobi.web2app.HM_Web2App;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HM_Web2App web2AppInstance = HM_Web2App.getInstance(getApplication());// 实例对象
        //首次启动传空值，非首次启动传入安卓ID或是其他唯一标识，首次安装定义：手机安装后App第一次打开，卸载重装后第一次打开App均为首次安装；升级更新App后打开不算是首次安装
        web2AppInstance.deviceTrackID = "";
        //关联设备或者用户的唯一ID，可用于投放后与BI数据对齐。（当游客账号与正式账号无绑定关系时，不要传游客ID，避免造成后期数据对不齐的情况）
        web2AppInstance.Uid = "";
        // 调用方法初始化并归因，在前面的参数赋值完之后再执行
        web2AppInstance.attibuteWithAppname("test", new HM_Web2App.attibuteCallback() {
            @Override
            public void onSuccess(JSONObject data) {
                if (data == null) {
                    // 如果数据为空，直接返回或处理异常情况
                    return;
                }
                try {
                    // 落地页传输的deeplink，用落地页协定的方式去解析，跳转到对应的页面或执行特定的操作
                    JSONArray advDataArray = data.optJSONArray("adv_data");
                    // 唯一识别ID，可用于与BI数据对齐
                    String externalId = data.optString("external_id", "");
                    // 0: w2a用户，1: w2a 老用户(被再次追踪到)， 2 :非w2a 追踪用户
                    String userType = data.optString("user_type", "2");
                    // 归因状态：false-归因失败，true-归因成功
                    boolean isAttribution = data.optBoolean("isAttribution", false);
                    // 归因模式：字符串，如果是来自剪切板归因，该属性为："cut"；其余为服务器归因；isAttribution==false时为空字符串
                    String attributionType = data.optString("attribution_type", "");

                    assert advDataArray != null;
                    handleDeepLink(advDataArray, externalId, userType, isAttribution, attributionType);

                } catch (ClassCastException e) {
                    // 捕获可能的类型转换异常，避免程序崩溃
                    e.printStackTrace();
                }
            }

            private void handleDeepLink(JSONArray advData, String externalId,
                                        String userType, boolean isAttribution, String attributionType) {
                Log.d("HMLOG", "advData Array: " + advData.toString() + " externalId : " + externalId + " userType : " + userType + " isAttribution : " + isAttribution + " attributionType + " + attributionType);
                // 处理 deeplink 数据，跳转页面或执行特定操作
                // 在此进行业务逻辑处理，例如页面跳转或数据上传
                // 设置事件数据

            }
        });

        //当用户数据发生变化时调用上报用户信息接口，供FB广告投放学习
        //最优方案是通过FBSDK获取到以下相应的数据
        //没有可传空字符串，获取到任意值均可上报，可重复上报
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
        //email、fbLoginId、phone是重要字段，尽可能传入； 其余字段获取不到传空或不传，可以的话还是尽量传入
        HM_UserInfoModel userInfo = new HM_UserInfoModel(
                "abc@gmail.com",// 获取到用户的邮箱
                "fbid111222333",// 用户使用登录Facebook时拿到的userid(没有可传空字符串)
                "",//获取到用户的电话
                "",//获取到用户的国家
                "",//获取到用户的邮编
                "",//获取到用户的城市
                "",//获取到用户的省
                "",//获取到用户的性别
                "",//获取到用户的first name
                "",//获取到用户的last name
                "");//获取到用户的生日
        web2AppInstance.updateUserInfo(userInfo);

        /*
        当用户看完落地页对应的剧或小说时，上报这个事件
        poid : 付费订单Id，第三方支付返回的订单id、账单id或流水id等唯一标识，可通过该id与后台用户数据匹配上。Purchase事件必传，非付费事件可传空
        eventID : 事件ID，需要确保唯一性，web2app的后台会做幂等性处理；可传空字符串，SDK默认会生成GUID上报。
        event_name : 件名，推荐使用BI事件流对应的事件名称：BI_AddToWishlist
        currency : 货币单位，使用国际标准货币代码，如：USD，代表美元；INR 代表印度卢比
        value : 货币价值，使用浮点小数；如果传入非数字将强制默认为0
        contentType : 内容类型(商品、剧、小说、礼包、套餐等)，单个传product，传多个product_group
        contentIds : 内容编号，content_type=product时，Id数组只能传入一个Id，若是使用content_type=product_group时，Id可以传入多个
        */
        // 设置事件数据
        HM_EventDataModel eventData = new HM_EventDataModel(
                "",
                "",
                "BI_AddToWishlist",
                "",
                "",
                "product",
                Collections.singletonList("id1"));
        HM_EventInfoModel eventInfo = new HM_EventInfoModel(
                false,   //是否延迟上报，目前只有购物事件可以开启，如另有需要可以联系技术支持
                false,   //是否是关键事件，关键事件不上报媒体，只在BI统计
                eventData);   //事件具体内容
        //上报事件
        web2AppInstance.eventPostWithEventInfo(eventInfo);

        /*
        当用户点击加入购物车按钮时触发
        poid : 付费订单Id，第三方支付返回的订单id、账单id或流水id等唯一标识，可通过该id与后台用户数据匹配上。Purchase事件必传，非付费事件可传空
        eventID : 事件ID，需要确保唯一性，web2app的后台会做幂等性处理；可传空字符串，SDK默认会生成GUID上报。
        event_name : 事件名，推荐使用BI事件流对应的事件名称：BI_AddToCart
        currency : 货币单位，使用国际标准货币代码，如：USD，代表美元；INR 代表印度卢比
        value : 货币价值，使用浮点小数；如果传入非数字将强制默认为0
        contentType : 内容类型(商品、剧、小说、礼包、套餐等)，单个传product，传多个product_group
        contentIds : 内容编号，content_type=product时，Id数组只能传入一个Id，若是使用content_type=product_group时，Id可以传入多个
        */
        // 设置事件数据
        eventData = new HM_EventDataModel(
                "",
                "",
                "BI_AddToCart",
                "",
                "",
                "product_group",
                Arrays.asList("vid000001", "vid000002", "vid000003"));
        eventInfo = new HM_EventInfoModel(
                false,   //是否延迟上报，目前只有购物事件可以开启，如另有需要可以联系技术支持
                false,   //是否是关键事件，关键事件不上报媒体，只在BI统计
                eventData);   //事件具体内容
        //上报事件
        web2AppInstance.eventPostWithEventInfo(eventInfo);

        /*
        当用户点击付款按钮时触发
        poid : 付费订单Id，第三方支付返回的订单id、账单id或流水id等唯一标识，可通过该id与后台用户数据匹配上。Purchase事件必传，非付费事件可传空
        eventID : 事件ID，需要确保唯一性，web2app的后台会做幂等性处理；可传空字符串，SDK默认会生成GUID上报。
        event_name : 事件名，推荐使用BI事件流对应的事件名称：BI_InitiateCheckout
        currency : 货币单位，使用国际标准货币代码，如：USD，代表美元；INR 代表印度卢比
        value : 货币价值，使用浮点小数；如果传入非数字将强制默认为0
        contentType : 内容类型(商品、剧、小说、礼包、套餐等)，单个传product，传多个product_group
        contentIds : 内容编号，content_type=product时，Id数组只能传入一个Id，若是使用content_type=product_group时，Id可以传入多个
        */
        // 设置事件数据
        eventData = new HM_EventDataModel(
                "",
                "",
                "BI_InitiateCheckout",
                "USD",
                "9.99",
                "product_group",
                Arrays.asList("vid000001", "vid000002", "vid000003"));
        eventInfo = new HM_EventInfoModel(
                false,   //是否延迟上报，目前只有购物事件可以开启，如另有需要可以联系技术支持
                false,   //是否是关键事件，关键事件不上报媒体，只在BI统计
                eventData);   //事件具体内容
        //上报事件
        web2AppInstance.eventPostWithEventInfo(eventInfo);

        /*
        用户支付完成后上报这个事件
        poid : 付费订单Id，第三方支付返回的订单id、账单id或流水id等唯一标识，可通过该id与后台用户数据匹配上。Purchase事件必传，非付费事件可传空
        eventID : 事件ID，需要确保唯一性，web2app的后台会做幂等性处理；可传空字符串，SDK默认会生成GUID上报。
        event_name : 事件名，推荐使用BI事件流对应的事件名称：BI_Purchase
        currency : 货币单位，使用国际标准货币代码，如：USD，代表美元；INR 代表印度卢比
        value : 货币价值，使用浮点小数；如果传入非数字将强制默认为0
        contentType : 内容类型(商品、剧、小说、礼包、套餐等)，单个传product，传多个product_group
        contentIds : 内容编号，content_type=product时，Id数组只能传入一个Id，若是使用content_type=product_group时，Id可以传入多个
        */
        // 设置事件数据
        eventData = new HM_EventDataModel(
                "poid112233",
                "",
                "BI_Purchase",
                "USD",
                "9.99",
                "product_group",
                Arrays.asList("vid000001", "vid000002", "vid000003"));
        eventInfo = new HM_EventInfoModel(
                true,   //是否延迟上报，目前只有购物事件可以开启，如另有需要可以联系技术支持
                false,   //是否是关键事件，关键事件不上报媒体，只在BI统计
                eventData);   //事件具体内容
        //上报事件
        web2AppInstance.eventPostWithEventInfo(eventInfo);

        /*
        当用户进入商品详情页时，上报这个事件
        poid : 付费订单Id，第三方支付返回的订单id、账单id或流水id等唯一标识，可通过该id与后台用户数据匹配上。Purchase事件必传，非付费事件可传空
        eventID : 事件ID，需要确保唯一性，web2app的后台会做幂等性处理；可传空字符串，SDK默认会生成GUID上报。
        event_name : 事件名，推荐使用BI事件流对应的事件名称：BI_ProductView
        currency : 货币单位，使用国际标准货币代码，如：USD，代表美元；INR 代表印度卢比
        value : 货币价值，使用浮点小数；如果传入非数字将强制默认为0
        contentType : 内容类型(商品、剧、小说、礼包、套餐等)，单个传product，传多个product_group
        contentIds : 内容编号，content_type=product时，Id数组只能传入一个Id，若是使用content_type=product_group时，Id可以传入多个
        */
        // 设置事件数据
        eventData = new HM_EventDataModel(
                "",
                "",
                "BI_ProductView",
                "USD",
                "9.99",
                "product",
                null);
        eventInfo = new HM_EventInfoModel(
                false,   //是否延迟上报，目前只有购物事件可以开启，如另有需要可以联系技术支持
                false,   //是否是关键事件，关键事件不上报媒体，只在BI统计
                eventData);   //事件具体内容
        //上报事件
        web2AppInstance.eventPostWithEventInfo(eventInfo);

        /*
        当用户订阅某套餐或项目时，上报这个事件
        poid : 付费订单Id，第三方支付返回的订单id、账单id或流水id等唯一标识，可通过该id与后台用户数据匹配上。Purchase事件必传，非付费事件可传空
        eventID : 事件ID，需要确保唯一性，web2app的后台会做幂等性处理；可传空字符串，SDK默认会生成GUID上报。
        event_name : 事件名，推荐使用BI事件流对应的事件名称：BI_Subscribe
        currency : 货币单位，使用国际标准货币代码，如：USD，代表美元；INR 代表印度卢比
        value : 货币价值，使用浮点小数；如果传入非数字将强制默认为0
        contentType : 内容类型(商品、剧、小说、礼包、套餐等)，单个传product，传多个product_group
        contentIds : 内容编号，content_type=product时，Id数组只能传入一个Id，若是使用content_type=product_group时，Id可以传入多个
        */
        // 设置事件数据
        eventData = new HM_EventDataModel(
                "",
                "",
                "BI_Subscribe",
                "USD",
                "99.99",
                "product",
                null);
        eventInfo = new HM_EventInfoModel(
                false,   //是否延迟上报，目前只有购物事件可以开启，如另有需要可以联系技术支持
                false,   //是否是关键事件，关键事件不上报媒体，只在BI统计
                eventData);   //事件具体内容
        //上报事件
        web2AppInstance.eventPostWithEventInfo(eventInfo);

        // 在订阅事件上报之后，需要再次上报一个付费事件，用于投放时做数据对齐
        // 设置事件数据
        eventData = new HM_EventDataModel(
                "poid112233",
                "",
                "BI_Purchase",
                "USD",
                "9.99",
                "product_group",
                Arrays.asList("vid000001", "vid000002", "vid000003"));
        eventInfo = new HM_EventInfoModel(
                true,   //是否延迟上报，目前只有购物事件可以开启，如另有需要可以联系技术支持
                false,   //是否是关键事件，关键事件不上报媒体，只在BI统计
                eventData);   //事件具体内容
        //上报事件
        web2AppInstance.eventPostWithEventInfo(eventInfo);

        /*
        只统计次数，可在BI上看到
        */
        web2AppInstance = HM_Web2App.getInstance(getApplication());// 实例对象
        eventInfo = new HM_EventInfoModel(
                false,   //是否延迟上报，目前只有购物事件可以开启，如另有需要可以联系技术支持
                true,   //是否是关键事件，关键事件不上报媒体，只在BI统计
                null);   //事件具体内容
        //上报事件
        web2AppInstance.eventPostWithEventInfo(eventInfo);

    }

}