/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.network.api;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;


@SuppressWarnings("unused")
abstract public class BaseModel<T> implements Serializable {
    @JSONField
    public Integer code;
    @JSONField
    public String msg;
    @JSONField
    public T data;
}
