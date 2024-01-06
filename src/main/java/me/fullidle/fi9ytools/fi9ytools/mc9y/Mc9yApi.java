package me.fullidle.fi9ytools.fi9ytools.mc9y;

import com.rylinaux.plugman.PlugMan;
import lombok.SneakyThrows;
import me.fullidle.fi9ytools.fi9ytools.mc9y.post.ForumPost;
import me.fullidle.fi9ytools.fi9ytools.mc9y.post.Post;
import me.fullidle.fi9ytools.fi9ytools.mc9y.post.ResourcePost;
import me.fullidle.fi9ytools.fi9ytools.util.SomeMethod;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;

import static me.fullidle.fi9ytools.fi9ytools.data.FI9yData.*;

public class Mc9yApi {
    /**
     * 全局搜索,资源,论坛帖子都会返回
     * @param keywords 关键词
     * @param users 用户名(指定用户名的情况下需要)
     * @param searchTitlesOnly 仅搜索标题
     * @return 返回所有搜索结果,如果没有结果(也就是搜不到则会空的组)
     */
    @SneakyThrows
    public static Post[] search(String keywords, String users, boolean searchTitlesOnly){
        String xfToken = SomeMethod.getSearchXfToken();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "keywords="+
                URLEncoder.encode(keywords,"UTF-8") +
                (searchTitlesOnly?"c[title_only]=1":"")+
                "&c[users]="+users+"&_xfToken="+URLEncoder.encode(xfToken,"UTF-8"));
        Request request = SomeMethod.getDefaultPostMethodBuilder(new Request.Builder()
                .url("https://bbs.mc9y.net/search/search"), body).build();
        Response response = client.newCall(request).execute();
        ArrayList<Post> posts = new ArrayList<>();
        if (response.code() == 303) {
            String targetUrl = response.headers().get("Location");
            Request targetRequest = SomeMethod.getDefaultGETMethodBuilder(targetUrl).build();
            Response targetResponse = client.newCall(targetRequest).execute();
            Document parse = Jsoup.parse(targetResponse.body().string());
            Elements list = parse.select(".contentRow-title a");
            for (Element el : list) {
                String href = el.attr("href");
                String postUrl = targetRequest.url().resolve(href).toString();
                if (href.startsWith("/resources")){
                    posts.add(ResourcePost.getInstance(postUrl));
                }else{
                    posts.add(ForumPost.getInstance(postUrl));
                }
                /*待完善其他Post*/
            }
        }
        response.close();
        return posts.toArray(new Post[0]);
    }

    /**
     * 安装远程云端插件,非插件不会下载在服务端/plugins文件夹内,且不会进行安装
     */
    public static File installCloudPlugin(String downloadUrl) throws Exception {
        File pluginsFolder = plugin.getDataFolder().
                getParentFile();
        File file = SomeMethod.downloadPlugInToFolder(downloadUrl,
                pluginsFolder.toPath());
        PlugMan.getInstance().getPluginUtil().load(file.getName().replace(".jar",""));
        return file;
    }

    /**
     * @return 返回最新的资源
     */
    @SneakyThrows
    public static ResourcePost[] getLatestResources(){
        Request request = SomeMethod.getDefaultGETMethodBuilder("https://bbs.mc9y.net/whats-new/resources/?skip=1").build();
        Response response = client.newCall(request).execute();
        String url = response.headers().get("Location");
        request = SomeMethod.getDefaultGETMethodBuilder(url).build();
        response = client.newCall(request).execute();
        Document parse = Jsoup.parse(response.body().string());
        HttpUrl urled = request.url();
        return parse.select(".structItem-title a[data-tp-primary]")
                .stream()
                .map(el -> ResourcePost.getInstance(urled.resolve(el.attr("href")).toString()))
                .toArray(ResourcePost[]::new);
    }

    /**
     * 登录/切换账号用
     * 请不要没事用这个方法
     * @param account 账号
     * @param password 密码
     */
    @SneakyThrows
    public static void login9y(String account, String password){
        String token = SomeMethod.getXfTokenAndCachingCookies();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType,
                "login="+URLEncoder.encode(account,"UTF-8")+
                        "&password="+URLEncoder.encode(password,"UTF-8")+
                        "&remember=1&_xfRedirect=%2F&_xfToken="+
                        URLEncoder.encode(token,"UTF-8"));
        Request request = SomeMethod.getDefaultPostMethodBuilder(SomeMethod.addDefaultHeader(new Request.Builder()
                .url("https://bbs.mc9y.net/login/login")),body)
                .build();
        Response response = client.newCall(request).execute();
        int code = response.code();
        if (code != 303){
            throw new RuntimeException("Failed to log in to mc9y account! response code:" + code);
        }

        for (Cookie c : Cookie.parseAll(request.url(), response.headers())) {
            loginCookie.put(c.name(),c);
        }
        response.close();
    }
}
