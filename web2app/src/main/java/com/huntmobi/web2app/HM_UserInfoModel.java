package com.huntmobi.web2app;

import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HM_UserInfoModel {

    /**
     * 邮箱，有则传入，无则空
     */
    private String email;

    /**
     * FB登录ID，有则传入，无则传空
     */
    private String fbLoginId;

    /**
     * 电话号码，有则传入，无则留空
     */
    private String phone;

    /**
     * 国家，请按照 ISO 3166-1 二位字母代码表示方式使用小写二位字母国家/地区代码。
     */
    private String country;

    /**
     * 邮编 - 使用小写字母，且不可包含空格和破折号。美国邮编只限使用前 5 位数。英国邮编请使用邮域 + 邮区 + 邮政部门格式。
     */
    private String zipCode;

    /**
     * 城市 - 小写字母（移除所有空格）推荐使用罗马字母字符 a 至 z。仅限小写字母，且不可包含标点符号、特殊字符和空格。若使用特殊字符，则须按 UTF-8 格式对文本进行编码。
     */
    private String city;

    /**
     * 州或省, 以两个小写字母表示的州或省代码 - 使用 2 个字符的 ANSI 缩写代码，必须为小写字母。请使用小写字母对美国境外的州/省/自治区/直辖市名称作标准化处理，且不可包含标点符号、特殊字符和空格。
     */
    private String state;

    /**
     * 性别 - f 表示女性, m 表示男性
     */
    private String gender;

    /**
     * 名字 - 不包含姓氏 推荐使用罗马字母字符 a 至 z。仅限小写字母，且不可包含标点符号。若使用特殊字符，则须按 UTF-8 格式对文本进行编码
     */
    private String firstName;

    /**
     * 姓氏 - 不包含名字 推荐使用罗马字母字符 a 至 z。仅限小写字母，且不可包含标点符号。若使用特殊字符，则须按 UTF-8 格式对文本进行编码。
     */
    private String lastName;

    /**
     * 出生年月 - 输入：2/16/1997 标准化格式：19970216 格式规则 YYYYMMDD
     */
    private String birthday;

    /**
     * 构造函数
     *
     * @param email      邮箱
     * @param fbLoginId  FB登录ID
     * @param phone      电话号码
     * @param country    国家
     * @param zipCode    邮编
     * @param city       城市
     * @param state      州或省
     * @param gender     性别
     * @param firstName  名字
     * @param lastName   姓氏
     * @param birthday   出生年月
     */
    public HM_UserInfoModel(String email, String fbLoginId, String phone, String country,
                            String zipCode, String city, String state, String gender,
                            String firstName, String lastName, String birthday) {
        this.email = email != null ? email : "";  // 防止 null
        this.fbLoginId = fbLoginId != null ? fbLoginId : "";
        this.phone = phone != null ? phone : "";
        this.country = country != null ? country : "";
        this.zipCode = zipCode != null ? zipCode : "";
        this.city = city != null ? city : "";
        this.state = state != null ? state : "";
        this.gender = gender != null ? gender : "";
        this.firstName = firstName != null ? firstName : "";
        this.lastName = lastName != null ? lastName : "";
        this.birthday = birthday != null ? birthday : "";
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email : "";
    }

    public String getFbLoginId() {
        return fbLoginId;
    }

    public void setFbLoginId(String fbLoginId) {
        this.fbLoginId = fbLoginId != null ? fbLoginId : "";
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone != null ? phone : "";
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country != null ? country : "";
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode != null ? zipCode : "";
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city != null ? city : "";
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state != null ? state : "";
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender != null ? gender : "";
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName != null ? firstName : "";
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName != null ? lastName : "";
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday != null ? birthday : "";
    }

    /**
     * 将对象转换为 Map
     */
    public JSONObject toMap() {
        Map<String, String> map = new HashMap<>();
        map.put("em", email);
        map.put("fb_login_id", fbLoginId);
        map.put("ph", phone);
        map.put("country", country);
        map.put("zp", zipCode);
        map.put("ct", city);
        map.put("st", state);
        map.put("ge", gender);
        map.put("fn", firstName);
        map.put("ln", lastName);
        map.put("db", birthday);
        return new JSONObject(Collections.unmodifiableMap(map));
    }
}
