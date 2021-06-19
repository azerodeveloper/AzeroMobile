package com.soundai.azero.lib_surrogate

enum class VerificationType(val type: String) {
    REGISTER("REGISTER"), //注册
    VERIFICATION("VERIFICATION"), //登录
    RESET_PWD("RESET_PWD"); //重置密码

    override fun toString(): String {
        return type
    }
}

enum class Sender(val value: Int) {
    SOUNDAI(0),
    MICROSOUND(1);

    override fun toString(): String {
        return value.toString()
    }
}