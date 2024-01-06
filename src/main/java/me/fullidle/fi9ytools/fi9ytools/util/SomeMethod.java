package me.fullidle.fi9ytools.fi9ytools.util;

import lombok.SneakyThrows;
import me.fullidle.fi9ytools.fi9ytools.data.FI9yData;
import me.fullidle.fi9ytools.fi9ytools.mc9y.post.Post;
import okhttp3.*;
import org.bukkit.command.CommandSender;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static me.fullidle.fi9ytools.fi9ytools.data.FI9yData.*;
public class SomeMethod {
    /**
     * 默认请求头
     */
    public static Request.Builder addDefaultHeader(Request.Builder builder){
        return builder.addHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                .addHeader("accept-language", "zh-CN,zh;q=0.9,en;q=0.8")
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
    }

    /**
     * 添加已登录的cookie数据
     */
    public static Request.Builder addLoginCookieHeader(Request.Builder builder){
        return builder.addHeader("cookie", getSplicedCookie());
    }

    /**
     * 拼接Cookie
     */
    public static String splicingCookies(List<Cookie> cookies) {
        if (cookies == null || cookies.isEmpty()) {
            return "";
        }
        StringJoiner joiner = new StringJoiner("; ");
        for (Cookie cookie : cookies) {
            joiner.add(cookie.name() + "=" + cookie.value());
        }
        return joiner.toString();
    }

    /**
     * 获取登录所需负载_xfToken并将cookie存起来
     * @return 返回登录所需要的xfToken
     */
    @SneakyThrows
    public static String getXfTokenAndCachingCookies(){
        Request request = addDefaultHeader(new Request.Builder()
                .url("https://bbs.mc9y.net/login/login")
                .method("GET", null))
                .build();
        Response response = client.newCall(request).execute();
        Document parse = Jsoup.parse(response.body().string(), Parser.htmlParser());
        Element element = parse.selectXpath("/html/body/div[2]/div/div[1]/div[2]/nav/div/div[3]/div[1]/form/input[2]").get(0);
        String token = element.val();
        List<Cookie> cookies = Cookie.parseAll(request.url(), response.headers());
        for (Cookie cookie : cookies) {
            loginCookie.put(cookie.name(), cookie);
        }
        response.close();
        return token;
    }

    public static String getSplicedCookie(){
        ArrayList<Cookie> cookies = new ArrayList<>(loginCookie.values());
        return splicingCookies(cookies);
    }

    public static Request.Builder getDefaultGETMethodBuilder(String url){
        return addLoginCookieHeader(addDefaultHeader(new Request.Builder()
                .url(url)
                .method("GET", null)));
    }
    public static Request.Builder getDefaultPostMethodBuilder(Request.Builder builder,RequestBody body){
        return addLoginCookieHeader(addDefaultHeader(builder)
                .addHeader("content-type", body.contentType().type())
                .method("POST", body));
    }

    //getSearchXfToken
    @SneakyThrows
    public static String getSearchXfToken(){
        Request request = getDefaultGETMethodBuilder("https://bbs.mc9y.net/search/").build();
        Response response = client.newCall(request).execute();
        String xfToken = Jsoup.parse(response.body().string()).selectXpath("//*[@id=\"top\"]/div[1]/div[2]/nav/div/div[3]/div[2]/form/input").first().val();
        response.close();
        return xfToken;
    }

    @SneakyThrows
    public static File downloadPlugInToFolder(String url, Path folderPath){
        OkHttpClient client = new OkHttpClient().newBuilder()
                .followRedirects(false)
                .build();
        Request request = getDefaultGETMethodBuilder(url).build();
        Response response = client.newCall(request).execute();
        byte[] bytes = response.body().bytes();
        String fileName = response.headers().get("Content-Disposition")
                .split(";", 2)[1]
                .replaceAll("[\\s\"]", "")
                .substring("filename=".length());
        response.close();
        File file = getSuitableFile(new File(folderPath.resolve(fileName).toAbsolutePath().toString()));
        FileOutputStream stream = new FileOutputStream(file);
        stream.write(bytes);
        stream.close();
        return file;
    }

    private static int copyNumber = 0;
    @SneakyThrows
    private static File getSuitableFile(File file){
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
        }else {
            copyNumber++;
            String[] split = file.getName().split(",");
            if (split.length>1){
                String suffix = split[split.length - 1];
                return getSuitableFile(new File(file.getAbsolutePath().replace(file.getName(),file.getName().replace(suffix,copyNumber+suffix))));
            }else {
                return getSuitableFile(new File(file.getAbsolutePath()+copyNumber));
            }
        }
        return file;
    }

    /**
     * @return 返回默认下载地址
     */
    public static String getDownloadPath(){
        return plugin.getConfig().getString("defaultDownloadDirectory").replace("{user}", System.getProperty("user.home"));
    }
    /*显示缓存*/
    public static void showCache(CommandSender sender){
        for (Map.Entry<Integer, Post> entry : FI9yData.searchCache.entrySet()) {
            sender.sendMessage("§a"+ entry.getKey() +"."+entry.getValue().getTitle());
        }
    }
}
