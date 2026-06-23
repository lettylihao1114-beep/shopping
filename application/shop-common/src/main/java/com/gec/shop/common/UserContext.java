package com.gec.shop.common;

public class UserContext {
    private static final ThreadLocal<LoginUser> tl = new ThreadLocal<>();
    public static void set(LoginUser u) { tl.set(u); }
    public static LoginUser get() { return tl.get(); }
    public static void remove() { tl.remove(); }
    public static Long getUserId() { return get() != null ? get().getUserId() : null; }
    public static String getRole() { return get() != null ? get().getRole() : null; }
}
