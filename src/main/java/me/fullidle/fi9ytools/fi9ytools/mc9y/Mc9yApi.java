package me.fullidle.fi9ytools.fi9ytools.mc9y;

import com.rylinaux.plugman.PlugMan;
import lombok.SneakyThrows;
import me.fullidle.fi9ytools.fi9ytools.data.SearchCache;
import me.fullidle.fi9ytools.fi9ytools.mc9y.post.ForumPost;
import me.fullidle.fi9ytools.fi9ytools.mc9y.post.Post;
import me.fullidle.fi9ytools.fi9ytools.mc9y.post.resource.ResourcePost;
import me.fullidle.fi9ytools.fi9ytools.util.SomeMethod;
import okhttp3.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static me.fullidle.fi9ytools.fi9ytools.data.FI9yData.*;
import static me.fullidle.fi9ytools.fi9ytools.data.SearchCache.cacheSearchData;
import static me.fullidle.fi9ytools.fi9ytools.data.SearchCache.searchNextPageUrl;

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
        SearchCache.clear();
        String xfToken = SomeMethod.getSearchXfToken();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "keywords="+
                URLEncoder.encode(keywords,"UTF-8") +
                (searchTitlesOnly?"c[title_only]=1":"")+
                "&c[users]="+users+"&_xfToken="+URLEncoder.encode(xfToken,"UTF-8"));
        Request request = SomeMethod.getDefaultPostMethodBuilder(new Request.Builder()
                .url("https://bbs.mc9y.net/search/search"), body).build();
        Response response = client.newCall(request).execute();
        ArrayList<Post> posts = null;
        if (response.code() == 303) {
            String targetUrl = response.headers().get("Location");
            posts = getSearchPagePost(targetUrl);
        }
        response.close();
        return posts==null?new Post[0]:posts.toArray(new Post[0]);
    }

    @SneakyThrows
    public static ArrayList<Post> getSearchPagePost(String targetUrl){
        ArrayList<Post> posts = new ArrayList<>();
        Request request = SomeMethod.getDefaultGETMethodBuilder(targetUrl).build();
        Response response = client.newCall(request).execute();
        Document parse = Jsoup.parse(response.body().string());
        Elements list = parse.select(".contentRow-title a");
        {
            /*缓存下一页地址*/
            String href = parse.select("a.pageNav-jump--next").first().attr("href");
            searchNextPageUrl = request.url().resolve(href).toString();
        }

        for (Element el : list) {
            String href = el.attr("href");
            String postUrl = request.url().resolve(href).toString();
            if (href.startsWith("/resources")){
                if (href.contains("update")){
                    continue;
                }
                posts.add(ResourcePost.getInstance(postUrl));
            }else{
                posts.add(ForumPost.getInstance(postUrl));
            }
            /*待完善其他Post*/
        }
        cacheSearchData(posts);
        return posts;
    }

    /**
     * 安装远程云端插件,只用于下载Jar文件,请不要用在下载其他的类型的文件！
     */
    public static File installCloudPlugin(String downloadUrl, boolean canUpdate) throws Exception {
        File pluginsFolder = plugin.getDataFolder().
                getParentFile();
        File file = SomeMethod.downloadPlugInToFolder(downloadUrl,
                pluginsFolder.toPath());

        JarFile jarFile = new JarFile(file);
        JarEntry jarEntry = jarFile.getJarEntry("plugin.yml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(jarEntry)));
        YamlConfiguration pluginYaml = YamlConfiguration.loadConfiguration(reader);
        String pluginName = pluginYaml.getString("name");
        Plugin targetPlugin = Bukkit.getPluginManager().getPlugin(pluginName);
        reader.close();
        if (targetPlugin != null) {
            if (canUpdate){
                PlugMan.getInstance().getPluginUtil().unload(targetPlugin);
            }
            return file;
        }
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
        HttpUrl curled = request.url();
        return parse.select(".structItem-title a[data-tp-primary]")
                .stream()
                .map(el -> ResourcePost.getInstance(curled.resolve(el.attr("href")).toString()))
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
