package me.fullidle.fi9ytools.fi9ytools.data;

import com.google.gson.Gson;
import me.fullidle.fi9ytools.fi9ytools.Main;
import lombok.SneakyThrows;
import me.fullidle.fi9ytools.fi9ytools.mc9y.Mc9yApi;
import okhttp3.Cookie;
import okhttp3.OkHttpClient;

import java.util.HashMap;
import java.util.Map;

public class FI9yData {
    public static Main plugin;
    public static final Gson gson = new Gson();
    public static final OkHttpClient client = new OkHttpClient().newBuilder()
            .followRedirects(false)
            .build();
    public static final Map<String, Cookie> loginCookie = new HashMap<>();

    @SneakyThrows
    public static void init(Main plugin){
        FI9yData.plugin = plugin;
        {
            client.dispatcher().cancelAll();
            client.dispatcher().executorService().shutdown();
            loginCookie.clear();
        }

        Cookie xf_notice_dismiss = new Cookie.Builder()
                .path("/")
                .domain("bbs.mc9y.net")
                .value("2")
                .httpOnly()
                .secure()
                .name("xf_notice_dismiss")
                .build();
        loginCookie.put(xf_notice_dismiss.name(),xf_notice_dismiss);
        {
            String account = plugin.getConfig().getString("account");
            String password = plugin.getConfig().getString("password");
            if (account == null || password == null) {
                plugin.getLogger().info("§c请先在config.yml中配置账户密码!(使用重载重新登录)");
                return;
            }
            try {
                Mc9yApi.login9y(account, password);
                plugin.getLogger().info("§a登录成功!");
            } catch (Exception e) {
                plugin.getLogger().info("§c登入失败!,请检查账户密码配置并尝试重载!");
                e.printStackTrace();
            }
            return;
        }
    }
}
