package org.example.spotify_music.common;

// import lombok.Data; // 删掉或注释掉这个

public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    // 手动生成的 Getter 和 Setter
    public Integer getCode() {
        return code;
    }
    public void setCode(Integer code) {
        this.code = code;
    }
    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }
    public T getData() {
        return data;
    }
    public void setData(T data) {
        this.data = data;
    }

    // 下面这两个静态方法保持不变
    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(200); // 注意这里变成了调 set 方法
        r.setMsg("操作成功");
        r.setData(data);
        return r;
    }

    public static <T> Result<T> error(String msg) {
        return error(500, msg);
    }

    public static <T> Result<T> error(int code, String msg) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }
}